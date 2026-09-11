import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { clinicalApi } from './clinicalApi'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() } }))

describe('clinicalApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('loads the selected patient clinical workspace', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { code: 200, message: 'success', data: [] } })
    await clinicalApi.listRecords(12)
    await clinicalApi.listDiagnoses(34)
    await clinicalApi.listOrders(12)
    expect(http.get).toHaveBeenNthCalledWith(1, '/clinical/records/patient/12')
    expect(http.get).toHaveBeenNthCalledWith(2, '/clinical/diagnoses/encounter/34')
    expect(http.get).toHaveBeenNthCalledWith(3, '/clinical/orders/patient/12')
  })

  it('creates, signs, and deletes clinical documents with backend contracts', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { code: 200, message: 'success', data: { id: 1 } } })
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, message: 'success' } })
    vi.mocked(http.delete).mockResolvedValue({ data: { code: 200, message: 'success' } })
    const record = { patientId: 12, encounterId: 34, deptId: 2, doctorId: 8, recordType: 'OUTPATIENT' }
    const diagnosis = { patientId: 12, encounterId: 34, doctorId: 8, diagnosisName: '上呼吸道感染', diagnosisType: 'WESTERN', isPrimary: 1, isConfirmed: 1 }
    await clinicalApi.createRecord(record)
    await clinicalApi.signRecord(7)
    await clinicalApi.createDiagnosis(diagnosis)
    await clinicalApi.deleteDiagnosis(9)
    expect(http.post).toHaveBeenNthCalledWith(1, '/clinical/records', record)
    expect(http.put).toHaveBeenCalledWith('/clinical/records/7/sign')
    expect(http.post).toHaveBeenNthCalledWith(2, '/clinical/diagnoses', diagnosis)
    expect(http.delete).toHaveBeenCalledWith('/clinical/diagnoses/9')
  })

  it('creates and cancels an order', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { code: 200, message: 'success', data: { id: 3 } } })
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, message: 'success' } })
    const request = { patientId: 12, encounterId: 34, deptId: 2, doctorId: 8, orderType: 'MEDICINE', orderCategory: 'ROUTINE', isStat: 0, items: [{ itemName: '阿莫西林' }] }
    await clinicalApi.createOrder(request)
    await clinicalApi.cancelOrder(3, '医生撤销')
    expect(http.post).toHaveBeenCalledWith('/clinical/orders', request)
    expect(http.put).toHaveBeenCalledWith('/clinical/orders/3/cancel', { reason: '医生撤销' })
  })
})
