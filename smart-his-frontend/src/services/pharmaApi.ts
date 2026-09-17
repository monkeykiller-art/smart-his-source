import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { Dispense, DispenseCreateRequest, DrugCatalog, DrugCatalogPage, DrugCatalogSaveRequest, InventoryBatch, InventoryOperationRequest, InventoryTransaction, RxReview, RxReviewPage } from '@/types/pharma'

export const pharmaApi = {
  async queryDrugs(params: { page: number; size: number; keyword?: string; isActive?: number }) {
    const response = await http.get<ApiResponse<DrugCatalogPage>>('/pharma/drugs', { params })
    return response.data.data
  },
  async createDrug(request: DrugCatalogSaveRequest) {
    const response = await http.post<ApiResponse<DrugCatalog>>('/pharma/drugs', request)
    return response.data.data
  },
  async updateDrug(id: number, request: DrugCatalogSaveRequest) {
    const response = await http.put<ApiResponse<DrugCatalog>>(`/pharma/drugs/${id}`, request)
    return response.data.data
  },
  async setDrugActive(id: number, active: boolean) {
    const response = await http.put<ApiResponse<DrugCatalog>>(`/pharma/drugs/${id}/active`, undefined, { params: { active } })
    return response.data.data
  },
  async listBatches(params: { drugId?: number; warehouseCode?: string; availableOnly?: boolean } = {}) {
    const response = await http.get<ApiResponse<InventoryBatch[]>>('/pharma/inventory/batches', { params })
    return response.data.data
  },
  async listNearExpiry(warehouseCode = 'OPD', days = 90) {
    const response = await http.get<ApiResponse<InventoryBatch[]>>('/pharma/inventory/near-expiry', { params: { warehouseCode, days } })
    return response.data.data
  },
  async operateInventory(request: InventoryOperationRequest) {
    const response = await http.post<ApiResponse<InventoryTransaction>>('/pharma/inventory/operations', request)
    return response.data.data
  },
  async traceInventory(params: { drugId?: number; batchId?: number } = {}) {
    const response = await http.get<ApiResponse<InventoryTransaction[]>>('/pharma/inventory/transactions', { params })
    return response.data.data
  },
  async dispense(request: DispenseCreateRequest) {
    const response = await http.post<ApiResponse<Dispense>>('/pharma/dispenses', request)
    return response.data.data
  },
  async listDispenses(patientId: number) {
    const response = await http.get<ApiResponse<Dispense[]>>(`/pharma/dispenses/patient/${patientId}`)
    return response.data.data
  },
  async returnDispense(id: number, params: { operatorId?: number; operatorName?: string; reason?: string }) {
    const response = await http.put<ApiResponse<Dispense>>(`/pharma/dispenses/${id}/return`, undefined, { params })
    return response.data.data
  },
  async queryReviews(params: { page: number; size: number; reviewStatus?: string }) {
    const response = await http.get<ApiResponse<RxReviewPage>>('/pharma/rx-reviews', { params })
    return response.data.data
  },
  async getReview(id: number) {
    const response = await http.get<ApiResponse<RxReview>>(`/pharma/rx-reviews/${id}`)
    return response.data.data
  },
  async approveReview(id: number, reviewerId?: number, reviewerName?: string) {
    const response = await http.put<ApiResponse<RxReview>>(`/pharma/rx-reviews/${id}/approve`, undefined, { params: { reviewerId, reviewerName } })
    return response.data.data
  },
  async rejectReview(id: number, rejectReason: string, reviewerId?: number, reviewerName?: string) {
    const response = await http.put<ApiResponse<RxReview>>(`/pharma/rx-reviews/${id}/reject`, { rejectReason }, { params: { reviewerId, reviewerName } })
    return response.data.data
  },
}
