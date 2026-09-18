import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { ConfigProvider } from 'antd'
import { AxiosError, type AxiosAdapter, type InternalAxiosRequestConfig } from 'axios'
import dayjs from 'dayjs'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from '@/services/http'
import { RegistrationPage } from './RegistrationPage'

const { searchPatients } = vi.hoisted(() => ({ searchPatients: vi.fn() }))
// The registration picker must use the unified search contract, not the old list endpoint.
vi.mock('@/services/patientApi', () => ({ patientApi: { search: searchPatients } }))

const patientId = '2099735874343563265'
const otherPatientId = '2099735874343563266'
const scheduleId = '2099735874343563267'
const deptId = '2099735874343563268'
const doctorId = '2099735874343563269'
const otherDeptId = '2099735874343563270'
const registrationId = '2099735874343563271'
const schedule = {
  id: scheduleId, deptId, deptName: '心内科', doctorId, doctorName: '王医生',
  scheduleDate: dayjs().format('YYYY-MM-DD'), timePeriod: 'MORNING', startTime: '08:00:00',
  totalQuota: 10, usedQuota: 8, availableQuota: 2, regFee: 12, regLevel: '普通号', scheduleStatus: 'ACTIVE',
}
const registration = {
  id: registrationId, regNo: 'GH20260918001', patientId, patientName: '张三', scheduleId,
  deptId, deptName: '心内科', doctorId, doctorName: '王医生', visitSeq: 9,
  regDate: schedule.scheduleDate, timePeriod: 'MORNING', regFee: 12, payStatus: 'UNPAID', regStatus: 'ACTIVE',
}
const patients = [
  { id: patientId, empiNo: 'EM0001', name: '张三', gender: 1, idType: 'ID_CARD', idNo: '110101199001010011', phone: '13800000001' },
  { id: otherPatientId, empiNo: 'EM0002', name: '李四', gender: 1, idType: 'ID_CARD', idNo: '110101199001010020', phone: '13800000002' },
]
const originalAdapter = http.defaults.adapter
const transport = vi.fn<AxiosAdapter>()
const failures = new Map<string, number>()
let client: QueryClient
let scheduleRows: typeof schedule[]
let registrationRows: typeof registration[]

function page<T>(records: T[], size = 20) {
  return { records, total: records.length, page: 1, size, totalPages: records.length ? 1 : 0 }
}

function response(config: InternalAxiosRequestConfig, data: unknown) {
  return { config, status: 200, statusText: 'OK', headers: {}, data: JSON.stringify({ code: 200, message: '成功', data }) }
}

function requests(method: string, url: string) {
  return transport.mock.calls.map(([config]) => config).filter(config => config.method === method && config.url === url)
}

function renderPage() {
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  render(<ConfigProvider theme={{ token: { motion: false } }}><QueryClientProvider client={client}><RegistrationPage /></QueryClientProvider></ConfigProvider>)
}

async function openCreate() {
  await waitFor(() => expect(screen.getByRole('button', { name: /新建挂号/ })).toBeEnabled())
  fireEvent.click(screen.getByRole('button', { name: /新建挂号/ }))
  const dialog = await screen.findByRole('dialog', { name: '新建门诊挂号' })
  await waitFor(() => expect(dialog).toBeVisible())
  return within(dialog)
}

async function choose(input: HTMLElement, label: string) {
  fireEvent.mouseDown(input)
  fireEvent.click(await screen.findByText(label, { selector: '.ant-select-item-option-content' }))
}

function filter(placeholder: string) {
  return within(screen.getByText(placeholder).closest('.ant-select') as HTMLElement).getByRole('combobox')
}

async function fillRegistration(dialog: ReturnType<typeof within>) {
  await choose(dialog.getByLabelText('患者'), '张三 · EM0001')
  await choose(dialog.getByLabelText('排班号源'), '心内科 · 王医生 · 上午 · 余 2 号 · ¥12.00')
}

beforeEach(() => {
  failures.clear()
  transport.mockReset()
  searchPatients.mockReset().mockResolvedValue(page(patients, 50))
  scheduleRows = [{ ...schedule }]
  registrationRows = []
  // Keep real registrationApi, Axios serialization, React Query and Ant Design controls.
  transport.mockImplementation(async config => {
    const key = `${config.method} ${config.url}`
    if (failures.has(key)) {
      const status = failures.get(key)!
      failures.delete(key)
      throw new AxiosError('Request failed', AxiosError.ERR_BAD_RESPONSE, config, undefined, {
        config, status, statusText: 'Error', headers: {}, data: { code: status, message: '该患者在此时段已有挂号', data: null },
      })
    }
    if (key === 'get /patient/departments') return response(config, [
      { id: deptId, deptCode: 'CARD', deptName: '心内科' },
      { id: otherDeptId, deptCode: 'SURG', deptName: '外科' },
    ])
    if (key === 'get /patient/doctors') return response(config, config.params.deptId === otherDeptId ? [] : [
      { id: doctorId, employeeNo: 'D001', doctorName: '王医生', deptId, deptName: '心内科', title: '主任医师' },
    ])
    if (key === 'get /patient/schedules') return response(config, page(scheduleRows, 100))
    if (key === 'get /patient/registrations') return response(config, page(registrationRows))
    if (key === 'post /patient/registrations') {
      const values = JSON.parse(config.data)
      const created = { ...registration, ...values }
      registrationRows = [created]
      scheduleRows = [{ ...schedule, availableQuota: 1, usedQuota: 9 }]
      return response(config, created)
    }
    throw new Error(`Unexpected request: ${key}`)
  })
  http.defaults.adapter = transport
})

afterEach(() => {
  cleanup()
  client?.clear()
  http.defaults.adapter = originalAdapter
})

describe('RegistrationPage', () => {
  it('filters schedules and registrations by department and doctor, resetting the doctor on department change', async () => {
    renderPage()
    await screen.findByText('王医生')
    expect(searchPatients).not.toHaveBeenCalled()
    expect(requests('get', '/patient/schedules')[0].params).toMatchObject({ page: 1, size: 100, scheduleDate: schedule.scheduleDate })

    await choose(filter('全部科室'), '心内科')
    await waitFor(() => expect(requests('get', '/patient/doctors').at(-1)?.params).toEqual({ deptId }))
    await choose(filter('全部医生'), '王医生 · 主任医师')
    await waitFor(() => {
      expect(requests('get', '/patient/schedules').at(-1)?.params).toMatchObject({ deptId, doctorId })
      expect(requests('get', '/patient/registrations').at(-1)?.params).toMatchObject({ page: 1, deptId, doctorId })
    })

    fireEvent.mouseDown(screen.getAllByRole('combobox')[0])
    fireEvent.click(await screen.findByText('外科'))
    await waitFor(() => {
      expect(requests('get', '/patient/doctors').at(-1)?.params).toEqual({ deptId: otherDeptId })
      expect(requests('get', '/patient/schedules').at(-1)?.params).toMatchObject({ deptId: otherDeptId, doctorId: undefined })
      expect(requests('get', '/patient/registrations').at(-1)?.params).toMatchObject({ deptId: otherDeptId, doctorId: undefined })
    })
    expect(screen.getByText('全部医生')).toBeVisible()
  }, 20_000)

  it('requires patient and schedule, searches patients and submits distinct 19-digit IDs without rounding', async () => {
    renderPage()
    const dialog = await openCreate()
    fireEvent.click(dialog.getByRole('button', { name: '确认挂号' }))
    expect(await dialog.findByText('请选择患者')).toBeVisible()
    expect(await dialog.findByText('请选择排班号源')).toBeVisible()
    expect(requests('post', '/patient/registrations')).toHaveLength(0)
    expect(searchPatients).toHaveBeenCalledWith({ page: 1, size: 50, keyword: undefined })

    fireEvent.change(dialog.getByLabelText('患者'), { target: { value: ` ${otherPatientId} ` } })
    await waitFor(() => expect(searchPatients).toHaveBeenLastCalledWith({ page: 1, size: 50, keyword: otherPatientId }))
    await choose(dialog.getByLabelText('患者'), '李四 · EM0002')
    await choose(dialog.getByLabelText('排班号源'), '心内科 · 王医生 · 上午 · 余 2 号 · ¥12.00')
    fireEvent.click(dialog.getByRole('button', { name: '确认挂号' }))

    expect(await screen.findByText('挂号成功，挂号单号 GH20260918001')).toBeVisible()
    expect(JSON.parse(requests('post', '/patient/registrations')[0].data)).toEqual({ patientId: otherPatientId, scheduleId, regSource: 'WINDOW' })
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '新建门诊挂号' })).not.toBeInTheDocument())
    expect(await screen.findByText(registration.regNo)).toBeVisible()
    expect(requests('get', '/patient/schedules')).toHaveLength(2)
    expect(requests('get', '/patient/registrations')).toHaveLength(2)
  }, 20_000)

  it('preselects the clicked schedule and preserves its string ID when registering', async () => {
    renderPage()
    fireEvent.click(await screen.findByRole('button', { name: /^挂\s*号$/ }))
    const element = await screen.findByRole('dialog', { name: '新建门诊挂号' })
    await waitFor(() => expect(element).toBeVisible())
    const dialog = within(element)
    expect(dialog.getByText('心内科 · 王医生 · 上午 · 余 2 号 · ¥12.00')).toBeVisible()
    await choose(dialog.getByLabelText('患者'), '张三 · EM0001')
    fireEvent.click(dialog.getByRole('button', { name: '确认挂号' }))
    await screen.findByText('挂号成功，挂号单号 GH20260918001')
    expect(JSON.parse(requests('post', '/patient/registrations')[0].data)).toEqual({ patientId, scheduleId, regSource: 'WINDOW' })
  }, 20_000)

  it.each([
    ['没有排班', []],
    ['已满', [{ ...schedule, availableQuota: 0, usedQuota: 10 }]],
    ['已停诊', [{ ...schedule, scheduleStatus: 'STOPPED' }]],
  ])('disables registration when %s', async (_label, rows) => {
    scheduleRows = rows as typeof schedule[]
    renderPage()
    await screen.findByText(rows.length ? String(_label) : '当前筛选条件下没有排班')
    expect(screen.getByRole('button', { name: /新建挂号/ })).toBeDisabled()
    if (rows.length) expect(screen.getByRole('button', { name: /^挂\s*号$/ })).toBeDisabled()
    expect(searchPatients).not.toHaveBeenCalled()
    expect(requests('post', '/patient/registrations')).toHaveLength(0)
  })

  it.each([409, 403])('shows a failed registration for HTTP %s and keeps selections for retry', async status => {
    failures.set('post /patient/registrations', status)
    renderPage()
    const dialog = await openCreate()
    await fillRegistration(dialog)
    fireEvent.click(dialog.getByRole('button', { name: '确认挂号' }))
    // The current page intentionally exposes only its generic failure copy, not the backend message.
    expect(await screen.findByText('挂号失败，请检查号源余量、患者状态和服务连接。')).toBeVisible()
    expect(screen.getByRole('dialog', { name: '新建门诊挂号' })).toBeVisible()
    expect(dialog.getByText('张三 · EM0001')).toBeVisible()
    expect(dialog.getByText('心内科 · 王医生 · 上午 · 余 2 号 · ¥12.00')).toBeVisible()
    expect(screen.queryByText(/挂号成功，挂号单号/)).not.toBeInTheDocument()
    expect(requests('get', '/patient/registrations')).toHaveLength(1)
    const payload = JSON.parse(requests('post', '/patient/registrations')[0].data)
    // onError returns Ant Design's message thenable; the mutation stays pending until it closes.
    fireEvent.click(await dialog.findByRole('button', { name: '确认挂号' }, { timeout: 5_000 }))
    await screen.findByText('挂号成功，挂号单号 GH20260918001')
    expect(requests('post', '/patient/registrations')).toHaveLength(2)
    expect(JSON.parse(requests('post', '/patient/registrations')[1].data)).toEqual(payload)
  }, 20_000)

  it('reports schedule and registration loading failures and reloads both on refresh', async () => {
    failures.set('get /patient/schedules', 503)
    failures.set('get /patient/registrations', 503)
    renderPage()
    expect(await screen.findByText('排班加载失败')).toBeVisible()
    expect(await screen.findByText('挂号记录加载失败')).toBeVisible()
    expect(screen.getByRole('button', { name: /新建挂号/ })).toBeDisabled()
    fireEvent.click(screen.getByRole('button', { name: /刷\s*新/ }))
    expect(await screen.findByText('王医生')).toBeVisible()
    await waitFor(() => {
      expect(screen.queryByText('排班加载失败')).not.toBeInTheDocument()
      expect(screen.queryByText('挂号记录加载失败')).not.toBeInTheDocument()
    })
    expect(requests('get', '/patient/schedules')).toHaveLength(2)
    expect(requests('get', '/patient/registrations')).toHaveLength(2)
  })

  it('reports patient search failure without submitting an incomplete registration', async () => {
    searchPatients.mockRejectedValue(new Error('offline'))
    renderPage()
    const dialog = await openCreate()
    await waitFor(() => expect(searchPatients).toHaveBeenCalledOnce())
    fireEvent.mouseDown(dialog.getByLabelText('患者'))
    await waitFor(() => expect(screen.getByText('患者服务连接失败')).toBeVisible())
    fireEvent.click(dialog.getByRole('button', { name: '确认挂号' }))
    expect(await dialog.findByText('请选择患者')).toBeVisible()
    expect(requests('post', '/patient/registrations')).toHaveLength(0)
  }, 20_000)
})
