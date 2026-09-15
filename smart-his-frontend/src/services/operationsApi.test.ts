import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { operationsApi } from './operationsApi'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn() } }))

describe('operationsApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('queries paginated bills with patient and status filters', async () => {
    const page = { records: [], total: 0, page: 1, size: 20, totalPages: 0 }
    vi.mocked(http.get).mockResolvedValueOnce({ data: { code: 200, message: 'success', data: page } })

    await expect(operationsApi.queryBills({ page: 1, size: 20, patientId: 1001, billStatus: 'UNSETTLED' })).resolves.toEqual(page)
    expect(http.get).toHaveBeenCalledWith('/operations/bills', {
      params: { page: 1, size: 20, patientId: 1001, billStatus: 'UNSETTLED' },
    })
  })

  it('loads a bill and its fee lines from their read endpoints', async () => {
    const bill = { id: 12, billNo: 'B20260914001', patientId: 1001, billStatus: 'UNSETTLED' }
    const items = [{ id: 5, billId: 12, itemName: '门诊诊查费', amount: 12 }]
    vi.mocked(http.get)
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: bill } })
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: items } })

    await expect(operationsApi.getBill(12)).resolves.toEqual(bill)
    await expect(operationsApi.listBillItems(12)).resolves.toEqual(items)
    expect(http.get).toHaveBeenNthCalledWith(1, '/operations/bills/12')
    expect(http.get).toHaveBeenNthCalledWith(2, '/operations/bills/12/items')
  })

  it('sends idempotent payment, refund, and void commands to the bill endpoints', async () => {
    const transaction = { id: 4, billId: 12, transactionNo: 'SK001', transactionType: 'PAYMENT', amount: 8, transactionTime: '2026-09-14T10:00:00', transactionStatus: 'SUCCESS' }
    const bill = { id: 12, billNo: 'B001', billStatus: 'CANCELLED' }
    vi.mocked(http.post)
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: transaction } })
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: { ...transaction, transactionType: 'REFUND' } } })
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: bill } })
    vi.mocked(http.get).mockResolvedValueOnce({ data: { code: 200, message: 'success', data: [transaction] } })

    await expect(operationsApi.payBill(12, { amount: '8.00', payMethod: 'CASH', idempotencyKey: 'cashier-01-key-0001' })).resolves.toEqual(transaction)
    await operationsApi.refundBill(12, { amount: '3.00', reason: '重复收费', idempotencyKey: 'cashier-01-key-0002' })
    await operationsApi.voidBill(12, { reason: '重复开单' })
    await expect(operationsApi.listTransactions(12)).resolves.toEqual([transaction])

    expect(http.post).toHaveBeenNthCalledWith(1, '/operations/bills/12/payments', { amount: '8.00', payMethod: 'CASH', idempotencyKey: 'cashier-01-key-0001' })
    expect(http.post).toHaveBeenNthCalledWith(2, '/operations/bills/12/refunds', { amount: '3.00', reason: '重复收费', idempotencyKey: 'cashier-01-key-0002' })
    expect(http.post).toHaveBeenNthCalledWith(3, '/operations/bills/12/void', { reason: '重复开单' })
    expect(http.get).toHaveBeenCalledWith('/operations/bills/12/transactions')
  })
})
