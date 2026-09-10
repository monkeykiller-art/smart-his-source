import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { registrationApi } from './registrationApi'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn(), put: vi.fn() } }))

describe('registrationApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('loads departments and filters doctors with backend contracts', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { code: 200, message: 'success', data: [] } })
    await registrationApi.listDepartments()
    await registrationApi.listDoctors(8)
    expect(http.get).toHaveBeenNthCalledWith(1, '/patient/departments')
    expect(http.get).toHaveBeenNthCalledWith(2, '/patient/doctors', { params: { deptId: 8 } })
  })

  it('queries schedules and registrations with their pagination filters', async () => {
    const page = { records: [], total: 0, page: 1, size: 20, totalPages: 0 }
    vi.mocked(http.get).mockResolvedValue({ data: { code: 200, message: 'success', data: page } })
    await registrationApi.querySchedules({ page: 1, size: 50, deptId: 8, scheduleDate: '2026-09-10' })
    await registrationApi.queryRegistrations({ page: 2, size: 20, regStatus: 'ACTIVE' })
    expect(http.get).toHaveBeenNthCalledWith(1, '/patient/schedules', { params: { page: 1, size: 50, deptId: 8, scheduleDate: '2026-09-10' } })
    expect(http.get).toHaveBeenNthCalledWith(2, '/patient/registrations', { params: { page: 2, size: 20, regStatus: 'ACTIVE' } })
  })

  it('creates and cancels registrations using the required parameters', async () => {
    const registration = { id: 12, regNo: 'R001' }
    vi.mocked(http.post).mockResolvedValue({ data: { code: 200, message: 'success', data: registration } })
    vi.mocked(http.put).mockResolvedValue({ data: { code: 200, message: 'success', data: registration } })
    await registrationApi.create({ patientId: 2, scheduleId: 6, regSource: 'WINDOW' })
    await registrationApi.cancel(12, '患者取消')
    expect(http.post).toHaveBeenCalledWith('/patient/registrations', { patientId: 2, scheduleId: 6, regSource: 'WINDOW' })
    expect(http.put).toHaveBeenCalledWith('/patient/registrations/12/cancel', undefined, { params: { reason: '患者取消' } })
  })
})
