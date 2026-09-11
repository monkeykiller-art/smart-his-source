import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { patientApi } from './patientApi'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn(), put: vi.fn() } }))

const patient = {
  id: 1,
  empiNo: 'EMPI0001',
  name: '张三',
  gender: 1,
  idType: 'ID_CARD',
  idNo: '110101199001011234',
  phone: '13800138000',
  patientStatus: 'ACTIVE',
}

describe('patientApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('sends the backend pagination contract unchanged', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { code: 200, message: 'success', data: { records: [patient], total: 1, page: 2, size: 10, totalPages: 1 } } })
    const result = await patientApi.query({ page: 2, size: 10, keyword: '张三' })
    expect(http.get).toHaveBeenCalledWith('/patient/patients', { params: { page: 2, size: 10, keyword: '张三' } })
    expect(result.records).toEqual([patient])
  })

  it('posts the create request to the patient endpoint', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { code: 200, message: 'success', data: patient } })
    const request = { name: '张三', idType: 'ID_CARD', idNo: patient.idNo, phone: patient.phone }
    await expect(patientApi.create(request)).resolves.toEqual(patient)
    expect(http.post).toHaveBeenCalledWith('/patient/patients', request)
  })

  it('puts editable patient fields without changing the identity document', async () => {
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, message: 'success', data: { ...patient, phone: '13900139000', allergyHistory: '青霉素' } } })
    const request = { name: '张三', phone: '13900139000', allergyHistory: '青霉素' }
    await expect(patientApi.update(patient.id, request)).resolves.toMatchObject(request)
    expect(http.put).toHaveBeenCalledWith('/patient/patients/1', request)
    expect(request).not.toHaveProperty('idNo')
  })
})
