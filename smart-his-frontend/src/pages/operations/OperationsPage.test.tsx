import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { operationsApi } from '@/services/operationsApi'
import { OperationsPage } from './OperationsPage'
import { patientApi } from '@/services/patientApi'

vi.mock('@/services/operationsApi', () => ({ operationsApi: {
  queryBills: vi.fn(), getBill: vi.fn(), listBillItems: vi.fn(), listTransactions: vi.fn(),
  payBill: vi.fn(), refundBill: vi.fn(), voidBill: vi.fn(),
} }))
vi.mock('@/services/patientApi', () => ({ patientApi: { query: vi.fn() } }))

describe('OperationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(operationsApi.queryBills).mockResolvedValue({
      records: [{ id: 12, billNo: 'B20260914001', patientId: 1001, visitType: 'OUTPATIENT', totalAmount: 12, discountAmount: 0, payableAmount: 12, paidAmount: 0, billStatus: 'UNSETTLED', createdTime: '2026-09-14T09:30:00' }],
      total: 1, page: 1, size: 20, totalPages: 1,
    })
    vi.mocked(operationsApi.getBill).mockResolvedValue({ id: 12, billNo: 'B20260914001', patientId: 1001, visitType: 'OUTPATIENT', totalAmount: 12, discountAmount: 0, payableAmount: 12, paidAmount: 0, billStatus: 'UNSETTLED' })
    vi.mocked(operationsApi.listBillItems).mockResolvedValue([{ id: 8, billId: 12, itemSeq: 1, itemName: '门诊诊查费', itemClass: 'REGISTRATION', feeItemId: 21, unitPrice: 12, quantity: 1, amount: 12 }])
    vi.mocked(operationsApi.listTransactions).mockResolvedValue([])
    vi.mocked(operationsApi.payBill).mockResolvedValue({ id: 20, billId: 12, transactionNo: 'SK001', transactionType: 'PAYMENT', amount: 12, transactionTime: '2026-09-14T10:00:00', transactionStatus: 'SUCCESS' })
  })

  it('loads pending bills, applies patient filtering, and opens fee details', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}><OperationsPage /></QueryClientProvider>)

    expect(await screen.findByText('B20260914001')).toBeInTheDocument()
    expect(operationsApi.queryBills).toHaveBeenCalledWith({ page: 1, size: 20, billStatus: 'UNSETTLED' })

    fireEvent.change(screen.getByRole('textbox', { name: '患者编号' }), { target: { value: '1001' } })
    fireEvent.click(screen.getByRole('button', { name: /查询/ }))
    await waitFor(() => expect(operationsApi.queryBills).toHaveBeenLastCalledWith(expect.objectContaining({ patientId: 1001 })))

    fireEvent.click(await screen.findByRole('button', { name: /费用明细/ }))
    expect(await screen.findByText('门诊诊查费')).toBeInTheDocument()
    expect(await screen.findByText('挂号费 · 项目21')).toBeInTheDocument()
    expect(operationsApi.listBillItems).toHaveBeenCalledWith(12)
  })

  it('resolves an EMPI patient identifier before querying bills', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}><OperationsPage /></QueryClientProvider>)
    await screen.findByText('B20260914001')
    vi.mocked(operationsApi.queryBills).mockClear()

    vi.mocked(patientApi.query).mockResolvedValue({ records: [{ id: 1001, empiNo: 'EM20260915000001', name: '张三' }], total: 1, page: 1, size: 2, totalPages: 1 } as never)
    fireEvent.change(screen.getByRole('textbox', { name: '患者编号' }), { target: { value: 'EM20260915000001' } })
    fireEvent.click(screen.getByRole('button', { name: /查询/ }))
    await waitFor(() => expect(patientApi.query).toHaveBeenCalledWith({ page: 1, size: 2, keyword: 'EM20260915000001' }))
    await waitFor(() => expect(operationsApi.queryBills).toHaveBeenLastCalledWith(expect.objectContaining({ patientId: 1001 })))
  })

  it('requires cashier confirmation and sends the payment amount with an idempotency key', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}><OperationsPage /></QueryClientProvider>)
    await screen.findByText('B20260914001')
    fireEvent.click(screen.getByRole('button', { name: /费用明细/ }))
    await waitFor(() => expect(operationsApi.getBill).toHaveBeenCalledWith(12), { timeout: 5_000 })
    const paymentButton = await screen.findByRole('button', { name: '登记收款' }, { timeout: 5_000 })
    fireEvent.click(paymentButton)

    expect(await screen.findByText(/不会调用第三方支付平台/)).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: '确认收款' }))

    await waitFor(() => expect(operationsApi.payBill).toHaveBeenCalledWith(12, expect.objectContaining({
      amount: '12.00', payMethod: 'CASH', idempotencyKey: expect.any(String),
    })))
  }, 15_000)

  it('charges the exact four-decimal remainder and reuses the key after a failed response', async () => {
    vi.mocked(operationsApi.getBill).mockResolvedValue({ id: 12, billNo: 'B20260914001', patientId: 1001,
      totalAmount: '0.3456', discountAmount: '0', payableAmount: '0.3456', paidAmount: '0.1000', billStatus: 'PARTIAL', sourceType: 'ORDER', sourceId: 81 })
    vi.mocked(operationsApi.payBill).mockRejectedValueOnce(new Error('timeout'))
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}><OperationsPage /></QueryClientProvider>)
    await screen.findByText('B20260914001')
    fireEvent.click(screen.getByRole('button', { name: /费用明细/ }))
    await waitFor(() => expect(operationsApi.getBill).toHaveBeenCalledWith(12), { timeout: 5_000 })
    fireEvent.click(await screen.findByRole('button', { name: '登记收款' }, { timeout: 5_000 }))
    fireEvent.click(await screen.findByRole('button', { name: '确认收款' }))
    await screen.findByText(/收款未完成/)
    const first = vi.mocked(operationsApi.payBill).mock.calls[0][1]
    expect(first.amount).toBe('0.2456')
    fireEvent.click(screen.getByRole('button', { name: '确认收款' }))
    await waitFor(() => expect(operationsApi.payBill).toHaveBeenCalledTimes(2))
    expect(vi.mocked(operationsApi.payBill).mock.calls[1][1]).toEqual(first)
    client.clear()
  }, 15_000)
})
