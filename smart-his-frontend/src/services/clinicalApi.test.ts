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
    await clinicalApi.listRecordsByEncounter(34)
    expect(http.get).toHaveBeenNthCalledWith(1, '/clinical/records/patient/12')
    expect(http.get).toHaveBeenNthCalledWith(2, '/clinical/diagnoses/encounter/34')
    expect(http.get).toHaveBeenNthCalledWith(3, '/clinical/orders/patient/12')
    expect(http.get).toHaveBeenNthCalledWith(4, '/clinical/records/encounter/34')
  })

  it('creates, signs, and deletes clinical documents with backend contracts', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { code: 200, message: 'success', data: { id: 1 } } })
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, message: 'success' } })
    vi.mocked(http.delete).mockResolvedValue({ data: { code: 200, message: 'success' } })
    const record = { patientId: 12, encounterId: 34, deptId: 2, doctorId: 8, recordType: 'OUTPATIENT' }
    const diagnosis = { patientId: 12, encounterId: 34, doctorId: 8, diagnosisName: '上呼吸道感染', diagnosisType: 'WESTERN', isPrimary: 1, isConfirmed: 1 }
    await clinicalApi.createRecord(record)
    await clinicalApi.signRecord(7)
    await clinicalApi.updateRecord(7, { chiefComplaint: '发热三天', physicalExam: '体温 38.2℃' })
    await clinicalApi.createDiagnosis(diagnosis)
    await clinicalApi.deleteDiagnosis(9)
    expect(http.post).toHaveBeenNthCalledWith(1, '/clinical/records', record)
    expect(http.put).toHaveBeenCalledWith('/clinical/records/7/sign')
    expect(http.put).toHaveBeenCalledWith('/clinical/records/7', { chiefComplaint: '发热三天', physicalExam: '体温 38.2℃' })
    expect(http.post).toHaveBeenNthCalledWith(2, '/clinical/diagnoses', diagnosis)
    expect(http.delete).toHaveBeenCalledWith('/clinical/diagnoses/9')
  })

  it('searches enabled ICD-10 entries', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { code: 200, message: 'success', data: { records: [{ id: 6, icdCode: 'J06.9', icdName: '急性上呼吸道感染' }] } } })
    await expect(clinicalApi.searchIcd10('上呼吸道')).resolves.toHaveLength(1)
    expect(http.get).toHaveBeenCalledWith('/clinical/icd10/search', { params: { page: 1, size: 20, keyword: '上呼吸道', dictStatus: 1 } })
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

  it('submits an order and returns its linked bill', async () => {
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, data: { id: 3, billId: 91, orderStatus: 'SUBMITTED' } } })
    await expect(clinicalApi.submitOrder(3)).resolves.toMatchObject({ billId: 91, orderStatus: 'SUBMITTED' })
    expect(http.put).toHaveBeenCalledWith('/clinical/orders/3/submit')
  })

  it('creates and lists exam requests', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { code: 200, data: [] } })
  vi.mocked(http.post).mockResolvedValue({ data: { code: 200, data: { id: 4 } } })
  vi.mocked(http.put).mockResolvedValue({ data: { code: 200, data: { id: 4 } } })
    await clinicalApi.listExamRequests(12)
  await clinicalApi.createExamRequest({ patientId: 12, encounterId: 34, deptId: 2, doctorId: 8, requestType: 'LAB', items: [{ itemName: '血常规', itemType: 'LAB' }] })
  await clinicalApi.updateExamRequestStatus(4, 'ACCEPTED')
  await clinicalApi.reportExamRequest(4, '结果正常', 'R001')
    expect(http.get).toHaveBeenCalledWith('/clinical/exam-requests/patient/12')
  expect(http.post).toHaveBeenCalledWith('/clinical/exam-requests', expect.objectContaining({ requestType: 'LAB' }))
  expect(http.put).toHaveBeenCalledWith('/clinical/exam-requests/4/status/ACCEPTED')
  expect(http.put).toHaveBeenCalledWith('/clinical/exam-requests/4/result', expect.objectContaining({ resultSummary: '结果正常', reportNo: 'R001' }))
})
})
