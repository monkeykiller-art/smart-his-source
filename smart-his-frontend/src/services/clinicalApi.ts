import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { ClinicalOrder, ClinicalOrderCreateRequest, Diagnosis, DiagnosisCreateRequest, ExamRequest, ExamRequestCreateRequest, Icd10Item, MedicalRecord, MedicalRecordCreateRequest, MedicalRecordUpdateRequest } from '@/types/clinical'

export const clinicalApi = {
  async listRecords(patientId: number) {
    const response = await http.get<ApiResponse<MedicalRecord[]>>(`/clinical/records/patient/${patientId}`)
    return response.data.data
  },
  async createRecord(request: MedicalRecordCreateRequest) {
    const response = await http.post<ApiResponse<MedicalRecord>>('/clinical/records', request)
    return response.data.data
  },
  async listRecordsByEncounter(encounterId: number) {
    const response = await http.get<ApiResponse<MedicalRecord[]>>(`/clinical/records/encounter/${encounterId}`)
    return response.data.data
  },
  async updateRecord(id: number, request: MedicalRecordUpdateRequest) {
    const response = await http.put<ApiResponse<MedicalRecord>>(`/clinical/records/${id}`, request)
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
  async searchIcd10(keyword: string) {
    const response = await http.get<ApiResponse<{ records: Icd10Item[] }>>('/clinical/icd10/search', { params: { page: 1, size: 20, keyword, dictStatus: 1 } })
    return response.data.data.records
  },
  async listExamRequests(patientId: number) {
    const response = await http.get<ApiResponse<ExamRequest[]>>(`/clinical/exam-requests/patient/${patientId}`)
    return response.data.data
  },
  async createExamRequest(request: ExamRequestCreateRequest) {
    const response = await http.post<ApiResponse<ExamRequest>>('/clinical/exam-requests', request)
    return response.data.data
  },
  async updateExamRequestStatus(id: number, status: string) {
    const response = await http.put<ApiResponse<ExamRequest>>(`/clinical/exam-requests/${id}/status/${status}`)
    return response.data.data
  },
  async reportExamRequest(id: number, resultSummary: string, reportNo?: string, reportUrl?: string, isCritical?: number) {
    const response = await http.put<ApiResponse<ExamRequest>>(`/clinical/exam-requests/${id}/result`, { resultSummary, reportNo, reportUrl, isCritical })
    return response.data.data
  },
  async acknowledgeCriticalExam(id: number, userId: number) {
    const response = await http.put<ApiResponse<ExamRequest>>(`/clinical/exam-requests/${id}/critical/acknowledge`, null, { params: { userId } })
    return response.data.data
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
  async submitOrder(id: number) {
    const response = await http.put<ApiResponse<ClinicalOrder>>(`/clinical/orders/${id}/submit`)
    return response.data.data
  },
}
