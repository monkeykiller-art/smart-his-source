import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { BillPrint } from './BillPrint'
import type { Bill, BillTransaction } from '@/types/operations'

const bill: Bill = { id: 1, billNo: 'B001', patientId: 5, totalAmount: '1.2345', discountAmount: 0, payableAmount: '1.2345', paidAmount: '0.1001', billStatus: 'PARTIAL' }
describe('BillPrint', () => {
  it('isolates the fee document at body level and preserves four decimal amounts', () => {
    const { container } = render(<BillPrint kind="items" bill={bill} transactions={[]} items={[{ id: 2, billId: 1, itemSeq: 1, itemName: '<script>test</script>', quantity: 1, unitPrice: '1.2345', amount: '1.2345' }]} />)
    const document = screen.getByLabelText('单据打印内容')
    expect(document.parentElement).toBe(window.document.body)
    expect(container).toBeEmptyDOMElement()
    expect(document.querySelector('script')).toBeNull()
    expect(document).toHaveTextContent('待收：¥1.1344')
    expect(document).toHaveTextContent('<script>test</script>')
  })
  it('includes only successful transactions in the receipt and labels refunds', () => {
    const transactions = ['SUCCESS', 'FAILED'].map((status, i) => ({ id: i, billId: 1, transactionNo: `T${i}`, transactionType: 'REFUND', amount: '0.0001', transactionTime: '2026-09-17T10:00:00', transactionStatus: status } as BillTransaction))
    render(<BillPrint kind="receipt" bill={bill} items={[]} transactions={transactions} />)
    expect(screen.getByText('T0')).toBeInTheDocument()
    expect(screen.queryByText('T1')).not.toBeInTheDocument()
    expect(screen.getByText('退费')).toBeInTheDocument()
    expect(screen.getByText('本单为院内费用记录。')).toBeInTheDocument()
  })

  it('renders an issued invoice number in the invoice document', () => {
    render(<BillPrint bill={{ ...bill, billStatus: 'SETTLED', invoiceNo: 'FP20260917001' }} items={[]} transactions={[]} kind="invoice" />)
    expect(screen.getByRole('heading', { name: '医疗收费电子发票' })).toBeInTheDocument()
    expect(screen.getByText(/发票号：FP20260917001/)).toBeInTheDocument()
  })
})
