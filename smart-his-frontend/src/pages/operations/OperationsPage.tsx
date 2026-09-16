import { EyeOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Descriptions, Drawer, Empty, Input, InputNumber, Modal, Select, Space, Statistic, Table, Tag } from 'antd'
import type { TableColumnsType } from 'antd'
import dayjs from 'dayjs'
import { useState } from 'react'
import { operationsApi } from '@/services/operationsApi'
import { patientApi } from '@/services/patientApi'
import { formatMoney as money, moneyAmount, moneyDue, moneySum, moneyUnits, validPayment } from '@/utils/money'
import type { Bill, BillItem, BillPaymentRequest, BillQuery, BillRefundRequest, BillStatus, BillTransaction, VisitType } from '@/types/operations'

const billStatusMeta: Record<BillStatus, { label: string; color: string }> = {
  UNSETTLED: { label: '待缴费', color: 'gold' },
  PARTIAL: { label: '部分缴费', color: 'processing' },
  SETTLED: { label: '已结清', color: 'success' },
  CANCELLED: { label: '已作废', color: 'default' },
}
const visitTypeLabel: Record<VisitType, string> = {
  OUTPATIENT: '门诊',
  INPATIENT: '住院',
  EMERGENCY: '急诊',
}
const newIdempotencyKey = () => crypto.randomUUID()
const payMethodLabel: Record<string, string> = { CASH: '现金', POS: '银行卡', WECHAT: '微信', ALIPAY: '支付宝' }
const transactionTypeLabel: Record<string, string> = { PAYMENT: '收款', REFUND: '退费' }
const feeClassLabel: Record<string, string> = { REGISTRATION: '挂号费', REG: '挂号费', DRUG: '药费', MEDICINE: '药费', LAB: '检验费', IMAGING: '检查费', EXAM: '检查项目', BED: '床位费', NURSING: '护理费', TREATMENT: '治疗费', MATERIAL: '材料费', OTHER: '其他费用' }

export function OperationsPage() {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [patientIdInput, setPatientIdInput] = useState('')
  const [filterError, setFilterError] = useState('')
  const [filters, setFilters] = useState<Pick<BillQuery, 'patientId' | 'billStatus' | 'visitType'>>({ billStatus: 'UNSETTLED' })
  const [selectedBillId, setSelectedBillId] = useState<number>()
  const [paymentOpen, setPaymentOpen] = useState(false)
  const [paymentAmount, setPaymentAmount] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<BillPaymentRequest['payMethod']>('CASH')
  const [paymentReference, setPaymentReference] = useState('')
  const [paymentKey, setPaymentKey] = useState('')
  const [refundOpen, setRefundOpen] = useState(false)
  const [refundAmount, setRefundAmount] = useState('')
  const [refundReason, setRefundReason] = useState('')
  const [refundKey, setRefundKey] = useState('')
  const [voidOpen, setVoidOpen] = useState(false)
  const [voidReason, setVoidReason] = useState('')
  const [actionError, setActionError] = useState('')

  const bills = useQuery({
    queryKey: ['operations-bills', page, pageSize, filters],
    queryFn: () => operationsApi.queryBills({ page, size: pageSize, ...filters }),
  })
  const billDetail = useQuery({
    queryKey: ['operations-bill', selectedBillId],
    queryFn: () => operationsApi.getBill(selectedBillId!),
    enabled: selectedBillId !== undefined,
  })
  const billItems = useQuery({
    queryKey: ['operations-bill-items', selectedBillId],
    queryFn: () => operationsApi.listBillItems(selectedBillId!),
    enabled: selectedBillId !== undefined,
  })
  const transactions = useQuery({
    queryKey: ['operations-bill-transactions', selectedBillId],
    queryFn: () => operationsApi.listTransactions(selectedBillId!),
    enabled: selectedBillId !== undefined,
  })
  const refreshAfterBillAction = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['operations-bills'] }),
      queryClient.invalidateQueries({ queryKey: ['operations-bill', selectedBillId] }),
      queryClient.invalidateQueries({ queryKey: ['operations-bill-transactions', selectedBillId] }),
      queryClient.invalidateQueries({ queryKey: ['registrations'] }),
    ])
  }
  const paymentMutation = useMutation({
    mutationFn: ({ id, request }: { id: number; request: BillPaymentRequest }) => operationsApi.payBill(id, request),
    onSuccess: refreshAfterBillAction,
  })
  const refundMutation = useMutation({
    mutationFn: ({ id, request }: { id: number; request: BillRefundRequest }) => operationsApi.refundBill(id, request),
    onSuccess: refreshAfterBillAction,
  })
  const voidMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => operationsApi.voidBill(id, { reason }),
    onSuccess: refreshAfterBillAction,
  })

  const applyFilters = async () => {
    const value = patientIdInput.trim()
    let patientId: number | undefined
    if (value) {
      if (/^\d+$/.test(value) && Number(value) > 0) patientId = Number(value)
      else {
        try {
          const result = await patientApi.query({ page: 1, size: 2, keyword: value })
          if (result.total !== 1 || !result.records[0]) { setFilterError('未找到唯一患者，请输入数字 ID、EMPI、姓名或手机号。'); return }
          patientId = result.records[0].id
        } catch { setFilterError('患者查询失败，请检查患者服务连接。'); return }
      }
    }
    setFilterError('')
    setPage(1)
    setFilters((current) => ({
      ...current,
      patientId,
    }))
  }
  const resetFilters = () => {
    setPatientIdInput('')
    setFilterError('')
    setPage(1)
    setFilters({ billStatus: 'UNSETTLED' })
  }
  const openPayment = () => {
    if (!billDetail.data) return
    setActionError('')
    setPaymentAmount(moneyDue(billDetail.data.payableAmount, billDetail.data.paidAmount))
    setPaymentMethod('CASH')
    setPaymentReference('')
    setPaymentKey(newIdempotencyKey())
    setPaymentOpen(true)
  }
  const confirmPayment = async () => {
    if (selectedBillId === undefined || !billDetail.data || !validPayment(paymentAmount, moneyDue(billDetail.data.payableAmount, billDetail.data.paidAmount))) {
      setActionError('请输入大于 0、不超过待收余额的金额，最多四位小数。')
      return
    }
    setActionError('')
    try {
      await paymentMutation.mutateAsync({
        id: selectedBillId,
        request: { amount: paymentAmount, payMethod: paymentMethod, referenceNo: paymentReference.trim() || undefined, idempotencyKey: paymentKey },
      })
      setPaymentOpen(false)
    } catch {
      setActionError('收款未完成，请核对应收金额、账单状态或网络连接后重试。')
    }
  }
  const openRefund = () => {
    if (!billDetail.data) return
    setActionError('')
    setRefundAmount(moneyAmount(billDetail.data.paidAmount))
    setRefundReason('')
    setRefundKey(newIdempotencyKey())
    setRefundOpen(true)
  }
  const confirmRefund = async () => {
    if (selectedBillId === undefined || !billDetail.data || !validPayment(refundAmount, billDetail.data.paidAmount) || !refundReason.trim()) {
      setActionError('请填写有效退费金额和退费原因。')
      return
    }
    setActionError('')
    try {
      await refundMutation.mutateAsync({
        id: selectedBillId,
        request: { amount: refundAmount, reason: refundReason.trim(), idempotencyKey: refundKey },
      })
      setRefundOpen(false)
    } catch {
      setActionError('退费未完成，请核对已收余额、账单状态或网络连接后重试。')
    }
  }
  const confirmVoid = async () => {
    if (selectedBillId === undefined || !voidReason.trim()) {
      setActionError('请填写作废原因。')
      return
    }
    setActionError('')
    try {
      await voidMutation.mutateAsync({ id: selectedBillId, reason: voidReason.trim() })
      setVoidOpen(false)
    } catch {
      setActionError('作废未完成；仅未收款、未作废账单可以作废。')
    }
  }

  const rows = bills.data?.records || []
  const pageDue = moneySum(rows.filter(bill => bill.billStatus !== 'CANCELLED').map(bill => moneyDue(bill.payableAmount, bill.paidAmount)))
  const pagePaid = moneySum(rows.map(bill => bill.paidAmount))
  const columns: TableColumnsType<Bill> = [
    { title: '账单号', dataIndex: 'billNo', width: 180, render: (value) => <strong>{value}</strong> },
    { title: '患者编号', dataIndex: 'patientId', width: 105 },
    { title: '就诊类型', dataIndex: 'visitType', width: 100, render: (value) => visitTypeLabel[value as VisitType] || '—' },
    { title: '费用合计', dataIndex: 'totalAmount', width: 110, align: 'right', render: money },
    { title: '应收金额', dataIndex: 'payableAmount', width: 110, align: 'right', render: money },
    { title: '已收金额', dataIndex: 'paidAmount', width: 110, align: 'right', render: money },
    { title: '待收金额', key: 'due', width: 110, align: 'right', render: (_, row) => money(row.billStatus === 'CANCELLED' ? '0' : moneyDue(row.payableAmount, row.paidAmount)) },
    { title: '状态', dataIndex: 'billStatus', width: 105, render: (value: BillStatus) => { const status = billStatusMeta[value]; return <Tag color={status?.color}>{status?.label || value}</Tag> } },
    { title: '开单时间', dataIndex: 'createdTime', width: 155, render: (value) => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—' },
    { title: '操作', key: 'action', width: 90, fixed: 'right', render: (_, row) => <Button type="link" icon={<EyeOutlined />} onClick={() => setSelectedBillId(row.id)}>费用明细</Button> },
  ]
  const itemColumns: TableColumnsType<BillItem> = [
    { title: '序号', dataIndex: 'itemSeq', width: 65 },
    { title: '费用项目', dataIndex: 'itemName', width: 180 },
    { title: '费用来源', key: 'source', width: 125, render: (_, row) => <span>{feeClassLabel[row.itemClass || ''] || row.itemClass || '其他费用'}{row.orderId ? ` · 医嘱${row.orderId}` : row.feeItemId ? ` · 项目${row.feeItemId}` : ''}</span> },
    { title: '单价', dataIndex: 'unitPrice', width: 100, align: 'right', render: money },
    { title: '数量', dataIndex: 'quantity', width: 90, align: 'right' },
    { title: '单位', dataIndex: 'unit', width: 70, render: (value) => value || '—' },
    { title: '金额', dataIndex: 'amount', width: 110, align: 'right', render: money },
    { title: '项目状态', dataIndex: 'itemStatus', width: 100, render: (value, row) => row.isRefunded === 1 ? <Tag>已退费</Tag> : value || '正常' },
  ]
  const transactionColumns: TableColumnsType<BillTransaction> = [
    { title: '交易号', dataIndex: 'transactionNo', width: 180 },
    { title: '类型', dataIndex: 'transactionType', width: 80, render: (value: string) => <Tag color={value === 'REFUND' ? 'volcano' : 'green'}>{transactionTypeLabel[value] || value}</Tag> },
    { title: '金额', dataIndex: 'amount', width: 110, align: 'right', render: money },
    { title: '方式', dataIndex: 'payMethod', width: 90, render: (value) => payMethodLabel[value || ''] || '—' },
    { title: '参考号/原因', key: 'reference', render: (_, row) => row.reason || row.referenceNo || '—' },
    { title: '时间', dataIndex: 'transactionTime', width: 155, render: (value) => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—' },
  ]

  return <>
    <div className="page-heading patient-heading">
      <div><h1>收费与账单</h1><p>查询患者账单、应收状态及费用项目明细。</p></div>
      <Button icon={<ReloadOutlined />} onClick={() => bills.refetch()}>刷新账单</Button>
    </div>
    <section className="metric-grid operations-metrics" aria-label="当前页收费汇总">
      <Card size="small" className="metric-card"><div className="metric-label">当前页账单</div><div className="metric-value">{bills.data?.total ?? '—'}<small> 笔</small></div><div className="metric-note">按当前筛选条件</div></Card>
      <Card size="small" className="metric-card"><Statistic title="当前页待收" value={pageDue} formatter={() => money(pageDue)} /></Card>
      <Card size="small" className="metric-card"><Statistic title="当前页已收" value={pagePaid} formatter={() => money(pagePaid)} /></Card>
      <Card size="small" className="metric-card"><div className="metric-label">默认范围</div><div className="metric-value operations-default">待缴账单</div><div className="metric-note">可切换查看其他状态</div></Card>
    </section>
    <Card className="patient-table-card" title="账单查询" extra={<Space wrap className="operations-filters">
      <Input aria-label="患者编号" value={patientIdInput} onChange={(event) => setPatientIdInput(event.target.value)} onPressEnter={applyFilters} placeholder="数字 ID / EMPI / 姓名" style={{ width: 180 }} />
      <Select aria-label="账单状态" value={filters.billStatus} allowClear placeholder="全部账单状态" style={{ width: 130 }} options={Object.entries(billStatusMeta).map(([value, status]) => ({ value, label: status.label }))} onChange={(value) => { setPage(1); setFilters((current) => ({ ...current, billStatus: value })) }} />
      <Select aria-label="就诊类型" value={filters.visitType} allowClear placeholder="全部就诊类型" style={{ width: 120 }} options={Object.entries(visitTypeLabel).map(([value, label]) => ({ value, label }))} onChange={(value) => { setPage(1); setFilters((current) => ({ ...current, visitType: value })) }} />
      <Button type="primary" icon={<SearchOutlined />} onClick={applyFilters}>查询</Button>
      <Button onClick={resetFilters}>重置</Button>
    </Space>}>
      {filterError && <Alert type="warning" showIcon message={filterError} style={{ marginBottom: 8 }} />}
      {bills.isError && <Alert type="error" showIcon message="账单加载失败" description="请确认已登录，并检查网关和运营服务连接。" style={{ marginBottom: 8 }} />}
      <Table<Bill> rowKey="id" size="small" columns={columns} dataSource={rows} loading={bills.isLoading} scroll={{ x: 1160 }} locale={{ emptyText: '当前条件下没有账单' }} pagination={{ current: page, pageSize, total: bills.data?.total || 0, showSizeChanger: true, showTotal: (total) => `共 ${total} 笔账单`, onChange: (nextPage, nextSize) => { setPage(nextPage); setPageSize(nextSize) } }} />
    </Card>
    <Drawer title="账单费用明细" width={820} open={selectedBillId !== undefined} onClose={() => setSelectedBillId(undefined)} extra={billDetail.data && <Space wrap>
      <Tag color={billStatusMeta[billDetail.data.billStatus]?.color}>{billStatusMeta[billDetail.data.billStatus]?.label || billDetail.data.billStatus}</Tag>
      {['UNSETTLED', 'PARTIAL'].includes(billDetail.data.billStatus) && <Button type="primary" onClick={openPayment}>登记收款</Button>}
      {moneyUnits(billDetail.data.paidAmount) > 0n && billDetail.data.billStatus !== 'CANCELLED' && <Button danger onClick={openRefund}>登记退费</Button>}
      {['UNSETTLED', 'SETTLED'].includes(billDetail.data.billStatus) && moneyUnits(billDetail.data.paidAmount) === 0n && <Button danger onClick={() => { setActionError(''); setVoidReason(''); setVoidOpen(true) }}>作废账单</Button>}
    </Space>}>
      {billDetail.isError && <Alert type="error" showIcon message="账单信息读取失败" />}
      {actionError && !paymentOpen && !refundOpen && !voidOpen && <Alert type="error" showIcon message={actionError} style={{ marginBottom: 8 }} />}
      {billDetail.data && <Descriptions bordered size="small" column={2} items={[
        { key: 'billNo', label: '账单号', children: billDetail.data.billNo },
        { key: 'patientId', label: '患者编号', children: billDetail.data.patientId },
        { key: 'source', label: '账单来源', children: billDetail.data.sourceType === 'REGISTRATION' ? `挂号 ${billDetail.data.sourceId}` : billDetail.data.sourceType === 'ORDER' ? `医嘱 ${billDetail.data.sourceId}` : '窗口建账' },
        { key: 'visitType', label: '就诊类型', children: visitTypeLabel[billDetail.data.visitType as VisitType] || '—' },
        { key: 'createdTime', label: '开单时间', children: billDetail.data.createdTime ? dayjs(billDetail.data.createdTime).format('YYYY-MM-DD HH:mm') : '—' },
        { key: 'payableAmount', label: '应收金额', children: money(billDetail.data.payableAmount) },
        { key: 'paidAmount', label: '已收金额', children: money(billDetail.data.paidAmount) },
        ...(billDetail.data.voidReason ? [{ key: 'voidReason', label: '作废原因', children: billDetail.data.voidReason }] : []),
      ]} />}
      <h3 className="operations-detail-heading">费用项目</h3>
      {billItems.isError && <Alert type="error" showIcon message="费用项目加载失败" description="请刷新后重试。" />}
      {!billItems.isError && <Table<BillItem> rowKey="id" size="small" columns={itemColumns} dataSource={billItems.data || []} loading={billItems.isLoading} pagination={false} scroll={{ x: 780 }} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="该账单暂无费用项目" /> }} />}
      <h3 className="operations-detail-heading">收退款记录</h3>
      {transactions.isError && <Alert type="error" showIcon message="交易记录加载失败" />}
      {!transactions.isError && <Table<BillTransaction> rowKey="id" size="small" columns={transactionColumns} dataSource={transactions.data || []} loading={transactions.isLoading} pagination={false} scroll={{ x: 760 }} locale={{ emptyText: '暂无收退款记录' }} />}
    </Drawer>
    <Modal title="登记收款" open={paymentOpen} onCancel={() => setPaymentOpen(false)} onOk={confirmPayment} okText="确认收款" cancelText="返回" confirmLoading={paymentMutation.isPending}>
      {actionError && <Alert type="error" showIcon message={actionError} style={{ marginBottom: 12 }} />}
      <p>此操作登记线下收款并更新账单余额，不会调用第三方支付平台。</p>
      <div className="operations-action-form">
        <label>收款金额</label><InputNumber stringMode min="0.0001" step="0.01" precision={4} value={paymentAmount} onChange={(value) => setPaymentAmount(value == null ? '' : String(value))} style={{ width: '100%' }} />
        <label>收款方式</label><Select value={paymentMethod} onChange={setPaymentMethod} options={Object.entries(payMethodLabel).map(([value, label]) => ({ value, label }))} />
        <label>凭证/参考号</label><Input maxLength={64} value={paymentReference} onChange={(event) => setPaymentReference(event.target.value)} />
      </div>
    </Modal>
    <Modal title="登记退费" open={refundOpen} onCancel={() => setRefundOpen(false)} onOk={confirmRefund} okText="确认退费" cancelText="返回" okButtonProps={{ danger: true }} confirmLoading={refundMutation.isPending}>
      {actionError && <Alert type="error" showIcon message={actionError} style={{ marginBottom: 12 }} />}
      <Alert type="warning" showIcon message={`当前已收 ${money(billDetail.data?.paidAmount)}，退费金额不能超过已收余额。`} style={{ marginBottom: 12 }} />
      <div className="operations-action-form">
        <label>退费金额</label><InputNumber stringMode min="0.0001" step="0.01" precision={4} value={refundAmount} onChange={(value) => setRefundAmount(value == null ? '' : String(value))} style={{ width: '100%' }} />
        <label>退费原因</label><Input maxLength={256} value={refundReason} onChange={(event) => setRefundReason(event.target.value)} />
      </div>
    </Modal>
    <Modal title="作废账单" open={voidOpen} onCancel={() => setVoidOpen(false)} onOk={confirmVoid} okText="确认作废" cancelText="返回" okButtonProps={{ danger: true }} confirmLoading={voidMutation.isPending}>
      {actionError && <Alert type="error" showIcon message={actionError} style={{ marginBottom: 12 }} />}
      <Alert type="warning" showIcon message="仅未收款账单可以作废；作废后不能收款。" style={{ marginBottom: 12 }} />
      <label htmlFor="bill-void-reason">作废原因</label>
      <Input id="bill-void-reason" maxLength={240} value={voidReason} onChange={(event) => setVoidReason(event.target.value)} />
    </Modal>
  </>
}
