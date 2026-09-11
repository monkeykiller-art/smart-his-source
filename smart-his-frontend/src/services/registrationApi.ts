import { http } from './http'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Department, Doctor, Encounter, Registration, RegistrationCreateRequest, RegistrationQuery, Schedule, ScheduleQuery } from '@/types/registration'

export const registrationApi = {
  async listDepartments() {
    const response = await http.get<ApiResponse<Department[]>>('/patient/departments')
    return response.data.data
  },
  async listDoctors(deptId?: number) {
    const response = await http.get<ApiResponse<Doctor[]>>('/patient/doctors', { params: deptId ? { deptId } : {} })
    return response.data.data
  },
  async querySchedules(query: ScheduleQuery) {
    const response = await http.get<ApiResponse<PageResult<Schedule>>>('/patient/schedules', { params: query })
    return response.data.data
  },
  async queryRegistrations(query: RegistrationQuery) {
    const response = await http.get<ApiResponse<PageResult<Registration>>>('/patient/registrations', { params: query })
    return response.data.data
  },
  async create(request: RegistrationCreateRequest) {
    const response = await http.post<ApiResponse<Registration>>('/patient/registrations', request)
    return response.data.data
  },
  async cancel(id: number, reason: string) {
    await http.put<ApiResponse<void>>(`/patient/registrations/${id}/cancel`, undefined, { params: { reason } })
  },
  async markPaid(id: number, billId?: number) {
    await http.put<ApiResponse<void>>(`/patient/registrations/${id}/pay`, undefined, { params: billId ? { billId } : {} })
  },
  async refund(id: number, reason: string) {
    await http.put<ApiResponse<void>>(`/patient/registrations/${id}/refund`, undefined, { params: { reason } })
  },
  async getEncounterByRegistration(registrationId: number) {
    const response = await http.get<ApiResponse<Encounter>>(`/patient/encounters/by-reg/${registrationId}`)
    return response.data.data
  },
  async openEncounter(registrationId: number, chiefComplaint?: string) {
    const response = await http.post<ApiResponse<Encounter>>('/patient/encounters', { regId: registrationId, chiefComplaint })
    return response.data.data
  },
  async closeEncounter(encounterId: number) {
    await http.put<ApiResponse<void>>(`/patient/encounters/${encounterId}/close`)
  },
}
