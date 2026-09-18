import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { ConfigProvider } from 'antd'
import { AxiosError, type AxiosAdapter, type InternalAxiosRequestConfig } from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from '@/services/http'
import { useAuthStore } from '@/stores/authStore'
import type { Admission, EmergencyTriage } from '@/types/m8'
import { InpatientEmergencyPage } from './InpatientEmergencyPage'

const patientId = '2099735874343563265'
const plannedId = '2099735874343563266'
const admittedId = '2099735874343563267'
const dischargedId = '2099735874343563268'
const cancelledId = '2099735874343563269'
const bedId = '2099735874343563270'
const targetDeptId = '2099735874343563271'
const targetWardId = '2099735874343563272'
const triageId = '2099735874343563273'
const bed = { id: bedId, deptId: targetDeptId, wardId: targetWardId, wardName: '心内二区', bedNo: '08', bedType: 'NORMAL', bedStatus: 'AVAILABLE' }
const originalAdapter = http.defaults.adapter
const originalSession = useAuthStore.getState().session
const transport = vi.fn<AxiosAdapter>()
const failures = new Map<string, number>()
let client: QueryClient
let admissionRows: ReturnType<typeof admission>[]
let emergencyRows: ReturnType<typeof emergency>[]

// Transport fixtures deliberately use the string IDs returned by the backend, despite legacy number declarations.
function admission(id: string, admissionStatus: Admission['admissionStatus'], admissionNo: string) {
  return { id, admissionNo, patientId, patientName: '张三', admissionStatus, admissionType: 'NORMAL',
    deptId: '2099735874343563280', wardId: '2099735874343563281', bedId: '2099735874343563282',
    doctorId: '2099735874343563283', admissionDate: '2026-09-18', preliminaryDiagnosis: '待查胸痛' }
}

function emergency(id = triageId, triageStatus: EmergencyTriage['triageStatus'] = 'WAITING') {
  return { id, triageNo: `FZ-${id}`, patientId, triageLevel: 2, triageTime: '2026-09-18T09:00:00', chiefComplaint: '胸痛两小时', triageStatus }
}

function response(config: InternalAxiosRequestConfig, data: unknown) {
  return { config, status: 200, statusText: 'OK', headers: {}, data: JSON.stringify({ code: 200, message: '成功', data }) }
}

function requests(method: string, url: string) {
  return transport.mock.calls.map(([config]) => config).filter(config => config.method === method && config.url === url)
}

async function renderPage() {
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  render(<ConfigProvider theme={{ token: { motion: false } }}><QueryClientProvider client={client}><InpatientEmergencyPage /></QueryClientProvider></ConfigProvider>)
  await screen.findByText('ZY-PLANNED')
}

function row(admissionNo: string) {
  return within(screen.getByText(admissionNo).closest('tr') as HTMLElement)
}

async function dialog(name: string) {
  const element = await screen.findByRole('dialog', { name })
  await waitFor(() => expect(element).toBeVisible())
  return within(element)
}

async function choose(input: HTMLElement, label: string) {
  fireEvent.mouseDown(input)
  fireEvent.click(await screen.findByText(label, { selector: '.ant-select-item-option-content' }))
}

async function openTransfer() {
  fireEvent.click(row('ZY-ADMITTED').getByRole('button', { name: '转科/转床' }))
  return dialog('转科/转床')
}

async function fillTransfer(form: ReturnType<typeof within>) {
  await choose(form.getByLabelText('目标床位'), '心内二区 08床（NORMAL）')
  fireEvent.change(form.getByLabelText('转科原因'), { target: { value: '专科继续治疗' } })
}

async function openTriage() {
  fireEvent.click(screen.getByRole('tab', { name: '急诊队列' }))
  fireEvent.click(await screen.findByRole('button', { name: '新增分诊' }))
  return dialog('新增急诊分诊')
}

async function fillTriage(form: ReturnType<typeof within>, id = '1001') {
  fireEvent.change(form.getByLabelText('患者编号'), { target: { value: id } })
  await choose(form.getByLabelText('分诊级别'), 'Ⅱ级 危重')
  fireEvent.change(form.getByLabelText('主诉'), { target: { value: '胸痛两小时' } })
  fireEvent.change(form.getByLabelText('生命体征'), { target: { value: '血压 120/80，血氧 98%' } })
}

beforeEach(() => {
  failures.clear()
  transport.mockReset()
  admissionRows = [admission(plannedId, 'PLANNED', 'ZY-PLANNED'), admission(admittedId, 'ADMITTED', 'ZY-ADMITTED'),
    admission(dischargedId, 'DISCHARGED', 'ZY-DISCHARGED'), admission(cancelledId, 'CANCELLED', 'ZY-CANCELLED')]
  emergencyRows = [emergency()]
  useAuthStore.setState({ session: {
    accessToken: 'test-token', refreshToken: 'test-refresh', userId: 8, username: 'nurse', realName: '王护士',
    deptId: 2, deptName: '急诊科', roles: ['NURSE'], permissions: [],
  } })
  // Only replace HTTP transport; actual m8Api, authStore, permission checks and forms execute.
  transport.mockImplementation(async config => {
    const key = `${config.method} ${config.url}`
    if (failures.has(key)) {
      const status = failures.get(key)!
      failures.delete(key)
      throw new AxiosError('Request failed', AxiosError.ERR_BAD_RESPONSE, config, undefined, {
        config, status, statusText: 'Error', headers: {}, data: { code: status, message: '当前状态不允许此操作', data: null },
      })
    }
    if (key === 'get /patient/admissions') return response(config, { records: admissionRows, total: admissionRows.length, page: 1, size: 20, totalPages: 1 })
    if (key === 'get /patient/inpatient-beds') return response(config, [bed])
    if (key === 'get /emergency/triage') return response(config, emergencyRows)
    if (key === `put /patient/admissions/${plannedId}/admit`) {
      admissionRows = admissionRows.map(item => item.id === plannedId ? { ...item, admissionStatus: 'ADMITTED' } : item)
      return response(config, null)
    }
    if (key === `put /patient/admissions/${admittedId}/transfer`) {
      admissionRows = admissionRows.map(item => item.id === admittedId ? { ...item, deptId: targetDeptId, wardId: targetWardId, bedId } : item)
      return response(config, admissionRows[1])
    }
    if (key === `put /patient/admissions/${admittedId}/discharge`) {
      admissionRows = admissionRows.map(item => item.id === admittedId ? { ...item, admissionStatus: 'DISCHARGED' } : item)
      return response(config, null)
    }
    if (key === 'post /emergency/triage') {
      const created = { ...emergency(), ...JSON.parse(config.data), triageNo: 'FZ-NEW' }
      emergencyRows = [created]
      return response(config, created)
    }
    if (key === `put /emergency/triage/${triageId}/status`) {
      emergencyRows = emergencyRows.map(item => item.id === triageId ? { ...item, triageStatus: config.params.status } : item)
      return response(config, emergencyRows[0])
    }
    throw new Error(`Unexpected request: ${key}`)
  })
  http.defaults.adapter = transport
})

afterEach(() => {
  cleanup()
  client?.clear()
  http.defaults.adapter = originalAdapter
  useAuthStore.setState({ session: originalSession })
})

describe('InpatientEmergencyPage inpatient workflows', () => {
  it('admits an existing PLANNED record with its unchanged string ID and refreshes its actions', async () => {
    await renderPage()
    expect(requests('get', '/patient/admissions')[0].params).toEqual({ page: 1, size: 20 })
    fireEvent.click(row('ZY-PLANNED').getByRole('button', { name: '办理入院' }))
    await waitFor(() => expect(requests('put', `/patient/admissions/${plannedId}/admit`)).toHaveLength(1))
    await waitFor(() => expect(row('ZY-PLANNED').getByRole('button', { name: '转科/转床' })).toBeVisible())
    expect(row('ZY-PLANNED').queryByRole('button', { name: '办理入院' })).not.toBeInTheDocument()
    expect(requests('get', '/patient/admissions')).toHaveLength(2)
  })

  it.each([
    [[], ['NURSE'], false],
    [['operations:bill:create'], ['NURSE'], true],
    [[], ['ADMIN'], true],
  ])('gates admission actions by status and inpatient billing by permissions %j / roles %j', async (permissions, roles, allowed) => {
    useAuthStore.setState({ session: { ...useAuthStore.getState().session!, permissions, roles } })
    await renderPage()
    expect(row('ZY-PLANNED').getByRole('button', { name: '办理入院' })).toBeVisible()
    expect(row('ZY-PLANNED').queryByRole('button', { name: '转科/转床' })).not.toBeInTheDocument()
    expect(row('ZY-PLANNED').queryByRole('button', { name: /^出\s*院$/ })).not.toBeInTheDocument()
    expect(row('ZY-ADMITTED').queryByRole('button', { name: '办理入院' })).not.toBeInTheDocument()
    expect(row('ZY-ADMITTED').getByRole('button', { name: '转科/转床' })).toBeVisible()
    expect(row('ZY-ADMITTED').getByRole('button', { name: /^出\s*院$/ })).toBeVisible()
    if (allowed) expect(row('ZY-ADMITTED').getByRole('button', { name: '住院费用' })).toBeVisible()
    else expect(row('ZY-ADMITTED').queryByRole('button', { name: '住院费用' })).not.toBeInTheDocument()
    expect(row('ZY-DISCHARGED').getByText('已归档只读')).toBeVisible()
    expect(row('ZY-DISCHARGED').queryAllByRole('button')).toHaveLength(0)
    expect(row('ZY-CANCELLED').queryAllByRole('button')).toHaveLength(0)
    expect(transport.mock.calls.every(([config]) => config.method === 'get')).toBe(true)
  })

  it('validates transfer fields, derives department/ward from the bed and sends all IDs unchanged', async () => {
    await renderPage()
    expect(requests('get', '/patient/inpatient-beds')[0].params).toEqual({ wardId: undefined, status: 'AVAILABLE' })
    const form = await openTransfer()
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    await waitFor(() => expect(form.getByLabelText('目标床位')).toHaveAttribute('aria-invalid', 'true'))
    expect(requests('put', `/patient/admissions/${admittedId}/transfer`)).toHaveLength(0)
    await fillTransfer(form)
    expect(form.getByLabelText('目标科室 ID')).toBeDisabled()
    expect(form.getByLabelText('目标病区 ID')).toBeDisabled()
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    await waitFor(() => expect(requests('put', `/patient/admissions/${admittedId}/transfer`)).toHaveLength(1))
    expect(JSON.parse(requests('put', `/patient/admissions/${admittedId}/transfer`)[0].data)).toEqual({
      targetDeptId, targetWardId, targetBedId: bedId, reason: '专科继续治疗',
    })
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '转科/转床' })).not.toBeInTheDocument())
    expect(row('ZY-ADMITTED').getByText(`${targetDeptId} / ${targetWardId} / ${bedId}`)).toBeVisible()
  }, 20_000)

  it('keeps transfer selections and reason after a state conflict, allowing retry', async () => {
    failures.set(`put /patient/admissions/${admittedId}/transfer`, 409)
    await renderPage()
    const form = await openTransfer()
    await fillTransfer(form)
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('转科转床失败，仅在院患者可操作，目标位置不能与当前位置相同。')).toBeVisible()
    expect(screen.getByRole('dialog', { name: '转科/转床' })).toBeVisible()
    expect(form.getByLabelText('转科原因')).toHaveValue('专科继续治疗')
    expect(form.getByText('心内二区 08床（NORMAL）')).toBeVisible()
    expect(requests('get', '/patient/admissions')).toHaveLength(1)
    const payload = JSON.parse(requests('put', `/patient/admissions/${admittedId}/transfer`)[0].data)
    fireEvent.click(await form.findByRole('button', { name: 'OK' }))
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '转科/转床' })).not.toBeInTheDocument())
    expect(requests('put', `/patient/admissions/${admittedId}/transfer`)).toHaveLength(2)
    expect(JSON.parse(requests('put', `/patient/admissions/${admittedId}/transfer`)[1].data)).toEqual(payload)
  }, 20_000)

  it('requires a discharge summary and archives the admitted patient after success', async () => {
    await renderPage()
    fireEvent.click(row('ZY-ADMITTED').getByRole('button', { name: /^出\s*院$/ }))
    const form = await dialog('办理出院并归档')
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    await waitFor(() => expect(form.getByLabelText('出院小结')).toHaveAttribute('aria-invalid', 'true'))
    expect(requests('put', `/patient/admissions/${admittedId}/discharge`)).toHaveLength(0)
    fireEvent.change(form.getByLabelText('出院小结'), { target: { value: '病情稳定，门诊随访。' } })
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    await waitFor(() => expect(requests('put', `/patient/admissions/${admittedId}/discharge`)).toHaveLength(1))
    expect(JSON.parse(requests('put', `/patient/admissions/${admittedId}/discharge`)[0].data)).toEqual({ dischargeType: 'NORMAL', dischargeSummary: '病情稳定，门诊随访。' })
    await waitFor(() => expect(row('ZY-ADMITTED').getByText('已归档只读')).toBeVisible())
    expect(row('ZY-ADMITTED').queryAllByRole('button')).toHaveLength(0)
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '办理出院并归档' })).not.toBeInTheDocument())
  }, 20_000)

  it('reports admission failure without changing the PLANNED record', async () => {
    failures.set(`put /patient/admissions/${plannedId}/admit`, 409)
    await renderPage()
    fireEvent.click(row('ZY-PLANNED').getByRole('button', { name: '办理入院' }))
    expect(await screen.findByText('办理入院失败，请检查患者状态和服务连接。')).toBeVisible()
    expect(row('ZY-PLANNED').getByText('待入院')).toBeVisible()
    expect(row('ZY-PLANNED').getByRole('button', { name: '办理入院' })).toBeEnabled()
    expect(requests('get', '/patient/admissions')).toHaveLength(1)
  })

  it('keeps the discharge summary when the backend rejects archiving', async () => {
    failures.set(`put /patient/admissions/${admittedId}/discharge`, 409)
    await renderPage()
    fireEvent.click(row('ZY-ADMITTED').getByRole('button', { name: /^出\s*院$/ }))
    const form = await dialog('办理出院并归档')
    fireEvent.change(form.getByLabelText('出院小结'), { target: { value: '出院状态待核实' } })
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('出院归档失败，请检查患者状态和出院小结。')).toBeVisible()
    expect(form.getByLabelText('出院小结')).toHaveValue('出院状态待核实')
    expect(screen.getByRole('dialog', { name: '办理出院并归档' })).toBeVisible()
    expect(row('ZY-ADMITTED').getByText('在院')).toBeVisible()
  }, 20_000)
})

describe('InpatientEmergencyPage emergency workflows', () => {
  it('validates triage fields, submits the signed-in nurse and refreshes the queue', async () => {
    await renderPage()
    const form = await openTriage()
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    await waitFor(() => {
      for (const label of ['患者编号', '分诊级别', '主诉']) expect(form.getByLabelText(label)).toHaveAttribute('aria-invalid', 'true')
    })
    expect(requests('post', '/emergency/triage')).toHaveLength(0)
    await fillTriage(form)
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('FZ-NEW')).toBeVisible()
    expect(JSON.parse(requests('post', '/emergency/triage')[0].data)).toEqual({
      patientId: '1001', triageLevel: 2, chiefComplaint: '胸痛两小时', vitalSigns: '血压 120/80，血氧 98%', triageNurseId: 8, triageNurseName: '王护士',
    })
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '新增急诊分诊' })).not.toBeInTheDocument())
    expect(requests('get', '/emergency/triage')).toHaveLength(2)
  }, 20_000)

  it('preserves a 19-digit patient ID entered in the triage form', async () => {
    await renderPage()
    const form = await openTriage()
    await fillTriage(form, patientId)
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    await waitFor(() => expect(requests('post', '/emergency/triage')).toHaveLength(1))
    expect(JSON.parse(requests('post', '/emergency/triage')[0].data).patientId).toBe(patientId)
  }, 20_000)

  it('rejects non-positive, decimal and scientific-notation patient identifiers', async () => {
    await renderPage()
    const form = await openTriage()
    await fillTriage(form)
    for (const value of ['0', '-1', '1.5', '1e3', 'abc']) {
      fireEvent.change(form.getByLabelText('患者编号'), { target: { value } })
      fireEvent.click(form.getByRole('button', { name: 'OK' }))
      expect(await form.findByText('请输入有效的数字患者编号')).toBeVisible()
      expect(requests('post', '/emergency/triage')).toHaveLength(0)
    }
  }, 20_000)

  it('keeps triage inputs after a backend error and permits retry', async () => {
    failures.set('post /emergency/triage', 400)
    await renderPage()
    const form = await openTriage()
    await fillTriage(form)
    fireEvent.click(form.getByRole('button', { name: 'OK' }))
    expect(await screen.findByText('分诊登记失败，请检查患者编号、分级和主诉。')).toBeVisible()
    expect(form.getByLabelText('患者编号')).toHaveValue('1001')
    expect(form.getByLabelText('主诉')).toHaveValue('胸痛两小时')
    expect(form.getByLabelText('生命体征')).toHaveValue('血压 120/80，血氧 98%')
    expect(form.getByText('Ⅱ级 危重')).toBeVisible()
    expect(screen.getByRole('dialog', { name: '新增急诊分诊' })).toBeVisible()
    expect(requests('get', '/emergency/triage')).toHaveLength(1)
    const payload = JSON.parse(requests('post', '/emergency/triage')[0].data)
    fireEvent.click(await form.findByRole('button', { name: 'OK' }))
    expect(await screen.findByText('FZ-NEW')).toBeVisible()
    expect(requests('post', '/emergency/triage')).toHaveLength(2)
    expect(JSON.parse(requests('post', '/emergency/triage')[1].data)).toEqual(payload)
  }, 20_000)

  it('advances waiting triage with the unchanged ID and keeps completed/cancelled records read-only', async () => {
    emergencyRows = [emergency(), emergency('2099735874343563274', 'COMPLETED'), emergency('2099735874343563275', 'CANCELLED')]
    await renderPage()
    fireEvent.click(screen.getByRole('tab', { name: '急诊队列' }))
    await screen.findByText(`FZ-${triageId}`)
    expect(row('FZ-2099735874343563274').queryAllByRole('button')).toHaveLength(0)
    expect(row('FZ-2099735874343563275').queryAllByRole('button')).toHaveLength(0)
    expect(row(`FZ-${triageId}`).queryByRole('button', { name: '转留观' })).not.toBeInTheDocument()
    fireEvent.click(row(`FZ-${triageId}`).getByRole('button', { name: /^接\s*诊$/ }))
    await waitFor(() => expect(row(`FZ-${triageId}`).getByRole('button', { name: '转留观' })).toBeVisible())
    expect(requests('put', `/emergency/triage/${triageId}/status`)[0].params).toEqual({ status: 'IN_TREATMENT' })
    expect(row(`FZ-${triageId}`).queryByRole('button', { name: /^接\s*诊$/ })).not.toBeInTheDocument()
    fireEvent.click(row(`FZ-${triageId}`).getByRole('button', { name: '转留观' }))
    await waitFor(() => expect(row(`FZ-${triageId}`).getByText('OBSERVATION')).toBeVisible())
    expect(requests('put', `/emergency/triage/${triageId}/status`)[1].params).toEqual({ status: 'OBSERVATION' })
    fireEvent.click(row(`FZ-${triageId}`).getByRole('button', { name: /^完\s*成$/ }))
    await waitFor(() => expect(row(`FZ-${triageId}`).queryAllByRole('button')).toHaveLength(0))
    expect(requests('put', `/emergency/triage/${triageId}/status`)[2].params).toEqual({ status: 'COMPLETED' })
  }, 20_000)

  it('reports a rejected emergency status transition without changing the queue row', async () => {
    failures.set(`put /emergency/triage/${triageId}/status`, 409)
    await renderPage()
    fireEvent.click(screen.getByRole('tab', { name: '急诊队列' }))
    fireEvent.click(await screen.findByRole('button', { name: /^接\s*诊$/ }))
    expect(await screen.findByText('急诊状态更新失败，已完成或取消记录不能重新开启。')).toBeVisible()
    expect(row(`FZ-${triageId}`).getByText('WAITING')).toBeVisible()
    expect(requests('get', '/emergency/triage')).toHaveLength(1)
  }, 20_000)

  it('distinguishes unavailable inpatient and emergency services from empty queues', async () => {
    failures.set('get /patient/admissions', 503)
    failures.set('get /emergency/triage', 503)
    client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<ConfigProvider theme={{ token: { motion: false } }}><QueryClientProvider client={client}><InpatientEmergencyPage /></QueryClientProvider></ConfigProvider>)
    expect(await screen.findByText('住院服务连接失败')).toBeVisible()
    expect(screen.queryByText('暂无住院记录')).not.toBeInTheDocument()
    fireEvent.click(screen.getByRole('tab', { name: '急诊队列' }))
    expect(await screen.findByText('急诊服务连接失败')).toBeVisible()
    expect(screen.queryByText('当前无候诊患者')).not.toBeInTheDocument()
  })
})
