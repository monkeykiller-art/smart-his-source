import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { Bill, BillItem, BillPage, BillPaymentRequest, BillQuery, BillRefundRequest, BillTransaction, BillVoidRequest, FeeItem, FeeItemCreateRequest, FeeItemUpdateRequest, Settlement, SettlementCreateRequest, SettlementPreview, Deposit, DepositCreateRequest, DepositRefundRequest, CashierAccount } from '@/types/operations'

export const operationsApi = {
  async queryBills(query: BillQuery): Promise<BillPage> {
    const response = await http.get<ApiResponse<BillPage>>('/operations/bills', { params: query })
    return response.data.data
  },
  async getBill(id: number): Promise<Bill> {
    const response = await http.get<ApiResponse<Bill>>(`/operations/bills/${id}`)
    return response.data.data
  },
  async issueInvoice(id: number): Promise<Bill> {
    const response = await http.post<ApiResponse<Bill>>(`/operations/bills/${id}/invoice`)
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
  async queryFeeItems(params: { page: number; size: number; keyword?: string; itemClass?: string }): Promise<{ records: FeeItem[]; total: number; page: number; size: number; totalPages: number }> {
    const response = await http.get<ApiResponse<{ records: FeeItem[]; total: number; page: number; size: number; totalPages: number }>>('/operations/fee-items', { params })
    return response.data.data
  },
  async createFeeItem(request: FeeItemCreateRequest): Promise<FeeItem> {
    const response = await http.post<ApiResponse<FeeItem>>('/operations/fee-items', request)
    return response.data.data
  },
  async updateFeeItem(id: number, request: FeeItemUpdateRequest): Promise<FeeItem> {
    const response = await http.put<ApiResponse<FeeItem>>(`/operations/fee-items/${id}`, request)
    return response.data.data
  },
  async querySettlements(params: { page: number; size: number; patientId?: number; settleStatus?: string }): Promise<{ records: Settlement[]; total: number; page: number; size: number; totalPages: number }> {
    const response = await http.get<ApiResponse<{ records: Settlement[]; total: number; page: number; size: number; totalPages: number }>>('/operations/settlements', { params })
    return response.data.data
  },
  async previewSettlement(admissionId: number): Promise<SettlementPreview> {
    const response = await http.get<ApiResponse<SettlementPreview>>(`/operations/settlements/preview/${admissionId}`)
    return response.data.data
  },
  async createSettlement(request: SettlementCreateRequest): Promise<Settlement> {
    const response = await http.post<ApiResponse<Settlement>>('/operations/settlements', request)
    return response.data.data
  },
  async queryDeposits(params: { page: number; size: number; patientId?: number; admissionId?: number; depositStatus?: string }): Promise<{ records: Deposit[]; total: number; page: number; size: number; totalPages: number }> {
    const response = await http.get<ApiResponse<{ records: Deposit[]; total: number; page: number; size: number; totalPages: number }>>('/operations/deposits', { params })
    return response.data.data
  },
  async createDeposit(request: DepositCreateRequest): Promise<Deposit> {
    const response = await http.post<ApiResponse<Deposit>>('/operations/deposits', request)
    return response.data.data
  },
  async refundDeposit(id: number, request: DepositRefundRequest): Promise<Deposit> {
    const response = await http.post<ApiResponse<Deposit>>(`/operations/deposits/${id}/refund`, request)
    return response.data.data
  },
  async queryCashierAccounts(params: { page: number; size: number; cashierId?: string; accountStatus?: string }): Promise<{ records: CashierAccount[]; total: number; page: number; size: number; totalPages: number }> {
    const response = await http.get<ApiResponse<{ records: CashierAccount[]; total: number; page: number; size: number; totalPages: number }>>('/operations/accounts', { params })
    return response.data.data
  },
  async previewInsurance(admissionId: number): Promise<SettlementPreview> {
    const response = await http.get<ApiResponse<SettlementPreview>>(`/operations/insurance/preview/${admissionId}`)
    return response.data.data
  },
}
