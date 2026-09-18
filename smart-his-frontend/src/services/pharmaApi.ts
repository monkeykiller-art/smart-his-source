import { http } from './http'
import type { ApiResponse } from '@/types/api'
import type { Dispense, DispenseCreateRequest, DrugCatalog, DrugCatalogPage, DrugCatalogSaveRequest, EntityId, InventoryBatch, InventoryOperationRequest, InventoryTransaction, RxReview, RxReviewPage } from '@/types/pharma'

export const pharmaApi = {
  async queryDrugs(params: { page: number; size: number; keyword?: string; isActive?: number }) {
    const response = await http.get<ApiResponse<DrugCatalogPage>>('/pharma/drugs', { params })
    return response.data.data
  },
  async createDrug(request: DrugCatalogSaveRequest) {
    const response = await http.post<ApiResponse<DrugCatalog>>('/pharma/drugs', request)
    return response.data.data
  },
  async updateDrug(id: EntityId, request: DrugCatalogSaveRequest) {
    const response = await http.put<ApiResponse<DrugCatalog>>(`/pharma/drugs/${id}`, request)
    return response.data.data
  },
  async setDrugActive(id: EntityId, active: boolean) {
    const response = await http.put<ApiResponse<DrugCatalog>>(`/pharma/drugs/${id}/active`, undefined, { params: { active } })
    return response.data.data
  },
  async listBatches(params: { drugId?: EntityId; warehouseCode?: string; availableOnly?: boolean } = {}) {
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
  async traceInventory(params: { drugId?: EntityId; batchId?: EntityId } = {}) {
    const response = await http.get<ApiResponse<InventoryTransaction[]>>('/pharma/inventory/transactions', { params })
    return response.data.data
  },
  async dispense(request: DispenseCreateRequest) {
    const response = await http.post<ApiResponse<Dispense>>('/pharma/dispenses', request)
    return response.data.data
  },
  async listDispenses(patientId: EntityId) {
    const response = await http.get<ApiResponse<Dispense[]>>(`/pharma/dispenses/patient/${patientId}`)
    return response.data.data
  },
  async returnDispense(id: EntityId, params: { operatorId?: EntityId; operatorName?: string; reason?: string }) {
    const response = await http.put<ApiResponse<Dispense>>(`/pharma/dispenses/${id}/return`, undefined, { params })
    return response.data.data
  },
  async queryReviews(params: { page: number; size: number; reviewStatus?: string }) {
    const response = await http.get<ApiResponse<RxReviewPage>>('/pharma/rx-reviews', { params })
    return response.data.data
  },
  async getReview(id: EntityId) {
    const response = await http.get<ApiResponse<RxReview>>(`/pharma/rx-reviews/${id}`)
    return response.data.data
  },
  async approveReview(id: EntityId, reviewerId?: EntityId, reviewerName?: string) {
    const response = await http.put<ApiResponse<RxReview>>(`/pharma/rx-reviews/${id}/approve`, undefined, { params: { reviewerId, reviewerName } })
    return response.data.data
  },
  async rejectReview(id: EntityId, rejectReason: string, reviewerId?: EntityId, reviewerName?: string) {
    const response = await http.put<ApiResponse<RxReview>>(`/pharma/rx-reviews/${id}/reject`, { rejectReason }, { params: { reviewerId, reviewerName } })
    return response.data.data
  },
}
