import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { operationsApi } from './operationsApi'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn(), put: vi.fn() } }))

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

  it('manages configurable fee items and prices', async () => {
    const page = { records: [], total: 0, page: 1, size: 20, totalPages: 0 }
    const item = { id: 7, itemCode: 'LAB001', itemName: '血常规', itemClass: 'LAB', unitPrice: '25.0000' }
    vi.mocked(http.get).mockResolvedValueOnce({ data: { code: 200, message: 'success', data: page } })
    vi.mocked(http.post).mockResolvedValueOnce({ data: { code: 200, message: 'success', data: item } })
    vi.mocked(http.put).mockResolvedValueOnce({ data: { code: 200, message: 'success', data: item } })
    await expect(operationsApi.queryFeeItems({ page: 1, size: 20, itemClass: 'LAB' })).resolves.toEqual(page)
    await expect(operationsApi.createFeeItem({ itemCode: 'LAB001', itemName: '血常规', itemClass: 'LAB', unitPrice: '25.0000' })).resolves.toEqual(item)
    await expect(operationsApi.updateFeeItem(7, { unitPrice: '26.0000' })).resolves.toEqual(item)
    expect(http.get).toHaveBeenCalledWith('/operations/fee-items', { params: { page: 1, size: 20, itemClass: 'LAB' } })
    expect(http.post).toHaveBeenCalledWith('/operations/fee-items', { itemCode: 'LAB001', itemName: '血常规', itemClass: 'LAB', unitPrice: '25.0000' })
    expect(http.put).toHaveBeenCalledWith('/operations/fee-items/7', { unitPrice: '26.0000' })
  })

  it('supports settlement preview, final settlement creation, and record queries', async () => {
    const preview = { patientId: 1001, admissionId: 88, totalAmount: '100.0000', depositBalance: '20.0000' }
    const settlement = { id: 3, settleNo: 'JS001', patientId: 1001, admissionId: 88, totalAmount: '100.0000', settleStatus: 'SETTLED' }
    vi.mocked(http.get)
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: preview } })
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: { records: [settlement], total: 1, page: 1, size: 20, totalPages: 1 } } })
    vi.mocked(http.post).mockResolvedValueOnce({ data: { code: 200, message: 'success', data: settlement } })
    await expect(operationsApi.previewSettlement(88)).resolves.toEqual(preview)
    await expect(operationsApi.querySettlements({ page: 1, size: 20, settleStatus: 'SETTLED' })).resolves.toEqual({ records: [settlement], total: 1, page: 1, size: 20, totalPages: 1 })
    await expect(operationsApi.createSettlement({ patientId: 1001, admissionId: 88, settleType: 'FINAL', payMethod: 'CASH' })).resolves.toEqual(settlement)
    expect(http.get).toHaveBeenNthCalledWith(1, '/operations/settlements/preview/88')
    expect(http.get).toHaveBeenNthCalledWith(2, '/operations/settlements', { params: { page: 1, size: 20, settleStatus: 'SETTLED' } })
    expect(http.post).toHaveBeenCalledWith('/operations/settlements', { patientId: 1001, admissionId: 88, settleType: 'FINAL', payMethod: 'CASH' })
  })

  it('supports prepaid deposit and cashier account queries', async () => {
    const deposit = { id: 1, depositNo: 'YJ001', patientId: 1001, admissionId: 88, amount: '100.0000', payMethod: 'CASH', depositStatus: 'ACTIVE' }
    const accounts = { records: [], total: 0, page: 1, size: 20, totalPages: 0 }
    vi.mocked(http.get)
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: { records: [deposit], total: 1, page: 1, size: 20, totalPages: 1 } } })
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: accounts } })
    vi.mocked(http.post)
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: deposit } })
      .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: { ...deposit, depositStatus: 'REFUNDED' } } })
    await expect(operationsApi.queryDeposits({ page: 1, size: 20, patientId: 1001 })).resolves.toMatchObject({ total: 1 })
    await expect(operationsApi.createDeposit({ patientId: 1001, admissionId: 88, amount: '100.0000', payMethod: 'CASH' })).resolves.toEqual(deposit)
    await expect(operationsApi.refundDeposit(1, { amount: '20.0000' })).resolves.toMatchObject({ depositStatus: 'REFUNDED' })
    await expect(operationsApi.queryCashierAccounts({ page: 1, size: 20, cashierId: 'C001' })).resolves.toEqual(accounts)
    expect(http.get).toHaveBeenNthCalledWith(1, '/operations/deposits', { params: { page: 1, size: 20, patientId: 1001 } })
    expect(http.post).toHaveBeenNthCalledWith(2, '/operations/deposits/1/refund', { amount: '20.0000' })
  })

  it('requests the local insurance settlement preview adapter', async () => {
    const preview = { patientId: 1001, admissionId: 88, totalAmount: '100.0000', insuranceAmount: '70.0000' }
    vi.mocked(http.get).mockResolvedValueOnce({ data: { code: 200, message: 'success', data: preview } })
    await expect(operationsApi.previewInsurance(88)).resolves.toEqual(preview)
    expect(http.get).toHaveBeenCalledWith('/operations/insurance/preview/88')
  })
})
