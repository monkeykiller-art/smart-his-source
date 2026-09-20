export type EntityId = string | number

export interface DrugCatalog {
  id: EntityId; drugCode: string; genericName: string; tradeName?: string; pinyinCode?: string
  dosageForm: string; strength: string; manufacturer: string; approvalNo?: string
  packageUnit: string; minUnit: string; conversionFactor: number; purchasePrice: number; retailPrice: number
  prescriptionType: string; antibioticLevel?: string; isActive: number
}
export interface DrugCatalogPage { records: DrugCatalog[]; total: number; page: number; size: number; totalPages: number }
export type DrugCatalogSaveRequest = Omit<DrugCatalog, 'id' | 'isActive'> & { isActive?: number }
export interface InventoryBatch {
  id: EntityId; drugId: EntityId; drugCode?: string; drugName?: string; warehouseCode: string; batchNo: string
  productionDate?: string; expiryDate: string; unitCost: number; quantity: number; availableQuantity: number
  lockedQuantity: number; nearExpiry: boolean; expired: boolean
}
export type InventoryOperationType = 'INBOUND' | 'OUTBOUND' | 'STOCKTAKE' | 'RETURN' | 'LOSS'
export interface InventoryOperationRequest {
  operationType: InventoryOperationType; drugId: EntityId; batchId?: EntityId; warehouseCode: string; batchNo?: string
  productionDate?: string; expiryDate?: string; unitCost?: number; quantity: number; referenceType?: string
  referenceId?: EntityId; operatorId?: EntityId; operatorName?: string; reason?: string
}
export interface InventoryTransaction {
  id: EntityId; transactionNo: string; operationType: InventoryOperationType; drugId: EntityId; batchId: EntityId
  warehouseCode: string; quantityChange: number; quantityBefore: number; quantityAfter: number
  referenceType?: string; referenceId?: EntityId; operatorName?: string; reason?: string; occurredTime: string
}
export interface DispenseItemRequest { prescriptionItemId?: EntityId; drugId: EntityId; quantity: number; unit: string }
export interface DispenseCreateRequest {
  prescriptionId: EntityId; rxReviewId: EntityId; patientId: EntityId; warehouseCode: string
  pharmacistId?: EntityId; pharmacistName?: string; remark?: string; items: DispenseItemRequest[]
}
export interface DispenseItem extends DispenseItemRequest {
  id: EntityId; drugCode?: string; drugName?: string; strength?: string; batchId: EntityId; batchNo: string; expiryDate: string
}
export interface Dispense {
  id: EntityId; dispenseNo: string; prescriptionId: EntityId; rxReviewId: EntityId; patientId: EntityId
  warehouseCode: string; dispenseStatus: string; pharmacistId?: EntityId; pharmacistName?: string
  dispenseTime?: string; returnTime?: string; remark?: string; items: DispenseItem[]
}
export interface RxReviewItem { id: EntityId; alertType: string; alertLevel: string; drugNameA?: string; drugNameB?: string; alertDesc?: string; suggestion?: string }
export interface RxReview {
  id: EntityId; reviewNo: string; orderId?: EntityId; patientId: EntityId; doctorId: EntityId; prescriptionType: string
  reviewStatus: string; reviewResult?: string; reviewerName?: string; reviewTime?: string; rejectReason?: string
  warningCount: number; errorCount: number; items?: RxReviewItem[]
}
export interface RxReviewPage { records: RxReview[]; total: number; page: number; size: number; totalPages: number }

export interface DrugInteraction {
  id: EntityId; drugCodeA: string; drugCodeB: string; interactionLevel: string
  interactionDesc: string; suggestion?: string; reference?: string; isActive: number
}

export interface DoseLimit {
  id: EntityId; drugCode: string; patientType: string; ageMin?: number; ageMax?: number
  route?: string; maxSingleDose: number; maxSingleUnit: string; maxDailyDose: number
  maxDailyUnit: string; maxFreqPerDay?: number; description?: string; isActive: number
}

export interface DrugAllergyCross {
  id: EntityId; allergyCode: string; allergyName: string; crossDrugCode: string
  crossDrugName: string; crossLevel: string; description?: string; isActive: number
}
