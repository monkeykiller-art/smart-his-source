import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ClinicalPage } from './ClinicalPage'
import { clinicalApi } from '@/services/clinicalApi'
import { patientApi } from '@/services/patientApi'
import { registrationApi } from '@/services/registrationApi'

vi.mock('@/services/clinicalApi', () => ({ clinicalApi: {
  listRecordsByEncounter: vi.fn(), listDiagnoses: vi.fn(), listOrders: vi.fn(), searchIcd10: vi.fn(),
  createRecord: vi.fn(), updateRecord: vi.fn(), signRecord: vi.fn(), createDiagnosis: vi.fn(), deleteDiagnosis: vi.fn(), createOrder: vi.fn(), cancelOrder: vi.fn(),
} }))
vi.mock('@/services/patientApi', () => ({ patientApi: { getById: vi.fn() } }))
vi.mock('@/services/registrationApi', () => ({ registrationApi: {
  queryRegistrations: vi.fn(), getEncounterByRegistration: vi.fn(), openEncounter: vi.fn(), closeEncounter: vi.fn(),
} }))
vi.mock('@/stores/authStore', () => ({
  useAuthStore: (selector: (state: { session: { userId: number; deptId: number } }) => unknown) => selector({ session: { userId: 8, deptId: 2 } }),
}))

describe('ClinicalPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(registrationApi.queryRegistrations).mockResolvedValue({
      records: [{ id: 12, regNo: 'MZ001', patientId: 5, patientName: '张三', scheduleId: 6, deptId: 2, deptName: '内科', doctorId: 8, doctorName: '李医生', visitSeq: 3, regDate: '2026-09-11', timePeriod: 'MORNING', regFee: 10, payStatus: 'PAID', regStatus: 'ACTIVE', encounterId: 31 }],
      total: 1, page: 1, size: 100, totalPages: 1,
    })
    vi.mocked(registrationApi.getEncounterByRegistration).mockResolvedValue({ id: 31, encounterNo: 'JZ001', patientId: 5, regId: 12, deptId: 2, doctorId: 8, encounterType: 'OUTPATIENT', encounterStatus: 'PLANNED', visitDate: '2026-09-11' })
    vi.mocked(patientApi.getById).mockResolvedValue({ id: 5, empiNo: 'EM001', name: '张三', gender: 1, idType: 'ID_CARD', idNo: '110101199001011234', phone: '13800138000', ageDisplay: '36岁', allergyHistory: '青霉素' })
    vi.mocked(clinicalApi.listRecordsByEncounter).mockResolvedValue([])
    vi.mocked(clinicalApi.listDiagnoses).mockResolvedValue([])
    vi.mocked(clinicalApi.listOrders).mockResolvedValue([])
  })

  it('shows patient safety information and locks clinical editing before the encounter starts', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}><ClinicalPage /></QueryClientProvider>)

    expect(await screen.findByText('青霉素')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /开始接诊/ })).toBeEnabled()
    expect(screen.getByRole('button', { name: /写病历/ })).toBeDisabled()
    expect(screen.getByText('当前患者尚未开始接诊')).toBeInTheDocument()
  })
})
