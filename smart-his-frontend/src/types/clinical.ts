export interface MedicalRecord {
  id: number
  recordNo: string
  encounterId?: number
  patientId: number
  deptId: number
  doctorId: number
  recordType: string
  title?: string
  chiefComplaint?: string
  presentIllness?: string
  pastHistory?: string
  allergyHistory?: string
  physicalExam?: string
  auxiliaryExam?: string
  diagnosisDesc?: string
  treatmentPlan?: string
  recordContent?: string
  recordStatus: string
  signTime?: string
  createdTime?: string
  updatedTime?: string
}

export interface MedicalRecordUpdateRequest {
  title?: string
  chiefComplaint?: string
  presentIllness?: string
  pastHistory?: string
  allergyHistory?: string
  physicalExam?: string
  auxiliaryExam?: string
  diagnosisDesc?: string
  treatmentPlan?: string
  recordContent?: string
}

export interface MedicalRecordCreateRequest {
  patientId: number
  encounterId?: number
  deptId: number
  doctorId: number
  recordType: string
  title?: string
  chiefComplaint?: string
  presentIllness?: string
  pastHistory?: string
  allergyHistory?: string
  physicalExam?: string
  auxiliaryExam?: string
  diagnosisDesc?: string
  treatmentPlan?: string
  recordContent?: string
}

export interface Diagnosis {
  id: number
  encounterId?: number
  patientId: number
  doctorId: number
  icdCode?: string
  diagnosisName: string
  diagnosisType?: string
  isPrimary?: number
  isConfirmed?: number
  diagnosisDesc?: string
  diagnosisStatus?: string
  onsetDate?: string
}

export interface DiagnosisCreateRequest {
  patientId: number
  encounterId?: number
  doctorId: number
  icdCode?: string
  diagnosisName: string
  diagnosisType: string
  isPrimary: number
  isConfirmed: number
  diagnosisDesc?: string
  onsetDate?: string
  icd10Id?: number
}

export interface Icd10Item {
  id: number
  icdCode: string
  icdName: string
  namePinyin?: string
  chapter?: string
  category?: string
  isInfectious?: number
  isChronic?: number
  dictStatus?: number
}

export interface ClinicalOrderItem {
  id?: number
  itemName: string
  itemCode?: string
  itemType?: string
  spec?: string
  dose?: number
  doseUnit?: string
  usageMethod?: string
  frequency?: string
  days?: number
  quantity?: number
  quantityUnit?: string
  unitPrice?: number
  amount?: number
  itemStatus?: string
  remark?: string
}

export interface ClinicalOrder {
  id: number
  orderNo: string
  encounterId?: number
  patientId: number
  deptId: number
  doctorId: number
  orderType: string
  orderCategory?: string
  orderStatus: string
  billId?: number
  isStat?: number
  orderTime?: string
  remark?: string
  items: ClinicalOrderItem[]
}

export interface ClinicalOrderCreateRequest {
  patientId: number
  encounterId?: number
  deptId: number
  doctorId: number
  orderType: string
  orderCategory: string
  isStat: number
  remark?: string
  items: ClinicalOrderItem[]
}
