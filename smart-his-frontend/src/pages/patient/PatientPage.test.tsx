import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { patientApi } from '@/services/patientApi'
import type { Patient } from '@/types/patient'
import { PatientPage } from './PatientPage'

vi.mock('@/services/patientApi', () => ({ patientApi: { search: vi.fn(), create: vi.fn(), update: vi.fn() } }))

const patient: Patient = { id: '2099000000000000001', empiNo: 'EMPI0001', name: '测试患者', gender: 1, idType: 'ID_CARD', idNo: '110101199001011234', phone: '13800138000', patientStatus: 'ACTIVE' }
let client: QueryClient
function renderPage() {
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  render(<QueryClientProvider client={client}><PatientPage /></QueryClientProvider>)
}

beforeEach(() => {
  vi.resetAllMocks()
  vi.mocked(patientApi.search).mockResolvedValue({ records: [patient], total: 1, page: 1, size: 20, totalPages: 1 })
  vi.mocked(patientApi.create).mockResolvedValue(patient)
  vi.mocked(patientApi.update).mockResolvedValue(patient)
})
afterEach(() => { cleanup(); client?.clear() })

describe('PatientPage', () => {
  it.each([patient.id, patient.empiNo, patient.name, patient.idNo, patient.phone])('searches by %s using the unified endpoint', async keyword => {
    renderPage()
    await screen.findByText('EMPI0001', {}, { timeout: 5000 })
    fireEvent.change(screen.getByPlaceholderText('搜索系统 ID、EMPI、姓名、证件号或手机号'), { target: { value: `  ${keyword}  ` } })
    fireEvent.click(screen.getByRole('button', { name: /查\s*询/ }))
    await waitFor(() => expect(patientApi.search).toHaveBeenLastCalledWith({ page: 1, size: 20, keyword }), { timeout: 5000 })
    expect(await screen.findByText('测试患者', {}, { timeout: 5000 })).toBeInTheDocument()
    expect(screen.queryByText(patient.idNo)).not.toBeInTheDocument()
    expect(screen.queryByText(patient.phone)).not.toBeInTheDocument()
  }, 15000)

  it('distinguishes no matches from a service error', async () => {
    vi.mocked(patientApi.search).mockResolvedValueOnce({ records: [], total: 0, page: 1, size: 20, totalPages: 0 })
    renderPage()
    expect(await screen.findByText('未找到符合条件的患者', {}, { timeout: 5000 })).toBeInTheDocument()
    vi.mocked(patientApi.search).mockRejectedValueOnce(new Error('offline'))
    fireEvent.change(screen.getByPlaceholderText('搜索系统 ID、EMPI、姓名、证件号或手机号'), { target: { value: '不存在' } })
    fireEvent.click(screen.getByRole('button', { name: /查\s*询/ }))
    expect(await screen.findByText('患者列表加载失败', {}, { timeout: 5000 })).toBeInTheDocument()
  }, 15000)

  it('validates required fields and creates a patient without changing the document number', async () => {
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: /患者建档/ }))
    const dialog = await screen.findByRole('dialog')
    fireEvent.click(within(dialog).getByRole('button', { name: '确认建档' }))
    expect(await within(dialog).findByText('请输入患者姓名')).toBeInTheDocument()
    expect(patientApi.create).not.toHaveBeenCalled()
    fireEvent.change(within(dialog).getByLabelText('姓名'), { target: { value: patient.name } })
    fireEvent.change(within(dialog).getByLabelText('证件号码'), { target: { value: patient.idNo } })
    fireEvent.change(within(dialog).getByLabelText('手机号'), { target: { value: patient.phone } })
    fireEvent.click(within(dialog).getByRole('button', { name: '确认建档' }))
    await waitFor(() => expect(patientApi.create).toHaveBeenCalledWith(expect.objectContaining({ name: patient.name, idNo: patient.idNo, phone: patient.phone }), expect.anything()), { timeout: 5000 })
    expect(await screen.findByText('患者 测试患者 建档成功', {}, { timeout: 5000 })).toBeInTheDocument()
  }, 15000)

  it('shows a duplicate creation failure and leaves the form open for correction', async () => {
    vi.mocked(patientApi.create).mockRejectedValueOnce(new Error('duplicate'))
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: /患者建档/ }))
    const dialog = await screen.findByRole('dialog')
    fireEvent.change(within(dialog).getByLabelText('姓名'), { target: { value: patient.name } })
    fireEvent.change(within(dialog).getByLabelText('证件号码'), { target: { value: patient.idNo } })
    fireEvent.change(within(dialog).getByLabelText('手机号'), { target: { value: '123' } })
    fireEvent.click(within(dialog).getByRole('button', { name: '确认建档' }))
    expect(await within(dialog).findByText('请输入有效的手机号')).toBeInTheDocument()
    expect(patientApi.create).not.toHaveBeenCalled()
    fireEvent.change(within(dialog).getByLabelText('手机号'), { target: { value: patient.phone } })
    fireEvent.click(within(dialog).getByRole('button', { name: '确认建档' }))
    expect(await screen.findByText('患者建档失败，请检查证件是否重复或服务是否可用。', {}, { timeout: 5000 })).toBeInTheDocument()
    expect(within(dialog).getByLabelText('证件号码')).toHaveValue(patient.idNo)
  }, 15000)

  it('preserves the long patient ID when editing without submitting immutable identity fields', async () => {
    renderPage()
    fireEvent.click(await screen.findByRole('button', { name: /详\s*情/ }, { timeout: 5000 }))
    expect(await screen.findByText(patient.id)).toBeInTheDocument()
    fireEvent.click(await screen.findByRole('button', { name: /编辑资料/ }, { timeout: 5000 }))
    const title = await screen.findByText('编辑患者资料', {}, { timeout: 5000 })
    const dialog = title.closest('[role="dialog"]') as HTMLElement
    fireEvent.change(within(dialog).getByLabelText('手机号'), { target: { value: '13900139000' } })
    fireEvent.click(within(dialog).getByRole('button', { name: '保存修改' }))
    await waitFor(() => expect(patientApi.update).toHaveBeenCalledWith(patient.id, expect.objectContaining({ phone: '13900139000' })), { timeout: 5000 })
    const request = vi.mocked(patientApi.update).mock.calls[0][1]
    expect(request).not.toHaveProperty('idNo')
    expect(request).not.toHaveProperty('idType')
  }, 15000)
})
