import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Input, InputNumber, Modal, Select, Space, Table, Tabs } from 'antd'
import type { TableColumnsType } from 'antd'
import dayjs from 'dayjs'
import { useState } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { operationsApi } from '@/services/operationsApi'
import type { AccountRecord, SettlementRecord } from '@/types/operations'

const money = (value?: number) => `¥${(value ?? 0).toFixed(2)}`
const accountStatus: Record<AccountRecord['accountStatus'], string> = { PENDING: '待交班', SUBMITTED: '已提交', RECEIVED: '已接收', CANCELLED: '已取消' }

export function FinanceOperationsTabs() {
  const session = useAuthStore((state) => state.session)
  const queryClient = useQueryClient()
  const [page, setPage] = useState(1)
  const [admissionId, setAdmissionId] = useState('')
  const [preview, setPreview] = useState<{ patientId: number; admissionId: number; totalAmount: number; insuranceAmount: number; depositAmount: number; selfPayAmount: number; depositBalance: number }>()
  const [payMethod, setPayMethod] = useState('CASH')
  const [receiverId, setReceiverId] = useState('')
  const [error, setError] = useState('')
  const cashierId = session ? String(session.userId) : ''
  const today = dayjs().format('YYYY-MM-DD')
  const accounts = useQuery({ queryKey: ['operations-accounts', cashierId], queryFn: () => operationsApi.queryAccounts({ page: 1, size: 20, cashierId }), enabled: Boolean(session) })
  const settlements = useQuery({ queryKey: ['operations-settlements', page], queryFn: () => operationsApi.querySettlements({ page, size: 20 }) })
  const refreshAccounts = () => queryClient.invalidateQueries({ queryKey: ['operations-accounts'] })
  const hasTodayAccount = accounts.data?.records.some((account) => account.accountDate === today)
  const generate = useMutation({ mutationFn: () => operationsApi.generateAccount(cashierId, session?.realName || session?.username || cashierId, today), onSuccess: refreshAccounts, onError: () => setError('日结生成失败，请检查运营服务连接或当日是否已生成。') })
  const submit = useMutation({ mutationFn: (id: number) => operationsApi.submitAccount(id), onSuccess: refreshAccounts, onError: () => setError('交班提交失败，请刷新后确认日账状态。') })
  const receive = useMutation({ mutationFn: (id: number) => operationsApi.receiveAccount(id, receiverId.trim()), onSuccess: refreshAccounts, onError: () => setError('交接确认失败，请核对接收人工号及日账状态。') })
  const previewMutation = useMutation({ mutationFn: async () => {
    const admission = Number(admissionId)
    if (!Number.isSafeInteger(admission) || admission < 1) throw new Error('请输入有效的住院号。')
    const result = await operationsApi.previewSettlement(admission)
    if (!result.patientId) throw new Error('未能从未结账单中识别患者，请核对住院号和账单状态。')
    setPreview({ ...result, admissionId: admission })
  }, onError: (cause) => setError(cause instanceof Error ? cause.message : '结算预览失败，请核对住院信息。') })
  const createSettlement = useMutation({ mutationFn: () => operationsApi.createSettlement({ patientId: preview!.patientId, admissionId: preview!.admissionId, settleType: 'FINAL', payMethod, cashierId, cashierName: session?.realName || session?.username || cashierId }), onSuccess: async () => { setPreview(undefined); setError(''); await queryClient.invalidateQueries({ queryKey: ['operations-settlements'] }) }, onError: () => setError('结算失败，请刷新账单和预览金额后重试。') })

  const accountColumns: TableColumnsType<AccountRecord> = [
    { title: '日账单号', dataIndex: 'accountNo', width: 175 }, { title: '收费员', dataIndex: 'cashierName', width: 100 },
    { title: '日期', dataIndex: 'accountDate', width: 110 }, { title: '结算笔数', dataIndex: 'billCount', width: 85, align: 'right' },
    { title: '现金', dataIndex: 'cashAmount', width: 100, align: 'right', render: money }, { title: '银行卡', dataIndex: 'posAmount', width: 100, align: 'right', render: money },
    { title: '其他', dataIndex: 'otherAmount', width: 100, align: 'right', render: money }, { title: '合计', dataIndex: 'totalAmount', width: 110, align: 'right', render: money },
    { title: '状态', dataIndex: 'accountStatus', width: 90, render: (value: AccountRecord['accountStatus']) => accountStatus[value] || value },
    { title: '交班/交接', key: 'action', width: 160, render: (_, row) => row.accountStatus === 'PENDING' ? <Button type="link" onClick={() => submit.mutate(row.id)}>提交交班</Button> : row.accountStatus === 'SUBMITTED' ? <Button type="link" disabled={!receiverId.trim()} onClick={() => receive.mutate(row.id)}>确认接收</Button> : row.receiveStatus === 'RECEIVED' ? `接收人：${row.receiverId || '—'}` : '—' },
  ]
  const settlementColumns: TableColumnsType<SettlementRecord> = [
    { title: '结算单号', dataIndex: 'settleNo', width: 175 }, { title: '患者编号', dataIndex: 'patientId', width: 95 },
    { title: '住院号', dataIndex: 'admissionId', width: 95 }, { title: '费用合计', dataIndex: 'totalAmount', width: 105, align: 'right', render: money },
    { title: '医保支付', dataIndex: 'insuranceAmount', width: 105, align: 'right', render: money }, { title: '预交金抵扣', dataIndex: 'depositAmount', width: 105, align: 'right', render: money },
    { title: '个人支付', dataIndex: 'selfPayAmount', width: 105, align: 'right', render: money }, { title: '结算时间', dataIndex: 'settleTime', width: 145, render: (value) => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—' },
    { title: '状态', dataIndex: 'settleStatus', width: 90 },
  ]

  return <>
    <Tabs items={[
    { key: 'accounts', label: '日结交接', children: <Card size="small" title="收费员日结与交班" extra={<Space><Input aria-label="接收人工号" placeholder="接收人工号" value={receiverId} onChange={(event) => setReceiverId(event.target.value)} style={{ width: 145 }} /><Button type="primary" loading={generate.isPending} disabled={accounts.isLoading || hasTodayAccount} onClick={() => { setError(''); generate.mutate() }}>生成今日日账</Button></Space>}>
      <Alert type="info" showIcon message="日账按当前收费员、当日已完成的住院结算汇总；提交后由接收人确认交接。生成前请核对列表，避免重复生成。" style={{ marginBottom: 10 }} />
      {error && !preview && <Alert type="error" showIcon message={error} style={{ marginBottom: 10 }} />}
      <Table<AccountRecord> rowKey="id" size="small" columns={accountColumns} dataSource={accounts.data?.records || []} loading={accounts.isLoading || submit.isPending || receive.isPending} pagination={false} scroll={{ x: 1120 }} locale={{ emptyText: '当前收费员暂无日账' }} />
    </Card> },
    { key: 'settlements', label: '住院结算' , children: <Card size="small" title="住院费用结算" extra={<Space><InputNumber aria-label="住院号" min={1} precision={0} value={admissionId ? Number(admissionId) : null} onChange={(value) => setAdmissionId(value == null ? '' : String(value))} placeholder="住院号" /><Button type="primary" loading={previewMutation.isPending} onClick={() => { setError(''); previewMutation.mutate() }}>费用预览并结算</Button></Space>}>
      <Alert type="warning" showIcon message="此处使用现有住院结算接口。请先核对住院号和费用预览；确认后会结清该住院未缴账单。" style={{ marginBottom: 10 }} />
      {error && !preview && <Alert type="error" showIcon message={error} style={{ marginBottom: 10 }} />}
      <Table<SettlementRecord> rowKey="id" size="small" columns={settlementColumns} dataSource={settlements.data?.records || []} loading={settlements.isLoading || createSettlement.isPending} scroll={{ x: 1050 }} pagination={{ current: page, pageSize: 20, total: settlements.data?.total || 0, showTotal: (total) => `共 ${total} 笔结算`, onChange: setPage }} locale={{ emptyText: '暂无住院结算记录' }} />
    </Card> },
  ]} />
    <Modal title="住院结算确认" open={Boolean(preview)} onCancel={() => { setPreview(undefined); setError('') }} onOk={() => createSettlement.mutate()} confirmLoading={createSettlement.isPending} okText="确认结算" cancelText="返回核对">
      {preview && <><Alert type="warning" showIcon message={`患者 ${preview.patientId}，住院号 ${preview.admissionId}；结算后未结账单将更新为已结清。`} style={{ marginBottom: 12 }} />{error && <Alert type="error" message={error} style={{ marginBottom: 12 }} />}<p>费用合计：{money(preview.totalAmount)}</p><p>医保支付：{money(preview.insuranceAmount)}</p><p>预交金抵扣：{money(preview.depositAmount)}（可用余额 {money(preview.depositBalance)}）</p><p>个人支付：<strong>{money(preview.selfPayAmount)}</strong></p><Select aria-label="结算支付方式" value={payMethod} onChange={setPayMethod} options={[{ value: 'CASH', label: '现金' }, { value: 'POS', label: '银行卡' }, { value: 'WECHAT', label: '微信' }, { value: 'ALIPAY', label: '支付宝' }]} style={{ width: '100%' }} /></>}
    </Modal>
  </>
}
