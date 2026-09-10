export interface Department {
  id: number
  deptCode: string
  deptName: string
  deptType?: string
  parentId?: number
  sortOrder?: number
  deptStatus?: string
  description?: string
}

export interface Doctor {
  id: number
  employeeNo: string
  doctorName: string
  namePinyin?: string
  gender?: number
  deptId: number
  deptName: string
  title?: string
  specialty?: string
  prescribeRight?: boolean
  antibioticLevel?: string
  doctorStatus?: string
}

export interface Schedule {
  id: number
  deptId: number
  deptName: string
  doctorId: number
  doctorName: string
  scheduleDate: string
  timePeriod: string
  startTime?: string
  endTime?: string
  totalQuota: number
  usedQuota: number
  availableQuota: number
  regFee: number
  regLevel?: string
  scheduleStatus: string
}

export interface Registration {
  id: number
  regNo: string
  patientId: number
  patientName: string
  scheduleId: number
  deptId: number
  deptName: string
  doctorId: number
  doctorName: string
  visitSeq: number
  regDate: string
  timePeriod: string
  regFee: number
  payStatus: string
  payTime?: string
  regSource?: string
  regStatus: string
  billId?: number
  encounterId?: number
  createdTime?: string
}

export interface ScheduleQuery {
  page: number
  size: number
  deptId?: number
  doctorId?: number
  scheduleDate?: string
  timePeriod?: string
}

export interface RegistrationQuery {
  page: number
  size: number
  patientId?: number
  doctorId?: number
  deptId?: number
  regDate?: string
  payStatus?: string
  regStatus?: string
}

export interface RegistrationCreateRequest {
  patientId: number
  scheduleId: number
  regSource?: string
}
