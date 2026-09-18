import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { ConfigProvider } from 'antd'
import { AxiosError, type AxiosAdapter, type InternalAxiosRequestConfig } from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from '@/services/http'
import { useAuthStore } from '@/stores/authStore'
import type { DrugCatalog, InventoryBatch, RxReview } from '@/types/pharma'
import { PharmaPage } from './PharmaPage'

const patientId = '2099735874343563265'
const prescriptionId = '2099735874343563266'
const reviewId = '2099735874343563267'
const drugId = '2099735874343563268'
const batchId = '2099735874343563269'
const drug: DrugCatalog = {
  id: drugId, drugCode: 'YP001', genericName: '测试药品', dosageForm: '片剂', strength: '500mg', manufacturer: '测试厂家',
  packageUnit: '盒', minUnit: '片', conversionFactor: 10, purchasePrice: 1, retailPrice: 2, prescriptionType: 'RX', isActive: 1,
}
const batch: InventoryBatch = {
  id: batchId, drugId, drugName: drug.genericName, warehouseCode: 'OPD', batchNo: 'B001', expiryDate: '2030-12-31',
  unitCost: 1, quantity: 3, availableQuantity: 3, lockedQuantity: 0, nearExpiry: false, expired: false,
}
const review: RxReview = {
  id: reviewId, reviewNo: 'SH001', patientId, doctorId: 8, prescriptionType: 'RX', reviewStatus: 'PENDING',
  warningCount: 0, errorCount: 0, items: [],
}
const originalAdapter = http.defaults.adapter
const originalSession = useAuthStore.getState().session
const transport = vi.fn<AxiosAdapter>()
const failures = new Map<string, string | { code: number; message: string } | null>()
let client: QueryClient

function response(config: InternalAxiosRequestConfig, data: unknown) {
  return { config, status: 200, statusText: 'OK', headers: {}, data: JSON.stringify({ code: 200, message: '成功', data }) }
}

function requests(method: string, url: string) {
  return transport.mock.calls.map(([config]) => config).filter((config) => config.method === method && config.url === url)
}

async function renderPage() {
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  render(<ConfigProvider theme={{ token: { motion: false } }}><QueryClientProvider client={client}><PharmaPage /></QueryClientProvider></ConfigProvider>)
  await screen.findByText('测试药品')
}

async function visibleDialog(name: string) {
  const element = await screen.findByRole('dialog', { name })
  await waitFor(() => expect(element).toBeVisible(), { timeout: 3_000 })
  return within(element)
}

async function openDispense() {
  fireEvent.click(screen.getByRole('tab', { name: '门诊发药' }))
  fireEvent.click(await screen.findByRole('button', { name: /处方发药/ }))
  return visibleDialog('处方审核后发药')
}

async function fillDispense(dialog: ReturnType<typeof within>) {
  fireEvent.change(dialog.getByLabelText('患者数字 ID'), { target: { value: patientId } })
  fireEvent.change(dialog.getByLabelText('处方 ID'), { target: { value: prescriptionId } })
  fireEvent.change(dialog.getByLabelText('审核记录 ID'), { target: { value: reviewId } })
  fireEvent.mouseDown(dialog.getByLabelText('药品'))
  fireEvent.click(await screen.findByText('YP001 测试药品 500mg'))
  fireEvent.change(dialog.getByLabelText('发药数量'), { target: { value: '2' } })
}

beforeEach(() => {
  failures.clear()
  transport.mockReset()
  // Replace only the transport: real controls, React Query, pharmaApi and Axios serialization/interceptors execute.
  transport.mockImplementation(async (config) => {
    const key = `${config.method} ${config.url}`
    if (failures.has(key)) {
      const detail = failures.get(key)
      failures.delete(key)
      if (detail === null) throw new AxiosError('Network Error', AxiosError.ERR_NETWORK, config)
      throw new AxiosError('Request failed with status code 400', AxiosError.ERR_BAD_REQUEST, config, undefined, {
        config, status: 400, statusText: 'Bad Request', headers: {},
        data: typeof detail === 'object' ? detail : { code: 400, message: detail, data: null },
      })
    }
    if (config.method === 'get') {
      if (config.url === '/pharma/drugs') return response(config, { records: [drug], total: 1 })
      if (config.url === '/pharma/inventory/batches') return response(config, [batch])
      if (config.url === '/pharma/inventory/near-expiry') return response(config, [])
      if (config.url === '/pharma/rx-reviews') return response(config, { records: [review], total: 1 })
      if (config.url === `/pharma/rx-reviews/${reviewId}`) return response(config, review)
      if (config.url?.startsWith('/pharma/dispenses/patient/')) {
        const id = config.url.split('/').at(-1)
        return response(config, [{ id, dispenseNo: `FY-${id}`, prescriptionId, dispenseStatus: 'DISPENSED' }])
      }
    }
    if (key === 'post /pharma/inventory/operations') return response(config, { id: '2099735874343563270' })
    if (key === 'post /pharma/dispenses') return response(config, { id: '2099735874343563271' })
    if (key === `put /pharma/drugs/${drugId}`) return response(config, drug)
    if (key === `put /pharma/rx-reviews/${reviewId}/approve`) return response(config, { ...review, reviewStatus: 'APPROVED' })
    if (key === `put /pharma/rx-reviews/${reviewId}/reject`) return response(config, { ...review, reviewStatus: 'REJECTED' })
    throw new Error(`Unexpected request: ${key}`)
  })
  http.defaults.adapter = transport
  useAuthStore.setState({ session: {
    accessToken: 'test-token', refreshToken: 'test-refresh', userId: 8, username: 'pharmacist', realName: '王药师',
    deptId: 2, deptName: '药房', roles: [], permissions: [],
  } })
})

afterEach(() => {
  cleanup()
  client?.clear()
  http.defaults.adapter = originalAdapter
  useAuthStore.setState({ session: originalSession })
})

describe('PharmaPage request and error interactions', () => {
  it.each([
    ['库存不足，可用库存为 3', '出库失败，请核对库存数量或检查服务连接后重试。（库存不足，可用库存为 3）'],
    [{ code: 4002, message: 'insufficient stock quantity' }, '库存不足，请核对可用库存后重试。'],
    [null, '出库失败，请核对库存数量或检查服务连接后重试。'],
  ] as const)('keeps outbound inputs and permits retry after %s', async (failure, expected) => {
    failures.set('post /pharma/inventory/operations', failure)
    await renderPage()
    fireEvent.click(screen.getByRole('tab', { name: '库存管理' }))
    fireEvent.click(await screen.findByRole('button', { name: /出\s*库/ }))
    const dialog = await visibleDialog('出库')
    fireEvent.change(dialog.getByRole('spinbutton', { name: '数量' }), { target: { value: '4' } })
    fireEvent.change(dialog.getByRole('textbox', { name: '原因/备注' }), { target: { value: '门诊补药' } })
    fireEvent.click(dialog.getByRole('button', { name: 'OK' }))

    expect(await screen.findByText(expected!)).toBeVisible()
    expect(screen.getByRole('dialog', { name: '出库' })).toBeVisible()
    expect(dialog.getByRole('spinbutton', { name: '数量' })).toHaveValue('4.0000')
    expect(dialog.getByRole('textbox', { name: '原因/备注' })).toHaveValue('门诊补药')
    expect(JSON.parse(requests('post', '/pharma/inventory/operations')[0].data)).toMatchObject({
      operationType: 'OUTBOUND', drugId, batchId, quantity: 4, reason: '门诊补药',
    })

    fireEvent.change(dialog.getByRole('spinbutton', { name: '数量' }), { target: { value: '2' } })
    fireEvent.click(dialog.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('出库完成')).toBeVisible()
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '出库' })).not.toBeInTheDocument())
    expect(requests('post', '/pharma/inventory/operations')).toHaveLength(2)
    expect(JSON.parse(requests('post', '/pharma/inventory/operations')[1].data)).toMatchObject({ drugId, batchId, quantity: 2, reason: '门诊补药' })
  }, 30_000)

  it('queries distinct 19-digit patient IDs unchanged and never queries empty or invalid IDs', async () => {
    await renderPage()
    fireEvent.click(screen.getByRole('tab', { name: '门诊发药' }))
    const input = await screen.findByRole('textbox', { name: '查询患者 ID' })
    expect(input).toHaveAttribute('inputmode', 'numeric')
    expect(screen.getByText('输入患者数字 ID 查询发药记录')).toBeVisible()
    for (const invalid of ['0', '-1', '1.5', '1e3', 'abc', ' 123', '123 ', '001', '']) {
      fireEvent.change(input, { target: { value: invalid } })
      expect(await screen.findByText(invalid ? '患者 ID 必须为正整数' : '输入患者数字 ID 查询发药记录')).toBeVisible()
    }
    expect(transport.mock.calls.some(([config]) => config.url?.startsWith('/pharma/dispenses/patient/'))).toBe(false)

    for (const id of [patientId, prescriptionId]) {
      fireEvent.change(input, { target: { value: id } })
      expect(await screen.findByText(`FY-${id}`)).toBeVisible()
      expect(input).toHaveValue(id)
      expect(requests('get', `/pharma/dispenses/patient/${id}`)).toHaveLength(1)
    }
    fireEvent.change(input, { target: { value: '' } })
    expect(await screen.findByText('输入患者数字 ID 查询发药记录')).toBeVisible()
    expect(screen.queryByText(`FY-${prescriptionId}`)).not.toBeInTheDocument()
    expect(transport.mock.calls.filter(([config]) => config.url?.startsWith('/pharma/dispenses/patient/'))).toHaveLength(2)
  }, 30_000)

  it('serializes every entered dispense ID unchanged and keeps the form for retry after a business error', async () => {
    failures.set('post /pharma/dispenses', '处方尚未审核通过')
    await renderPage()
    const dialog = await openDispense()
    await fillDispense(dialog)
    for (const label of ['患者数字 ID', '处方 ID', '审核记录 ID']) {
      expect(dialog.getByRole('textbox', { name: label })).toHaveAttribute('inputmode', 'numeric')
    }
    fireEvent.click(dialog.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('发药失败，请核对处方审核状态、库存或检查服务连接后重试。（处方尚未审核通过）')).toBeVisible()
    expect(dialog.getByRole('textbox', { name: '患者数字 ID' })).toHaveValue(patientId)
    expect(dialog.getByRole('textbox', { name: '处方 ID' })).toHaveValue(prescriptionId)
    expect(dialog.getByRole('textbox', { name: '审核记录 ID' })).toHaveValue(reviewId)
    const payload = JSON.parse(requests('post', '/pharma/dispenses')[0].data)
    expect(payload).toEqual({
      patientId, prescriptionId, rxReviewId: reviewId, warehouseCode: 'OPD', pharmacistId: 8, pharmacistName: '王药师',
      items: [{ drugId, quantity: 2, unit: '盒' }],
    })
    fireEvent.click(dialog.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('发药完成，库存流水已生成')).toBeVisible()
    expect(requests('post', '/pharma/dispenses')).toHaveLength(2)
    expect(JSON.parse(requests('post', '/pharma/dispenses')[1].data)).toEqual(payload)
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '处方审核后发药' })).not.toBeInTheDocument())
  }, 30_000)

  it.each(['患者数字 ID', '处方 ID', '审核记录 ID'])('does not submit when %s is empty or invalid', async (label) => {
    await renderPage()
    const dialog = await openDispense()
    await fillDispense(dialog)
    const input = dialog.getByRole('textbox', { name: label })
    const submitButton = dialog.getByRole('button', { name: 'OK' })
    for (const invalid of ['', '0', '-1', '1.5', '1e3', 'abc', ' 123']) {
      await act(async () => {
        fireEvent.change(input, { target: { value: invalid } })
        fireEvent.click(submitButton)
      })
      expect(await dialog.findByText(invalid ? 'ID 必须为正整数' : '请输入 ID')).toBeVisible()
      expect(input).toHaveAttribute('aria-invalid', 'true')
      expect(requests('post', '/pharma/dispenses')).toHaveLength(0)
    }
  }, 30_000)

  it('reports catalog save errors without closing or resetting the edited drug', async () => {
    failures.set(`put /pharma/drugs/${drugId}`, '药品编码已存在')
    await renderPage()
    fireEvent.click(screen.getByRole('button', { name: /编\s*辑/ }))
    const dialog = await visibleDialog('编辑药品')
    fireEvent.change(dialog.getByRole('textbox', { name: '通用名' }), { target: { value: '更正后的药品' } })
    fireEvent.click(dialog.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('药品目录保存失败，请核对药品信息或检查服务连接后重试。（药品编码已存在）')).toBeVisible()
    expect(dialog.getByRole('textbox', { name: '通用名' })).toHaveValue('更正后的药品')
    expect(screen.getByRole('dialog', { name: '编辑药品' })).toBeVisible()
  }, 30_000)

  it('reports review-load network errors and permits retry', async () => {
    failures.set(`get /pharma/rx-reviews/${reviewId}`, null)
    await renderPage()
    fireEvent.click(screen.getByRole('tab', { name: '处方审核' }))
    fireEvent.click(await screen.findByRole('button', { name: '审核/打印' }))
    expect(await screen.findByText('处方审核单加载失败，请检查服务连接后重试。')).toBeVisible()
    expect(screen.queryByRole('dialog', { name: '处方审核单' })).not.toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: '审核/打印' }))
    await waitFor(() => expect(screen.getByRole('dialog', { name: '处方审核单' })).toBeVisible(), { timeout: 3_000 })
    expect(requests('get', `/pharma/rx-reviews/${reviewId}`)).toHaveLength(2)
  }, 30_000)

  it.each([
    ['approve', /通\s*过/, '审核状态已变更'],
    ['reject', /驳\s*回/, '驳回失败，请稍后重试'],
  ] as const)('preserves the review and rejection reason after %s fails', async (action, button, detail) => {
    failures.set(`put /pharma/rx-reviews/${reviewId}/${action}`, detail)
    await renderPage()
    fireEvent.click(screen.getByRole('tab', { name: '处方审核' }))
    fireEvent.click(await screen.findByRole('button', { name: '审核/打印' }))
    const dialog = await visibleDialog('处方审核单')
    fireEvent.change(dialog.getByPlaceholderText('驳回原因'), { target: { value: '重复用药' } })
    fireEvent.click(dialog.getByRole('button', { name: button }))
    expect(await screen.findByText(detail, { exact: false })).toBeVisible()
    await waitFor(() => expect(screen.getByRole('dialog', { name: '处方审核单' })).toBeVisible(), { timeout: 3_000 })
    expect(dialog.getByPlaceholderText('驳回原因')).toHaveValue('重复用药')
    expect(requests('put', `/pharma/rx-reviews/${reviewId}/${action}`)).toHaveLength(1)
  }, 30_000)
})
