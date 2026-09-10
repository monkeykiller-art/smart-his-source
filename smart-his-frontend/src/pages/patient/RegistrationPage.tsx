import { CalendarOutlined, PlusOutlined, ReloadOutlined, StopOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, DatePicker, Form, Modal, Popconfirm, Select, Statistic, Table, Tag, message } from 'antd'
import type { TableColumnsType } from 'antd'
import dayjs from 'dayjs'
import { useState } from 'react'
import { patientApi } from '@/services/patientApi'
import { registrationApi } from '@/services/registrationApi'
import type { Registration, RegistrationCreateRequest, Schedule } from '@/types/registration'

const periodText: Record<string, string> = { MORNING: '上午', AFTERNOON: '下午', EVENING: '晚间', ALL_DAY: '全天' }
const registrationStatus: Record<string, { text: string; color: string }> = {
  ACTIVE: { text: '有效', color: 'success' },
  CANCELLED: { text: '已取消', color: 'default' },
  COMPLETED: { text: '已完成', color: 'blue' },
}
const paymentStatus: Record<string, { text: string; color: string }> = {
  UNPAID: { text: '待缴费', color: 'warning' },
  PAID: { text: '已缴费', color: 'success' },
  REFUNDED: { text: '已退费', color: 'default' },
}

export function RegistrationPage() {
  const today = dayjs().format('YYYY-MM-DD')
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)
  const [date, setDate] = useState(today)
  const [deptId, setDeptId] = useState<number>()
  const [doctorId, setDoctorId] = useState<number>()
  const [status, setStatus] = useState<string>()
  const [createOpen, setCreateOpen] = useState(false)
  const [form] = Form.useForm<RegistrationCreateRequest>()
  const [messageApi, messageContext] = message.useMessage()
  const queryClient = useQueryClient()

  const departments = useQuery({ queryKey: ['departments'], queryFn: registrationApi.listDepartments })
  const doctors = useQuery({ queryKey: ['doctors', deptId], queryFn: () => registrationApi.listDoctors(deptId) })
  const schedules = useQuery({
    queryKey: ['schedules', date, deptId, doctorId],
    queryFn: () => registrationApi.querySchedules({ page: 1, size: 100, scheduleDate: date, deptId, doctorId }),
  })
  const registrations = useQuery({
    queryKey: ['registrations', page, size, date, deptId, doctorId, status],
    queryFn: () => registrationApi.queryRegistrations({ page, size, regDate: date, deptId, doctorId, regStatus: status }),
  })
  const patients = useQuery({
    queryKey: ['patients', 'registration-options'],
    queryFn: () => patientApi.query({ page: 1, size: 100 }),
    enabled: createOpen,
  })

  const refreshRegistrationData = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['registrations'] }),
      queryClient.invalidateQueries({ queryKey: ['schedules'] }),
    ])
  }
  const createRegistration = useMutation({
    mutationFn: registrationApi.create,
    onSuccess: async (result) => {
      messageApi.success(`挂号成功，挂号单号 ${result.regNo}`)
      setCreateOpen(false)
      form.resetFields()
      await refreshRegistrationData()
    },
    onError: () => messageApi.error('挂号失败，请检查号源余量、患者状态和服务连接。'),
  })
  const cancelRegistration = useMutation({
    mutationFn: (id: number) => registrationApi.cancel(id, '窗口取消'),
    onSuccess: async () => { messageApi.success('挂号已取消'); await refreshRegistrationData() },
    onError: () => messageApi.error('取消失败，该挂号可能已经接诊或取消。'),
  })

  const scheduleRows = schedules.data?.records || []
  const availableSchedules = scheduleRows.filter((item) => item.scheduleStatus === 'ACTIVE' && item.availableQuota > 0)
  const totalAvailable = scheduleRows.reduce((sum, item) => sum + Math.max(item.availableQuota, 0), 0)

  const scheduleColumns: TableColumnsType<Schedule> = [
    { title: '科室', dataIndex: 'deptName', width: 140 },
    { title: '医生', dataIndex: 'doctorName', width: 110 },
    { title: '职级', dataIndex: 'regLevel', width: 100, render: (value) => value || '普通号' },
    { title: '时段', dataIndex: 'timePeriod', width: 90, render: (value, row) => `${periodText[value] || value}${row.startTime ? ` ${row.startTime.slice(0, 5)}` : ''}` },
    { title: '号源', key: 'quota', width: 110, render: (_, row) => <span><strong>{row.availableQuota}</strong> / {row.totalQuota}</span> },
    { title: '挂号费', dataIndex: 'regFee', width: 90, render: (value) => `¥${Number(value).toFixed(2)}` },
    { title: '状态', dataIndex: 'scheduleStatus', width: 90, render: (value, row) => <Tag color={value === 'ACTIVE' && row.availableQuota > 0 ? 'success' : 'default'}>{value === 'ACTIVE' ? (row.availableQuota > 0 ? '可挂号' : '已满') : '已停诊'}</Tag> },
  ]
  const registrationColumns: TableColumnsType<Registration> = [
    { title: '挂号单号', dataIndex: 'regNo', width: 165, render: (value) => <strong>{value}</strong> },
    { title: '患者', dataIndex: 'patientName', width: 100 },
    { title: '科室', dataIndex: 'deptName', width: 130 },
    { title: '医生', dataIndex: 'doctorName', width: 100 },
    { title: '就诊序号', dataIndex: 'visitSeq', width: 100, render: (value) => `第 ${value} 号` },
    { title: '日期/时段', key: 'period', width: 150, render: (_, row) => `${row.regDate} ${periodText[row.timePeriod] || row.timePeriod}` },
    { title: '费用', dataIndex: 'regFee', width: 80, render: (value) => `¥${Number(value).toFixed(2)}` },
    { title: '缴费', dataIndex: 'payStatus', width: 90, render: (value) => { const item = paymentStatus[value]; return <Tag color={item?.color}>{item?.text || value}</Tag> } },
    { title: '状态', dataIndex: 'regStatus', width: 90, render: (value) => { const item = registrationStatus[value]; return <Tag color={item?.color}>{item?.text || value}</Tag> } },
    { title: '操作', key: 'action', width: 90, fixed: 'right', render: (_, row) => row.regStatus === 'ACTIVE' ? <Popconfirm title="确认取消挂号？" description="取消后号源将被释放。" okText="确认" cancelText="返回" onConfirm={() => cancelRegistration.mutate(row.id)}><Button danger type="link" icon={<StopOutlined />}>取消</Button></Popconfirm> : '—' },
  ]

  const filterBar = <div className="registration-filters">
    <DatePicker allowClear={false} value={dayjs(date)} onChange={(value) => { setPage(1); setDate(value.format('YYYY-MM-DD')) }} />
    <Select allowClear value={deptId} placeholder="全部科室" loading={departments.isLoading} options={(departments.data || []).map((item) => ({ value: item.id, label: item.deptName }))} onChange={(value) => { setPage(1); setDeptId(value); setDoctorId(undefined) }} />
    <Select allowClear value={doctorId} placeholder="全部医生" loading={doctors.isLoading} options={(doctors.data || []).map((item) => ({ value: item.id, label: `${item.doctorName}${item.title ? ` · ${item.title}` : ''}` }))} onChange={(value) => { setPage(1); setDoctorId(value) }} />
    <Select allowClear value={status} placeholder="全部挂号状态" options={Object.entries(registrationStatus).map(([value, item]) => ({ value, label: item.text }))} onChange={(value) => { setPage(1); setStatus(value) }} />
    <Button icon={<ReloadOutlined />} onClick={() => refreshRegistrationData()}>刷新</Button>
  </div>

  return <>
    {messageContext}
    <div className="page-heading patient-heading"><div><h1>门诊挂号</h1><p>按科室和医生查看当日号源，完成患者挂号及取消。</p></div><Button type="primary" icon={<PlusOutlined />} disabled={availableSchedules.length === 0} onClick={() => setCreateOpen(true)}>新建挂号</Button></div>
    <div className="registration-metrics">
      <Card><Statistic title="开放排班" value={scheduleRows.filter((item) => item.scheduleStatus === 'ACTIVE').length} suffix="个" prefix={<CalendarOutlined />} /></Card>
      <Card><Statistic title="剩余号源" value={totalAvailable} suffix="个" /></Card>
      <Card><Statistic title="当日挂号" value={registrations.data?.total || 0} suffix="人次" /></Card>
    </div>
    <Card className="patient-table-card" title="排班与号源" extra={filterBar}>
      {schedules.isError && <Alert type="error" showIcon message="排班加载失败" description="请确认患者服务已经启动。" style={{ marginBottom: 16 }} />}
      <Table<Schedule> rowKey="id" size="middle" columns={scheduleColumns} dataSource={scheduleRows} loading={schedules.isLoading} pagination={false} scroll={{ x: 800 }} locale={{ emptyText: '当前筛选条件下没有排班' }} />
    </Card>
    <Card className="patient-table-card registration-list" title="挂号记录">
      {registrations.isError && <Alert type="error" showIcon message="挂号记录加载失败" description="请检查网关及患者服务状态。" style={{ marginBottom: 16 }} />}
      <Table<Registration> rowKey="id" columns={registrationColumns} dataSource={registrations.data?.records || []} loading={registrations.isLoading} scroll={{ x: 1160 }} pagination={{ current: page, pageSize: size, total: registrations.data?.total || 0, showSizeChanger: true, showTotal: (total) => `共 ${total} 条记录`, onChange: (nextPage, nextSize) => { setPage(nextPage); setSize(nextSize) } }} />
    </Card>

    <Modal title="新建门诊挂号" open={createOpen} onCancel={() => setCreateOpen(false)} onOk={() => form.submit()} okText="确认挂号" cancelText="取消" confirmLoading={createRegistration.isPending} destroyOnHidden>
      <Form<RegistrationCreateRequest> form={form} layout="vertical" initialValues={{ regSource: 'WINDOW' }} onFinish={(values) => createRegistration.mutate(values)}>
        <Form.Item name="patientId" label="患者" rules={[{ required: true, message: '请选择患者' }]}>
          <Select showSearch optionFilterProp="label" loading={patients.isLoading} placeholder="输入姓名或患者主索引筛选" options={(patients.data?.records || []).map((item) => ({ value: item.id, label: `${item.name} · ${item.empiNo}` }))} />
        </Form.Item>
        <Form.Item name="scheduleId" label="排班号源" rules={[{ required: true, message: '请选择排班号源' }]}>
          <Select showSearch optionFilterProp="label" placeholder="选择科室、医生和时段" options={availableSchedules.map((item) => ({ value: item.id, label: `${item.deptName} · ${item.doctorName} · ${periodText[item.timePeriod] || item.timePeriod} · 余 ${item.availableQuota} 号 · ¥${Number(item.regFee).toFixed(2)}` }))} />
        </Form.Item>
        <Form.Item name="regSource" label="挂号来源"><Select options={[{ value: 'WINDOW', label: '人工窗口' }, { value: 'SELF_SERVICE', label: '自助机' }, { value: 'ONLINE', label: '线上预约' }]} /></Form.Item>
        <Alert type="info" showIcon message="提交后系统将占用号源，并同步创建待缴费账单和待就诊记录。" />
      </Form>
    </Modal>
  </>
}
