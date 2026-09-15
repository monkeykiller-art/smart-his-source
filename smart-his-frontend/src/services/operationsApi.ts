import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { Bill, BillItem, BillPage, BillPaymentRequest, BillQuery, BillRefundRequest, BillTransaction, BillVoidRequest } from '@/types/operations'

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
}
