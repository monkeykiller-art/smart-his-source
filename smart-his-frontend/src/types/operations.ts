import type { PageResult } from './api'

export type BillStatus = 'UNSETTLED' | 'PARTIAL' | 'SETTLED' | 'CANCELLED'
export type VisitType = 'OUTPATIENT' | 'INPATIENT' | 'EMERGENCY'

export interface Bill {
  id: number
  billNo: string
  patientId: number
  admissionId?: number
  encounterId?: number
  visitType?: VisitType
  deptId?: number
  totalAmount: number
  discountAmount: number
  payableAmount: number
  paidAmount: number
  billStatus: BillStatus
  billType?: string
  remark?: string
  voidReason?: string
  createdTime?: string
}

export interface BillItem {
  id: number
  billId: number
  itemSeq: number
  feeItemId?: number
  itemCode?: string
  itemName: string
  itemClass?: string
  spec?: string
  unit?: string
  unitPrice: number
  quantity: number
  amount: number
  orderId?: number
  orderItemId?: number
  chargeTime?: string
  isRefunded?: number
  itemStatus?: string
}

export interface BillTransaction {
  id: number
  billId: number
  transactionNo: string
  transactionType: 'PAYMENT' | 'REFUND'
  amount: number
  payMethod?: 'CASH' | 'POS' | 'WECHAT' | 'ALIPAY'
  referenceNo?: string
  reason?: string
  transactionTime: string
  transactionStatus: string
}

export interface BillPaymentRequest {
  amount: string
  payMethod: 'CASH' | 'POS' | 'WECHAT' | 'ALIPAY'
  referenceNo?: string
  idempotencyKey: string
}

export interface BillRefundRequest {
  amount: string
  reason: string
  idempotencyKey: string
}

export interface BillVoidRequest {
  reason: string
}

export interface BillQuery {
  page: number
  size: number
  patientId?: number
  billStatus?: BillStatus
  visitType?: VisitType
}

export type BillPage = PageResult<Bill>

export interface AccountRecord {
  id: number
  accountNo: string
  cashierId: string
  cashierName: string
  totalAmount: number
  cashAmount: number
  posAmount: number
  otherAmount: number
  billCount: number
  accountDate: string
  submitTime?: string
  receiveTime?: string
  receiverId?: string
  accountStatus: 'PENDING' | 'SUBMITTED' | 'RECEIVED' | 'CANCELLED'
  receiveStatus?: string
}

export interface SettlementRecord {
  id: number
  settleNo: string
  patientId: number
  admissionId: number
  totalAmount: number
  insuranceAmount: number
  depositAmount: number
  selfPayAmount: number
  settleType?: string
  payMethod?: string
  cashierName?: string
  settleTime?: string
  settleStatus: string
}

export interface SettlementPreview {
  patientId: number
  admissionId: number
  totalAmount: number
  insuranceAmount: number
  depositAmount: number
  selfPayAmount: number
  depositBalance: number
}
