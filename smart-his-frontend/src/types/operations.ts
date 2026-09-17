import type { PageResult } from './api'
import type { MoneyValue } from '@/utils/money'

export type BillStatus = 'UNSETTLED' | 'PARTIAL' | 'SETTLED' | 'CANCELLED'
export type VisitType = 'OUTPATIENT' | 'INPATIENT' | 'EMERGENCY'

export interface Bill {
  id: number
  billNo: string
  invoiceNo?: string
  sourceType?: 'REGISTRATION' | 'ORDER'
  sourceId?: number
  patientId: number
  admissionId?: number
  encounterId?: number
  visitType?: VisitType
  deptId?: number
  totalAmount: MoneyValue
  discountAmount: MoneyValue
  payableAmount: MoneyValue
  paidAmount: MoneyValue
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
  unitPrice: MoneyValue
  quantity: string | number
  amount: MoneyValue
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
  amount: MoneyValue
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

export interface FeeItem {
  id: number; itemCode: string; itemName: string; namePinyin?: string; itemClass: string
  itemCategory?: string; spec?: string; unit?: string; unitPrice: MoneyValue; dosageForm?: string
  isInsurance?: number; insuranceRatio?: MoneyValue; isSelfPay?: number; executeDeptType?: string
  needConfirm?: number; sortOrder?: number; itemStatus?: number
}
export interface FeeItemCreateRequest extends Omit<FeeItem, 'id' | 'unitPrice'> { unitPrice: string }
export type FeeItemUpdateRequest = Partial<Omit<FeeItemCreateRequest, 'itemCode'>>

export interface Settlement {
  id: number; settleNo: string; patientId: number; admissionId: number; encounterId?: number
  visitType?: string; patientType?: string; invoiceNo?: string; totalAmount: MoneyValue
  insuranceAmount?: MoneyValue; depositAmount?: MoneyValue; selfPayAmount?: MoneyValue
  settleBalance?: MoneyValue; settleType?: 'INTERIM' | 'FINAL' | 'ARREARS'; payMethod?: string; cashierId?: string
  cashierName?: string; settleTime?: string; settleStatus: string; remark?: string
}
export interface SettlementCreateRequest { patientId: number; admissionId: number; encounterId?: number; visitType?: string; patientType?: string; settleType?: 'INTERIM' | 'FINAL' | 'ARREARS'; payMethod?: string; cashierId?: string; cashierName?: string; remark?: string }
export interface SettlementPreview { patientId: number; admissionId: number; totalAmount: MoneyValue; insuranceAmount?: MoneyValue; depositAmount?: MoneyValue; selfPayAmount?: MoneyValue; depositBalance?: MoneyValue }
export interface Deposit { id: number; depositNo: string; patientId: number; admissionId: number; receiptNo?: string; amount: MoneyValue; payMethod: string; depositType?: string; balanceBefore?: MoneyValue; balanceAfter?: MoneyValue; cashierName?: string; chargeTime?: string; remark?: string; depositStatus: string }
export interface DepositCreateRequest { patientId: number; admissionId: number; amount: string; payMethod: string; receiptNo?: string; cashierId?: string; cashierName?: string; remark?: string }
export interface DepositRefundRequest { amount: string; cashierId?: string; cashierName?: string; remark?: string }
export interface CashierAccount { id: number; accountNo: string; cashierId: string; cashierName: string; totalAmount: MoneyValue; cashAmount?: MoneyValue; posAmount?: MoneyValue; otherAmount?: MoneyValue; billCount: number; accountDate: string; accountStatus: string; receiveStatus?: string; printCount?: number }
