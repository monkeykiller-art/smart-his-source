import { http } from './http'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Patient, PatientCreateRequest, PatientQuery, PatientUpdateRequest } from '@/types/patient'

export const patientApi = {
  async search(query: PatientQuery) {
    const response = await http.get<ApiResponse<PageResult<Patient>>>('/patient/patients/search', { params: query })
    return response.data.data
  },
  async query(query: PatientQuery) {
    const response = await http.get<ApiResponse<PageResult<Patient>>>('/patient/patients', { params: query })
    return response.data.data
  },
  async getById(id: Patient['id']) {
    const response = await http.get<ApiResponse<Patient>>(`/patient/patients/${id}`)
    return response.data.data
  },
  async create(request: PatientCreateRequest) {
    const response = await http.post<ApiResponse<Patient>>('/patient/patients', request)
    return response.data.data
  },
  async update(id: Patient['id'], request: PatientUpdateRequest) {
    const response = await http.put<ApiResponse<Patient>>(`/patient/patients/${id}`, request)
    return response.data.data
  },
}
