import { http } from './http'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Patient, PatientCreateRequest, PatientQuery } from '@/types/patient'

export const patientApi = {
  async query(query: PatientQuery) {
    const response = await http.get<ApiResponse<PageResult<Patient>>>('/patient/patients', { params: query })
    return response.data.data
  },
  async getById(id: number) {
    const response = await http.get<ApiResponse<Patient>>(`/patient/patients/${id}`)
    return response.data.data
  },
  async create(request: PatientCreateRequest) {
    const response = await http.post<ApiResponse<Patient>>('/patient/patients', request)
    return response.data.data
  },
}
