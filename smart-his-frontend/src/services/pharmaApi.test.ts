import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { pharmaApi } from './pharmaApi'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn(), put: vi.fn() } }))

describe('pharmaApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('queries catalog and inventory with explicit filters', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { data: { records: [], total: 0 } } } as never)
    await pharmaApi.queryDrugs({ page: 1, size: 20, keyword: '阿莫西林', isActive: 1 })
    expect(http.get).toHaveBeenCalledWith('/pharma/drugs', { params: { page: 1, size: 20, keyword: '阿莫西林', isActive: 1 } })
    vi.mocked(http.get).mockResolvedValue({ data: { data: [] } } as never)
    await pharmaApi.listNearExpiry('OPD', 90)
    expect(http.get).toHaveBeenLastCalledWith('/pharma/inventory/near-expiry', { params: { warehouseCode: 'OPD', days: 90 } })
  })

  it('submits auditable inventory operation and dispense', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { data: {} } } as never)
    const operation = { operationType: 'OUTBOUND' as const, drugId: 1, batchId: 2, warehouseCode: 'OPD', quantity: 3 }
    await pharmaApi.operateInventory(operation)
    expect(http.post).toHaveBeenNthCalledWith(1, '/pharma/inventory/operations', operation)
    const dispense = { prescriptionId: 3, rxReviewId: 4, patientId: 5, warehouseCode: 'OPD', items: [{ drugId: 1, quantity: 3, unit: '盒' }] }
    await pharmaApi.dispense(dispense)
    expect(http.post).toHaveBeenNthCalledWith(2, '/pharma/dispenses', dispense)
  })

  it('approves and rejects prescription reviews with reviewer identity', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { data: { id: 9, items: [] } } } as never)
    vi.mocked(http.put).mockResolvedValue({ data: { data: {} } } as never)
    await pharmaApi.getReview(9)
    await pharmaApi.approveReview(9, 8, '王药师')
    await pharmaApi.rejectReview(10, '重复用药', 8, '王药师')
    expect(http.get).toHaveBeenCalledWith('/pharma/rx-reviews/9')
    expect(http.put).toHaveBeenNthCalledWith(1, '/pharma/rx-reviews/9/approve', undefined, { params: { reviewerId: 8, reviewerName: '王药师' } })
    expect(http.put).toHaveBeenNthCalledWith(2, '/pharma/rx-reviews/10/reject', { rejectReason: '重复用药' }, { params: { reviewerId: 8, reviewerName: '王药师' } })
  })
})
