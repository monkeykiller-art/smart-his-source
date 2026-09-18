import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { ClinicalOrder, ClinicalOrderCreateRequest, CommonPhrase, CommonPhraseCreateRequest, CommonPhraseUpdateRequest, Diagnosis, DiagnosisCreateRequest, ExamRequest, ExamRequestCreateRequest, Icd10Item, MedicalRecord, MedicalRecordCreateRequest, MedicalRecordUpdateRequest, RecordTemplate, RecordTemplateCreateRequest, RecordTemplateUpdateRequest } from '@/types/clinical'

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
  async listCommonPhrases(phraseType?: string, deptId?: number, userId?: number, keyword?: string) {
    const params: Record<string, any> = { pageNum: 1, pageSize: 100 }
    if (phraseType) params.phraseType = phraseType
    if (deptId) params.deptId = deptId
    if (userId) params.userId = userId
    if (keyword) params.keyword = keyword
    const response = await http.get<ApiResponse<{ records: CommonPhrase[] }>>('/clinical/common-phrases', { params })
    return response.data.data.records
  },
  async createCommonPhrase(request: CommonPhraseCreateRequest) {
    const response = await http.post<ApiResponse<CommonPhrase>>('/clinical/common-phrases', request)
    return response.data.data
  },
  async updateCommonPhrase(id: number, request: CommonPhraseUpdateRequest) {
    const response = await http.put<ApiResponse<CommonPhrase>>(`/clinical/common-phrases/${id}`, request)
    return response.data.data
  },
  async deleteCommonPhrase(id: number) {
    await http.delete<ApiResponse<void>>(`/clinical/common-phrases/${id}`)
  },
  async listRecordTemplates(templateStatus?: number, deptId?: number, recordType?: string) {
    const params: Record<string, any> = { pageNum: 1, pageSize: 100 }
    if (templateStatus !== undefined) params.templateStatus = templateStatus
    if (deptId) params.deptId = deptId
    if (recordType) params.recordType = recordType
    const response = await http.get<ApiResponse<{ records: RecordTemplate[] }>>('/clinical/record-templates', { params })
    return response.data.data.records
  },
  async createRecordTemplate(request: RecordTemplateCreateRequest) {
    const response = await http.post<ApiResponse<RecordTemplate>>('/clinical/record-templates', request)
    return response.data.data
  },
  async updateRecordTemplate(id: number, request: RecordTemplateUpdateRequest) {
    const response = await http.put<ApiResponse<RecordTemplate>>(`/clinical/record-templates/${id}`, request)
    return response.data.data
  },
  async deleteRecordTemplate(id: number) {
    await http.delete<ApiResponse<void>>(`/clinical/record-templates/${id}`)
  },
}
