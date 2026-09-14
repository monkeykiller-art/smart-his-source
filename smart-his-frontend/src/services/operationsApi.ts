import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { AccountRecord, Bill, BillItem, BillPage, BillPaymentRequest, BillQuery, BillRefundRequest, BillTransaction, BillVoidRequest, SettlementPreview, SettlementRecord } from '@/types/operations'

export const operationsApi = {
  async queryBills(query: BillQuery): Promise<BillPage> {
    const response = await http.get<ApiResponse<BillPage>>('/operations/bills', { params: query })
    return response.data.data
  },
  async getBill(id: number): Promise<Bill> {
    const response = await http.get<ApiResponse<Bill>>(`/operations/bills/${id}`)
    return response.data.data
  },
  async listBillItems(id: number): Promise<BillItem[]> {
    const response = await http.get<ApiResponse<BillItem[]>>(`/operations/bills/${id}/items`)
    return response.data.data
  },
  async listTransactions(id: number): Promise<BillTransaction[]> {
    const response = await http.get<ApiResponse<BillTransaction[]>>(`/operations/bills/${id}/transactions`)
    return response.data.data
  },
  async payBill(id: number, request: BillPaymentRequest): Promise<BillTransaction> {
    const response = await http.post<ApiResponse<BillTransaction>>(`/operations/bills/${id}/payments`, request)
    return response.data.data
  },
  async refundBill(id: number, request: BillRefundRequest): Promise<BillTransaction> {
    const response = await http.post<ApiResponse<BillTransaction>>(`/operations/bills/${id}/refunds`, request)
    return response.data.data
  },
  async voidBill(id: number, request: BillVoidRequest): Promise<Bill> {
    const response = await http.post<ApiResponse<Bill>>(`/operations/bills/${id}/void`, request)
    return response.data.data
  },
  async queryAccounts(query: { page: number; size: number; cashierId?: string }): Promise<{ records: AccountRecord[]; total: number; page: number; size: number; totalPages: number }> {
    const response = await http.get<ApiResponse<{ records: AccountRecord[]; total: number; page: number; size: number; totalPages: number }>>('/operations/accounts', { params: query })
    return response.data.data
  },
  async generateAccount(cashierId: string, cashierName: string, accountDate: string): Promise<AccountRecord> {
    const response = await http.post<ApiResponse<AccountRecord>>('/operations/accounts/generate', null, { params: { cashierId, cashierName, accountDate } })
    return response.data.data
  },
  async submitAccount(id: number): Promise<AccountRecord> {
    const response = await http.post<ApiResponse<AccountRecord>>(`/operations/accounts/${id}/submit`)
    return response.data.data
  },
  async receiveAccount(id: number, receiverId: string): Promise<AccountRecord> {
    const response = await http.post<ApiResponse<AccountRecord>>(`/operations/accounts/${id}/receive`, null, { params: { receiverId } })
    return response.data.data
  },
  async querySettlements(query: { page: number; size: number; patientId?: number; admissionId?: number }): Promise<{ records: SettlementRecord[]; total: number; page: number; size: number; totalPages: number }> {
    const response = await http.get<ApiResponse<{ records: SettlementRecord[]; total: number; page: number; size: number; totalPages: number }>>('/operations/settlements', { params: query })
    return response.data.data
  },
  async previewSettlement(admissionId: number): Promise<SettlementPreview> {
    const response = await http.get<ApiResponse<SettlementPreview>>(`/operations/settlements/preview/${admissionId}`)
    return response.data.data
  },
  async createSettlement(request: { patientId: number; admissionId: number; settleType: string; payMethod: string; cashierId: string; cashierName: string }): Promise<SettlementRecord> {
    const response = await http.post<ApiResponse<SettlementRecord>>('/operations/settlements', request)
    return response.data.data
  },
}
