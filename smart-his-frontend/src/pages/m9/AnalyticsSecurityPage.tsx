import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, DatePicker, Input, Modal, Space, Statistic, Table, Tabs, Tag } from 'antd'
import dayjs from 'dayjs'
import { m9Api, type ReportQuery } from '@/services/m9Api'

export function AnalyticsSecurityPage() {
  const client = useQueryClient()
  const [query, setQuery] = useState<ReportQuery>({ from: dayjs().subtract(29, 'day').format('YYYY-MM-DD'), to: dayjs().format('YYYY-MM-DD') })
  const [draft, setDraft] = useState(query)
  const [filterName, setFilterName] = useState('')
  const [saveOpen, setSaveOpen] = useState(false)
  const [error, setError] = useState('')
  const summary = useQuery({ queryKey: ['m9-summary', query], queryFn: () => m9Api.summary(query) })
  const departments = useQuery({ queryKey: ['m9-departments', query], queryFn: () => m9Api.departments(query) })
  const doctors = useQuery({ queryKey: ['m9-doctors', query], queryFn: () => m9Api.doctors(query) })
  const filters = useQuery({ queryKey: ['m9-filters'], queryFn: m9Api.filters })
  const logs = useQuery({ queryKey: ['m9-audit'], queryFn: () => m9Api.operationLogs(), retry: false })
  const saveFilter = useMutation({ mutationFn: () => m9Api.saveFilter(filterName, query), onSuccess: async () => { setSaveOpen(false); setFilterName(''); await client.invalidateQueries({ queryKey: ['m9-filters'] }) }, onError: () => setError('查询条件保存失败，名称可能重复。') })
  const applySaved = (config: string) => { try { const value = JSON.parse(config) as ReportQuery; setDraft(value); setQuery(value) } catch { setError('保存的查询条件格式无效。') } }
  const metric = summary.data
  return <>
    <div className="page-heading"><div><h1>运营分析与安全</h1><p>按授权科室统计业务指标，导出报表并复核关键操作审计。</p></div><Space><Button onClick={() => m9Api.exportReport('xls', query)}>导出 Excel</Button><Button onClick={() => m9Api.exportReport('pdf', query)}>导出 PDF</Button></Space></div>
    {error && <Alert type="error" showIcon closable message={error} onClose={() => setError('')} />}
    <Card size="small" title="查询条件" extra={<Button onClick={() => setSaveOpen(true)}>保存条件</Button>}><Space wrap><DatePicker.RangePicker value={draft.from && draft.to ? [dayjs(draft.from), dayjs(draft.to)] : undefined} onChange={dates => setDraft({ ...draft, from: dates?.[0]?.format('YYYY-MM-DD'), to: dates?.[1]?.format('YYYY-MM-DD') })} /><InputNumberField value={draft.deptId} onChange={deptId => setDraft({ ...draft, deptId })} /><Button type="primary" onClick={() => setQuery(draft)}>查询</Button>{filters.data?.map(filter => <Button key={filter.id} onClick={() => applySaved(filter.configJson)}>{filter.filterName}</Button>)}</Space></Card>
    <section className="metric-grid operations-metrics" style={{ marginTop: 12 }}><Card><Statistic title="门诊量" value={metric?.outpatientVisits || 0} suffix="人次" /></Card><Card><Statistic title="账单数" value={metric?.billCount || 0} suffix="笔" /></Card><Card><Statistic title="实收金额" value={Number(metric?.receivedAmount || 0)} precision={2} prefix="¥" /></Card><Card><Statistic title="退费金额" value={Number(metric?.refundedAmount || 0)} precision={2} prefix="¥" /></Card></section>
    <Tabs items={[
      { key: 'dept', label: '科室绩效', children: <Table rowKey="dept_id" loading={departments.isLoading} dataSource={departments.data || []} pagination={false} columns={[{ title: '科室 ID', dataIndex: 'dept_id' }, { title: '账单数', dataIndex: 'bill_count' }, { title: '应收', dataIndex: 'billed_amount' }, { title: '实收', dataIndex: 'received_amount' }]} /> },
      { key: 'doctor', label: '医生工作量', children: <Table rowKey={row => `${row.dept_id}-${row.doctor_id}`} loading={doctors.isLoading} dataSource={doctors.data || []} pagination={false} columns={[{ title: '医生 ID', dataIndex: 'doctor_id' }, { title: '科室 ID', dataIndex: 'dept_id' }, { title: '接诊量', dataIndex: 'visit_count' }]} /> },
      { key: 'audit', label: '操作审计', children: logs.isError ? <Alert type="info" message="当前账号没有审计日志查看权限。" /> : <Table rowKey="id" loading={logs.isLoading} dataSource={logs.data?.records || []} columns={[{ title: '时间', dataIndex: 'operationTime', render: value => dayjs(value).format('YYYY-MM-DD HH:mm:ss') }, { title: '用户', dataIndex: 'username' }, { title: '操作', dataIndex: 'operationType' }, { title: '结果', dataIndex: 'responseCode', render: value => <Tag color={value === 200 ? 'green' : 'red'}>{value === 200 ? '成功' : `失败 ${value || ''}`}</Tag> }, { title: '说明', dataIndex: 'description' }]} /> },
    ]} />
    <Modal title="保存查询条件" open={saveOpen} onCancel={() => setSaveOpen(false)} onOk={() => saveFilter.mutate()} confirmLoading={saveFilter.isPending}><Input value={filterName} onChange={event => setFilterName(event.target.value)} placeholder="例如：本月内科" /></Modal>
  </>
}

function InputNumberField({ value, onChange }: { value?: number; onChange: (value?: number) => void }) {
  return <Input value={value == null ? '' : String(value)} onChange={event => onChange(/^\d+$/.test(event.target.value) ? Number(event.target.value) : undefined)} placeholder="科室 ID（管理员可选）" style={{ width: 190 }} />
}
