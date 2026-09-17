export interface DrugCatalog {
  id: number; drugCode: string; genericName: string; tradeName?: string; pinyinCode?: string
  dosageForm: string; strength: string; manufacturer: string; approvalNo?: string
  packageUnit: string; minUnit: string; conversionFactor: number; purchasePrice: number; retailPrice: number
  prescriptionType: string; antibioticLevel?: string; isActive: number
}
export interface DrugCatalogPage { records: DrugCatalog[]; total: number; page: number; size: number; totalPages: number }
export type DrugCatalogSaveRequest = Omit<DrugCatalog, 'id' | 'isActive'> & { isActive?: number }
export interface InventoryBatch {
  id: number; drugId: number; drugCode?: string; drugName?: string; warehouseCode: string; batchNo: string
  productionDate?: string; expiryDate: string; unitCost: number; quantity: number; availableQuantity: number
  lockedQuantity: number; nearExpiry: boolean; expired: boolean
}
export type InventoryOperationType = 'INBOUND' | 'OUTBOUND' | 'STOCKTAKE' | 'RETURN' | 'LOSS'
export interface InventoryOperationRequest {
  operationType: InventoryOperationType; drugId: number; batchId?: number; warehouseCode: string; batchNo?: string
  productionDate?: string; expiryDate?: string; unitCost?: number; quantity: number; referenceType?: string
  referenceId?: number; operatorId?: number; operatorName?: string; reason?: string
}
export interface InventoryTransaction {
  id: number; transactionNo: string; operationType: InventoryOperationType; drugId: number; batchId: number
  warehouseCode: string; quantityChange: number; quantityBefore: number; quantityAfter: number
  referenceType?: string; referenceId?: number; operatorName?: string; reason?: string; occurredTime: string
}
export interface DispenseItemRequest { prescriptionItemId?: number; drugId: number; quantity: number; unit: string }
export interface DispenseCreateRequest {
  prescriptionId: number; rxReviewId: number; patientId: number; warehouseCode: string
  pharmacistId?: number; pharmacistName?: string; remark?: string; items: DispenseItemRequest[]
}
export interface DispenseItem extends DispenseItemRequest {
  id: number; drugCode?: string; drugName?: string; strength?: string; batchId: number; batchNo: string; expiryDate: string
}
export interface Dispense {
  id: number; dispenseNo: string; prescriptionId: number; rxReviewId: number; patientId: number
  warehouseCode: string; dispenseStatus: string; pharmacistId?: number; pharmacistName?: string
  dispenseTime?: string; returnTime?: string; remark?: string; items: DispenseItem[]
}
export interface RxReviewItem { id: number; alertType: string; alertLevel: string; drugNameA?: string; drugNameB?: string; alertDesc?: string; suggestion?: string }
export interface RxReview {
  id: number; reviewNo: string; orderId?: number; patientId: number; doctorId: number; prescriptionType: string
  reviewStatus: string; reviewResult?: string; reviewerName?: string; reviewTime?: string; rejectReason?: string
  warningCount: number; errorCount: number; items?: RxReviewItem[]
}
export interface RxReviewPage { records: RxReview[]; total: number; page: number; size: number; totalPages: number }
