import { createPortal } from 'react-dom'
import type { Bill, BillItem, BillTransaction } from '@/types/operations'
import { formatMoney, moneyDue } from '@/utils/money'

export type BillPrintKind = 'items' | 'receipt' | 'invoice'
export function BillPrint({ bill, items, transactions, kind }: { bill: Bill; items: BillItem[]; transactions: BillTransaction[]; kind: BillPrintKind }) {
  return createPortal(<article className="billing-print-document" aria-label="单据打印内容">
    <h1>{kind === 'items' ? '费用清单' : kind === 'invoice' ? '医疗收费电子发票' : '收退款凭据'}</h1>
    <p>账单号：{bill.billNo}　{bill.invoiceNo ? `发票号：${bill.invoiceNo}　` : ''}患者编号：{bill.patientId}</p>
    <p>账单状态：{({ UNSETTLED: '待缴费', PARTIAL: '部分缴费', SETTLED: '已结清', CANCELLED: '已作废' })[bill.billStatus]}</p>
    {kind === 'items' || kind === 'invoice' ? <table><thead><tr><th>项目</th><th>单价</th><th>数量</th><th>单位</th><th>金额</th></tr></thead><tbody>{items.map(item => <tr key={item.id}><td>{item.itemName}</td><td>{formatMoney(item.unitPrice)}</td><td>{item.quantity}</td><td>{item.unit || '—'}</td><td>{formatMoney(item.amount)}</td></tr>)}</tbody></table>
      : <table><thead><tr><th>交易号</th><th>类型</th><th>金额</th><th>时间</th></tr></thead><tbody>{transactions.filter(t => t.transactionStatus === 'SUCCESS').map(t => <tr key={t.id}><td>{t.transactionNo}</td><td>{t.transactionType === 'REFUND' ? '退费' : '收款'}</td><td>{formatMoney(t.amount)}</td><td>{t.transactionTime.replace('T', ' ')}</td></tr>)}</tbody></table>}
    <p>应收：{formatMoney(bill.payableAmount)}　净已收：{formatMoney(bill.paidAmount)}　待收：{formatMoney(bill.billStatus === 'CANCELLED' ? '0' : moneyDue(bill.payableAmount, bill.paidAmount))}</p>
    <p>{kind === 'invoice' ? '本发票由院内开票适配层生成，凭发票号可追溯原始账单。' : '本单为院内费用记录。'}</p>
  </article>, document.body)
}
