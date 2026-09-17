import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { m8Api } from './m8Api'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn(), put: vi.fn() } }))

describe('m8Api', () => {
  beforeEach(() => vi.clearAllMocks())

  it('queries admissions and submits transfer and discharge transitions', async () => {
    const page = { records: [], total: 0, page: 1, size: 20, totalPages: 0 }
    vi.mocked(http.get).mockResolvedValue({ data: { code: 200, message: 'success', data: page } })
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, message: 'success', data: { id: 1 } } })
    await expect(m8Api.admissions({ page: 1, size: 20 })).resolves.toEqual(page)
    await m8Api.transfer(1, { targetDeptId: 2, targetWardId: 3, targetBedId: 4, reason: '术后转科' })
    await m8Api.discharge(1, { dischargeType: 'NORMAL', dischargeSummary: '病情稳定' })
    expect(http.put).toHaveBeenNthCalledWith(1, '/patient/admissions/1/transfer', { targetDeptId: 2, targetWardId: 3, targetBedId: 4, reason: '术后转科' })
    expect(http.put).toHaveBeenNthCalledWith(2, '/patient/admissions/1/discharge', { dischargeType: 'NORMAL', dischargeSummary: '病情稳定' })
  })

  it('creates prioritized emergency triage and updates queue state', async () => {
    const triage = { id: 8, triageNo: 'JZ001', patientId: 1, triageLevel: 1, chiefComplaint: '胸痛', triageStatus: 'WAITING' }
    vi.mocked(http.post).mockResolvedValue({ data: { code: 200, message: 'success', data: triage } })
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, message: 'success', data: { ...triage, triageStatus: 'IN_TREATMENT' } } })
    await expect(m8Api.createTriage({ patientId: 1, triageLevel: 1, chiefComplaint: '胸痛', triageNurseId: 2 })).resolves.toEqual(triage)
    await m8Api.updateTriageStatus(8, 'IN_TREATMENT')
    expect(http.put).toHaveBeenCalledWith('/emergency/triage/8/status', undefined, { params: { status: 'IN_TREATMENT' } })
  })

  it('creates one inpatient billing account from an admission snapshot', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { code: 200, message: 'success', data: { id: 9, billNo: 'ZY001' } } })
    await m8Api.createAdmissionBill({ id: 8, patientId: 1, deptId: 2 })
    expect(http.post).toHaveBeenCalledWith('/operations/bills/admission', expect.objectContaining({ admissionId: 8, patientId: 1, deptId: 2, visitType: 'INPATIENT' }))
  })
})
