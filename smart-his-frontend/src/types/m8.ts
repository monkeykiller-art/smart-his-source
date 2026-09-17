import type { PageResult } from './api'

export interface Admission {
  id: number; admissionNo: string; patientId: number; patientName?: string
  admissionStatus: 'PLANNED' | 'ADMITTED' | 'DISCHARGED' | 'CANCELLED'
  admissionType: string; deptId: number; wardId?: number; bedId?: number
  doctorId: number; admissionDate: string; expectedDischargeDate?: string
  preliminaryDiagnosis?: string; dischargeSummary?: string
}
export type AdmissionPage = PageResult<Admission>
export interface InpatientBed {
  id: number; deptId: number; wardId: number; wardName: string; bedNo: string
  bedType: string; bedStatus: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'; currentAdmissionId?: number
}
export interface SurgeryCase {
  id: number; surgeryNo: string; admissionId: number; patientId: number; surgeryName: string
  plannedStartTime: string; operatingRoom?: string; surgeonId: number; anesthetistId?: number
  anesthesiaMethod?: string; surgeryStatus: 'APPLIED' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  operativeNote?: string
}
export interface EmergencyTriage {
  id: number; triageNo: string; patientId: number; triageLevel: 1 | 2 | 3 | 4
  triageTime: string; chiefComplaint: string; vitalSigns?: string; targetDeptName?: string
  triageStatus: 'WAITING' | 'IN_TREATMENT' | 'OBSERVATION' | 'COMPLETED' | 'CANCELLED'
}
export interface ResuscitationRecord {
  id: number; triageId: number; patientId: number; startTime: string; endTime?: string
  procedures?: string; medications?: string; outcome?: string; outcomeSummary?: string
  resuscitationStatus: 'IN_PROGRESS' | 'COMPLETED'
}
export interface ObservationRecord {
  id: number; triageId: number; patientId: number; bedNo: string; admitTime: string; dischargeTime?: string
  diagnosis: string; treatmentPlan?: string; observationStatus: 'ADMITTED' | 'DISCHARGED'; dischargeSummary?: string
}
