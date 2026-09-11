export interface Patient {
  id: number
  empiNo: string
  name: string
  namePinyin?: string
  gender: number
  birthDate?: string
  ageDisplay?: string
  idType: string
  idNo: string
  nationality?: string
  nation?: string
  maritalStatus?: string
  occupation?: string
  phone: string
  phoneBackup?: string
  address?: string
  bloodType?: string
  allergyHistory?: string
  insuranceType?: string
  insuranceNo?: string
  patientType?: string
  patientStatus?: string
}

export interface PatientQuery {
  page: number
  size: number
  keyword?: string
  idType?: string
  idNo?: string
  phone?: string
}

export interface PatientCreateRequest {
  name: string
  gender?: number
  birthDate?: string
  idType: string
  idNo: string
  nationality?: string
  nation?: string
  maritalStatus?: string
  occupation?: string
  phone: string
  phoneBackup?: string
  address?: string
  bloodType?: string
  allergyHistory?: string
  insuranceType?: string
  insuranceNo?: string
}

export type PatientUpdateRequest = Omit<PatientCreateRequest, 'idType' | 'idNo'>
