import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { AxiosAdapter } from 'axios'
import { http } from './http'
import { pharmaApi } from './pharmaApi'

const transport = vi.fn<AxiosAdapter>()
const originalAdapter = http.defaults.adapter
const id = '2099735874343563265'
const otherId = '2099735874343563266'

describe('pharmaApi with real Axios serialization', () => {
  beforeEach(() => {
    transport.mockReset()
    transport.mockImplementation(async (config) => ({
      config, status: 200, statusText: 'OK', headers: {},
      data: JSON.stringify({ code: 200, message: '成功', data: { id } }),
    }))
    http.defaults.adapter = transport
  })

  afterEach(() => { http.defaults.adapter = originalAdapter })

  it('queries catalog and inventory with explicit filters', async () => {
    await pharmaApi.queryDrugs({ page: 1, size: 20, keyword: '阿莫西林', isActive: 1 })
    expect(transport.mock.calls[0][0]).toMatchObject({
      url: '/pharma/drugs', method: 'get', params: { page: 1, size: 20, keyword: '阿莫西林', isActive: 1 },
    })
    await pharmaApi.listNearExpiry('OPD', 90)
    expect(transport.mock.calls[1][0]).toMatchObject({ url: '/pharma/inventory/near-expiry', params: { warehouseCode: 'OPD', days: 90 } })
  })

  it('still submits existing numeric IDs and quantities without changing their representation', async () => {
    const operation = { operationType: 'OUTBOUND' as const, drugId: 1, batchId: 2, warehouseCode: 'OPD', quantity: 3 }
    await pharmaApi.operateInventory(operation)
    expect(transport.mock.calls[0][0]).toMatchObject({ url: '/pharma/inventory/operations', method: 'post' })
    expect(JSON.parse(transport.mock.calls[0][0].data)).toEqual(operation)
    const dispense = { prescriptionId: 3, rxReviewId: 4, patientId: 5, warehouseCode: 'OPD', items: [{ drugId: 1, quantity: 3, unit: '盒' }] }
    await pharmaApi.dispense(dispense)
    expect(transport.mock.calls[1][0]).toMatchObject({ url: '/pharma/dispenses', method: 'post' })
    expect(JSON.parse(transport.mock.calls[1][0].data)).toEqual(dispense)
  })

  it('preserves 19-digit IDs in the patient URL and serialized dispense JSON', async () => {
    await pharmaApi.listDispenses(id)
    expect(http.getUri(transport.mock.calls[0][0])).toBe(`/api/pharma/dispenses/patient/${id}`)
    const dispense = {
      prescriptionId: otherId, rxReviewId: '2099735874343563267', patientId: id, warehouseCode: 'OPD',
      pharmacistId: '2099735874343563268',
      items: [{ prescriptionItemId: '2099735874343563269', drugId: '2099735874343563270', quantity: 3, unit: '盒' }],
    }
    const result = await pharmaApi.dispense(dispense)
    const request = transport.mock.calls[1][0]
    expect(typeof request.data).toBe('string')
    expect(JSON.parse(request.data)).toEqual(dispense)
    expect(result.id).toBe(id)
  })

  it('preserves catalog, batch, reference and operator IDs in paths, queries and inventory JSON', async () => {
    await pharmaApi.setDrugActive(id, true)
    expect(http.getUri(transport.mock.calls[0][0])).toBe(`/api/pharma/drugs/${id}/active?active=true`)
    await pharmaApi.listBatches({ drugId: id })
    expect(http.getUri(transport.mock.calls[1][0])).toBe(`/api/pharma/inventory/batches?drugId=${id}`)
    await pharmaApi.traceInventory({ drugId: id, batchId: otherId })
    expect(http.getUri(transport.mock.calls[2][0])).toBe(`/api/pharma/inventory/transactions?drugId=${id}&batchId=${otherId}`)
    const operation = {
      operationType: 'OUTBOUND' as const, drugId: id, batchId: otherId, warehouseCode: 'OPD', quantity: 2,
      operatorId: '2099735874343563267', referenceId: '2099735874343563268',
    }
    await pharmaApi.operateInventory(operation)
    expect(JSON.parse(transport.mock.calls[3][0].data)).toEqual(operation)
    await pharmaApi.returnDispense(id, { operatorId: otherId })
    expect(http.getUri(transport.mock.calls[4][0])).toBe(`/api/pharma/dispenses/${id}/return?operatorId=${otherId}`)
  })

  it('approves and rejects string-ID reviews with unchanged reviewer identity', async () => {
    await pharmaApi.getReview(id)
    await pharmaApi.approveReview(id, otherId, '王药师')
    await pharmaApi.rejectReview(id, '重复用药', otherId, '王药师')
    expect(transport.mock.calls[0][0]).toMatchObject({ url: `/pharma/rx-reviews/${id}`, method: 'get' })
    expect(transport.mock.calls[1][0]).toMatchObject({
      url: `/pharma/rx-reviews/${id}/approve`, method: 'put', params: { reviewerId: otherId, reviewerName: '王药师' },
    })
    expect(http.getUri(transport.mock.calls[1][0])).toContain(`reviewerId=${otherId}`)
    expect(transport.mock.calls[2][0]).toMatchObject({
      url: `/pharma/rx-reviews/${id}/reject`, method: 'put', params: { reviewerId: otherId, reviewerName: '王药师' },
    })
    expect(JSON.parse(transport.mock.calls[2][0].data)).toEqual({ rejectReason: '重复用药' })
  })
})
