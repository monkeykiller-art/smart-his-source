import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { Admission, AdmissionPage, EmergencyTriage, InpatientBed, ObservationRecord, ResuscitationRecord, SurgeryCase } from '@/types/m8'

export const m8Api = {
  async admissions(params: { page: number; size: number; admissionStatus?: string }): Promise<AdmissionPage> {
    const response = await http.get<ApiResponse<AdmissionPage>>('/patient/admissions', { params })
    return response.data.data
  },
  async admit(id: number): Promise<void> { await http.put(`/patient/admissions/${id}/admit`) },
  async beds(wardId?: number, status?: InpatientBed['bedStatus']): Promise<InpatientBed[]> {
    const response = await http.get<ApiResponse<InpatientBed[]>>('/patient/inpatient-beds', { params: { wardId, status } })
    return response.data.data
  },
  async transfer(id: number, request: { targetDeptId: number; targetWardId: number; targetBedId: number; reason: string }): Promise<Admission> {
    const response = await http.put<ApiResponse<Admission>>(`/patient/admissions/${id}/transfer`, request)
    return response.data.data
  },
  async discharge(id: number, request: { dischargeType: string; dischargeSummary: string; actualDischargeDate?: string }): Promise<void> {
    await http.put(`/patient/admissions/${id}/discharge`, request)
  },
  async createAdmissionBill(admission: Pick<Admission, 'id' | 'patientId' | 'deptId'>): Promise<{ id: number; billNo: string }> {
    const response = await http.post<ApiResponse<{ id: number; billNo: string }>>('/operations/bills/admission', { admissionId: admission.id, patientId: admission.patientId, deptId: admission.deptId, visitType: 'INPATIENT', billType: 'NORMAL' })
    return response.data.data
  },
  async surgeries(admissionId: number): Promise<SurgeryCase[]> {
    const response = await http.get<ApiResponse<SurgeryCase[]>>('/patient/surgeries', { params: { admissionId } })
    return response.data.data
  },
  async applySurgery(request: { admissionId: number; surgeryName: string; plannedStartTime: string; surgeonId: number }): Promise<SurgeryCase> {
    const response = await http.post<ApiResponse<SurgeryCase>>('/patient/surgeries', request)
    return response.data.data
  },
  async scheduleSurgery(id: number, request: { plannedStartTime: string; operatingRoom: string; anesthetistId: number; anesthesiaMethod: string }): Promise<SurgeryCase> {
    const response = await http.put<ApiResponse<SurgeryCase>>(`/patient/surgeries/${id}/schedule`, request)
    return response.data.data
  },
  async surgeryAction(id: number, action: 'start' | 'complete', body?: { operativeNote: string }): Promise<SurgeryCase> {
    const response = await http.put<ApiResponse<SurgeryCase>>(`/patient/surgeries/${id}/${action}`, body)
    return response.data.data
  },
  async emergencyQueue(): Promise<EmergencyTriage[]> {
    const response = await http.get<ApiResponse<EmergencyTriage[]>>('/emergency/triage')
    return response.data.data
  },
  async createTriage(request: { patientId: string | number; triageLevel: number; chiefComplaint: string; vitalSigns?: string; triageNurseId: number; triageNurseName?: string }): Promise<EmergencyTriage> {
    const response = await http.post<ApiResponse<EmergencyTriage>>('/emergency/triage', request)
    return response.data.data
  },
  async updateTriageStatus(id: number, status: EmergencyTriage['triageStatus']): Promise<EmergencyTriage> {
    const response = await http.put<ApiResponse<EmergencyTriage>>(`/emergency/triage/${id}/status`, undefined, { params: { status } })
    return response.data.data
  },
  async resuscitations(patientId?: number): Promise<ResuscitationRecord[]> {
    const response = await http.get<ApiResponse<ResuscitationRecord[]>>('/emergency/resuscitations', { params: { patientId } })
    return response.data.data
  },
  async startResuscitation(request: { triageId: number; patientId: number; procedures?: string; medications?: string }): Promise<ResuscitationRecord> {
    const response = await http.post<ApiResponse<ResuscitationRecord>>('/emergency/resuscitations', request)
    return response.data.data
  },
  async completeResuscitation(id: number, request: { outcome: string; outcomeSummary: string }): Promise<ResuscitationRecord> {
    const response = await http.put<ApiResponse<ResuscitationRecord>>(`/emergency/resuscitations/${id}/complete`, request)
    return response.data.data
  },
  async observations(patientId?: number): Promise<ObservationRecord[]> {
    const response = await http.get<ApiResponse<ObservationRecord[]>>('/emergency/observations', { params: { patientId } })
    return response.data.data
  },
  async admitObservation(request: { triageId: number; patientId: number; bedNo: string; diagnosis: string; treatmentPlan?: string }): Promise<ObservationRecord> {
    const response = await http.post<ApiResponse<ObservationRecord>>('/emergency/observations', request)
    return response.data.data
  },
  async dischargeObservation(id: number, dischargeSummary: string): Promise<ObservationRecord> {
    const response = await http.put<ApiResponse<ObservationRecord>>(`/emergency/observations/${id}/discharge`, { dischargeSummary })
    return response.data.data
  },
}
