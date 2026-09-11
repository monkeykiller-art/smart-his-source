import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { ClinicalOrder, ClinicalOrderCreateRequest, Diagnosis, DiagnosisCreateRequest, MedicalRecord, MedicalRecordCreateRequest } from '@/types/clinical'

export const clinicalApi = {
  async listRecords(patientId: number) {
    const response = await http.get<ApiResponse<MedicalRecord[]>>(`/clinical/records/patient/${patientId}`)
    return response.data.data
  },
  async createRecord(request: MedicalRecordCreateRequest) {
    const response = await http.post<ApiResponse<MedicalRecord>>('/clinical/records', request)
    return response.data.data
  },
  async signRecord(id: number) {
    await http.put<ApiResponse<void>>(`/clinical/records/${id}/sign`)
  },
  async listDiagnoses(encounterId: number) {
    const response = await http.get<ApiResponse<Diagnosis[]>>(`/clinical/diagnoses/encounter/${encounterId}`)
    return response.data.data
  },
  async createDiagnosis(request: DiagnosisCreateRequest) {
    const response = await http.post<ApiResponse<Diagnosis>>('/clinical/diagnoses', request)
    return response.data.data
  },
  async deleteDiagnosis(id: number) {
    await http.delete<ApiResponse<void>>(`/clinical/diagnoses/${id}`)
  },
  async listOrders(patientId: number) {
    const response = await http.get<ApiResponse<ClinicalOrder[]>>(`/clinical/orders/patient/${patientId}`)
    return response.data.data
  },
  async createOrder(request: ClinicalOrderCreateRequest) {
    const response = await http.post<ApiResponse<ClinicalOrder>>('/clinical/orders', request)
    return response.data.data
  },
  async cancelOrder(id: number, reason: string) {
    await http.put<ApiResponse<void>>(`/clinical/orders/${id}/cancel`, { reason })
  },
}
