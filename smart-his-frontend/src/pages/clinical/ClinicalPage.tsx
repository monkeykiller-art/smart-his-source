import { DeleteOutlined, FileDoneOutlined, MedicineBoxOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Checkbox, Descriptions, Empty, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, message } from 'antd'
import type { TableColumnsType } from 'antd'
import dayjs from 'dayjs'
import { useMemo, useState } from 'react'
import { clinicalApi } from '@/services/clinicalApi'
import { registrationApi } from '@/services/registrationApi'
import { useAuthStore } from '@/stores/authStore'
import type { ClinicalOrder, Diagnosis, MedicalRecord } from '@/types/clinical'

interface RecordFormValues {
  title?: string
  chiefComplaint: string
  presentIllness?: string
  diagnosisDesc?: string
  treatmentPlan?: string
}

interface DiagnosisFormValues {
  diagnosisName: string
  icdCode?: string
  diagnosisDesc?: string
  isPrimary?: boolean
}

interface OrderFormValues {
  orderType: string
  orderCategory: string
  itemName: string
  spec?: string
  dose?: number
  doseUnit?: string
  usageMethod?: string
  frequency?: string
  days?: number
  quantity?: number
  quantityUnit?: string
  unitPrice?: number
  remark?: string
}

const recordStatus: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'warning' },
  SIGNED: { text: '已签署', color: 'success' },
  ARCHIVED: { text: '已归档', color: 'default' },
}
const orderStatus: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'default' },
  PENDING: { text: '待审核', color: 'warning' },
  VERIFIED: { text: '已审核', color: 'processing' },
  EXECUTING: { text: '执行中', color: 'blue' },
  COMPLETED: { text: '已完成', color: 'success' },
  CANCELLED: { text: '已取消', color: 'default' },
  STOPPED: { text: '已停止', color: 'default' },
}
const orderTypeText: Record<string, string> = {
  MEDICINE: '药品', EXAM: '检查', LAB: '检验', TREATMENT: '治疗', SURGERY: '手术', NURSING: '护理', OTHER: '其他',
}

export function ClinicalPage() {
  const session = useAuthStore((state) => state.session)
  const queryClient = useQueryClient()
  const [requestedRegistrationId, setRequestedRegistrationId] = useState<number>()
  const [recordOpen, setRecordOpen] = useState(false)
  const [diagnosisOpen, setDiagnosisOpen] = useState(false)
  const [orderOpen, setOrderOpen] = useState(false)
  const [recordForm] = Form.useForm<RecordFormValues>()
  const [diagnosisForm] = Form.useForm<DiagnosisFormValues>()
  const [orderForm] = Form.useForm<OrderFormValues>()
  const [messageApi, messageContext] = message.useMessage()
  const today = dayjs().format('YYYY-MM-DD')

  const registrations = useQuery({
    queryKey: ['registrations', 'clinical', today, session?.deptId],
    queryFn: () => registrationApi.queryRegistrations({ page: 1, size: 100, regDate: today, deptId: session?.deptId }),
  })
  const registrationRows = useMemo(() => registrations.data?.records || [], [registrations.data])
  const selectedRegistrationId = registrationRows.some((item) => item.id === requestedRegistrationId)
    ? requestedRegistrationId
    : registrationRows[0]?.id
  const selectedRegistration = registrationRows.find((item) => item.id === selectedRegistrationId)
  const patientId = selectedRegistration?.patientId
  const encounterId = selectedRegistration?.encounterId

  const records = useQuery({
    queryKey: ['clinical-records', patientId],
    queryFn: () => clinicalApi.listRecords(patientId!),
    enabled: Boolean(patientId),
  })
  const diagnoses = useQuery({
    queryKey: ['clinical-diagnoses', encounterId],
    queryFn: () => clinicalApi.listDiagnoses(encounterId!),
    enabled: Boolean(encounterId),
  })
  const orders = useQuery({
    queryKey: ['clinical-orders', patientId],
    queryFn: () => clinicalApi.listOrders(patientId!),
    enabled: Boolean(patientId),
  })

  const refreshClinicalData = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['clinical-records'] }),
      queryClient.invalidateQueries({ queryKey: ['clinical-diagnoses'] }),
      queryClient.invalidateQueries({ queryKey: ['clinical-orders'] }),
    ])
  }
  const createRecord = useMutation({
    mutationFn: (values: RecordFormValues) => clinicalApi.createRecord({
      ...values,
      patientId: selectedRegistration!.patientId,
      encounterId: selectedRegistration!.encounterId,
      deptId: selectedRegistration!.deptId,
      doctorId: selectedRegistration!.doctorId || session!.userId,
      recordType: 'OUTPATIENT',
    }),
    onSuccess: async () => { messageApi.success('门诊病历已保存'); setRecordOpen(false); recordForm.resetFields(); await refreshClinicalData() },
    onError: () => messageApi.error('病历保存失败，请检查临床服务。'),
  })
  const signRecord = useMutation({
    mutationFn: clinicalApi.signRecord,
    onSuccess: async () => { messageApi.success('病历已签署'); await refreshClinicalData() },
    onError: () => messageApi.error('病历签署失败，请确认病历仍为草稿。'),
  })
  const createDiagnosis = useMutation({
    mutationFn: (values: DiagnosisFormValues) => clinicalApi.createDiagnosis({
      patientId: selectedRegistration!.patientId,
      encounterId: selectedRegistration!.encounterId,
      doctorId: selectedRegistration!.doctorId || session!.userId,
      diagnosisName: values.diagnosisName,
      icdCode: values.icdCode,
      diagnosisDesc: values.diagnosisDesc,
      diagnosisType: 'WESTERN',
      isPrimary: values.isPrimary ? 1 : 0,
      isConfirmed: 1,
      onsetDate: today,
    }),
    onSuccess: async () => { messageApi.success('诊断已添加'); setDiagnosisOpen(false); diagnosisForm.resetFields(); await refreshClinicalData() },
    onError: () => messageApi.error('诊断保存失败，请检查就诊信息。'),
  })
  const deleteDiagnosis = useMutation({
    mutationFn: clinicalApi.deleteDiagnosis,
    onSuccess: async () => { messageApi.success('诊断已删除'); await refreshClinicalData() },
    onError: () => messageApi.error('诊断删除失败。'),
  })
  const createOrder = useMutation({
    mutationFn: (values: OrderFormValues) => clinicalApi.createOrder({
      patientId: selectedRegistration!.patientId,
      encounterId: selectedRegistration!.encounterId,
      deptId: selectedRegistration!.deptId,
      doctorId: selectedRegistration!.doctorId || session!.userId,
      orderType: values.orderType,
      orderCategory: values.orderCategory,
      isStat: values.orderCategory === 'STAT' ? 1 : 0,
      remark: values.remark,
      items: [{
        itemName: values.itemName, itemType: values.orderType, spec: values.spec, dose: values.dose,
        doseUnit: values.doseUnit, usageMethod: values.usageMethod, frequency: values.frequency,
        days: values.days, quantity: values.quantity, quantityUnit: values.quantityUnit,
        unitPrice: values.unitPrice,
      }],
    }),
    onSuccess: async () => { messageApi.success('医嘱已开立'); setOrderOpen(false); orderForm.resetFields(); await refreshClinicalData() },
    onError: () => messageApi.error('医嘱开立失败，请检查项目和患者信息。'),
  })
  const cancelOrder = useMutation({
    mutationFn: (id: number) => clinicalApi.cancelOrder(id, '医生撤销'),
    onSuccess: async () => { messageApi.success('医嘱已取消'); await refreshClinicalData() },
    onError: () => messageApi.error('医嘱取消失败，当前状态可能不允许取消。'),
  })

  const recordColumns: TableColumnsType<MedicalRecord> = [
    { title: '病历号', dataIndex: 'recordNo', width: 150 },
    { title: '标题', dataIndex: 'title', render: (value) => value || '门诊病历' },
    { title: '主诉', dataIndex: 'chiefComplaint', ellipsis: true },
    { title: '初步诊断', dataIndex: 'diagnosisDesc', ellipsis: true },
    { title: '状态', dataIndex: 'recordStatus', width: 90, render: (value) => { const item = recordStatus[value]; return <Tag color={item?.color}>{item?.text || value}</Tag> } },
    { title: '操作', key: 'action', width: 100, render: (_, row) => row.recordStatus === 'DRAFT' ? <Popconfirm title="签署后将不可继续修改，确认签署？" onConfirm={() => signRecord.mutate(row.id)}><Button type="link" icon={<FileDoneOutlined />}>签署</Button></Popconfirm> : '—' },
  ]
  const diagnosisColumns: TableColumnsType<Diagnosis> = [
    { title: '诊断', dataIndex: 'diagnosisName', render: (value, row) => <Space><strong>{value}</strong>{row.isPrimary === 1 && <Tag color="red">主诊断</Tag>}</Space> },
    { title: 'ICD-10', dataIndex: 'icdCode', width: 120, render: (value) => value || '—' },
    { title: '说明', dataIndex: 'diagnosisDesc', ellipsis: true, render: (value) => value || '—' },
    { title: '操作', key: 'action', width: 90, render: (_, row) => <Popconfirm title="确认删除该诊断？" onConfirm={() => deleteDiagnosis.mutate(row.id)}><Button danger type="link" icon={<DeleteOutlined />}>删除</Button></Popconfirm> },
  ]
  const orderColumns: TableColumnsType<ClinicalOrder> = [
    { title: '医嘱号', dataIndex: 'orderNo', width: 150 },
    { title: '类型', dataIndex: 'orderType', width: 90, render: (value) => orderTypeText[value] || value },
    { title: '项目', key: 'items', render: (_, row) => row.items?.map((item) => item.itemName).join('、') || '—' },
    { title: '状态', dataIndex: 'orderStatus', width: 100, render: (value) => { const item = orderStatus[value]; return <Tag color={item?.color}>{item?.text || value}</Tag> } },
    { title: '开立时间', dataIndex: 'orderTime', width: 160, render: (value) => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—' },
    { title: '操作', key: 'action', width: 90, render: (_, row) => !['CANCELLED', 'COMPLETED', 'STOPPED'].includes(row.orderStatus) ? <Popconfirm title="确认取消该医嘱？" onConfirm={() => cancelOrder.mutate(row.id)}><Button danger type="link">取消</Button></Popconfirm> : '—' },
  ]

  const noEncounter = selectedRegistration && !encounterId
  const tabItems = [
    { key: 'record', label: `病历 ${records.data?.length || 0}`, children: <Table<MedicalRecord> rowKey="id" columns={recordColumns} dataSource={records.data || []} loading={records.isLoading} pagination={false} locale={{ emptyText: '尚未书写病历' }} /> },
    { key: 'diagnosis', label: `诊断 ${diagnoses.data?.length || 0}`, children: noEncounter ? <Alert type="warning" showIcon message="该挂号尚未生成就诊号，暂不能录入诊断。" /> : <Table<Diagnosis> rowKey="id" columns={diagnosisColumns} dataSource={diagnoses.data || []} loading={diagnoses.isLoading} pagination={false} locale={{ emptyText: '尚未录入诊断' }} /> },
    { key: 'order', label: `医嘱 ${orders.data?.length || 0}`, children: <Table<ClinicalOrder> rowKey="id" columns={orderColumns} dataSource={orders.data || []} loading={orders.isLoading} pagination={false} scroll={{ x: 850 }} locale={{ emptyText: '尚未开立医嘱' }} /> },
  ]

  return <>
    {messageContext}
    <div className="page-heading patient-heading"><div><h1>临床接诊</h1><p>选择今日挂号患者，集中完成病历、诊断和医嘱处理。</p></div><Button icon={<ReloadOutlined />} onClick={() => refreshClinicalData()}>刷新诊疗数据</Button></div>
    {registrations.isError && <Alert type="error" showIcon message="今日挂号加载失败" description="请确认网关和患者服务已启动。" style={{ marginBottom: 16 }} />}
    <div className="clinical-layout">
      <Card className="clinical-patient-card" title="今日候诊患者">
        <Select className="clinical-patient-select" placeholder="请选择患者" loading={registrations.isLoading} value={selectedRegistrationId} onChange={setRequestedRegistrationId} options={registrationRows.map((item) => ({ value: item.id, label: `${item.visitSeq}号 · ${item.patientName} · ${item.doctorName}` }))} />
        {selectedRegistration ? <Descriptions column={1} size="small" className="clinical-patient-detail">
          <Descriptions.Item label="患者">{selectedRegistration.patientName}</Descriptions.Item>
          <Descriptions.Item label="挂号单">{selectedRegistration.regNo}</Descriptions.Item>
          <Descriptions.Item label="科室">{selectedRegistration.deptName}</Descriptions.Item>
          <Descriptions.Item label="接诊医生">{selectedRegistration.doctorName}</Descriptions.Item>
          <Descriptions.Item label="就诊序号">第 {selectedRegistration.visitSeq} 号</Descriptions.Item>
          <Descriptions.Item label="就诊状态"><Tag color={selectedRegistration.encounterId ? 'processing' : 'warning'}>{selectedRegistration.encounterId ? '接诊中' : '待接诊'}</Tag></Descriptions.Item>
        </Descriptions> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="今日暂无可接诊挂号" />}
      </Card>
      <Card className="clinical-workspace-card" title="诊疗工作区" extra={<Space wrap><Button icon={<PlusOutlined />} disabled={!selectedRegistration} onClick={() => setRecordOpen(true)}>写病历</Button><Button icon={<PlusOutlined />} disabled={!encounterId} onClick={() => setDiagnosisOpen(true)}>加诊断</Button><Button type="primary" icon={<MedicineBoxOutlined />} disabled={!selectedRegistration} onClick={() => setOrderOpen(true)}>开医嘱</Button></Space>}>
        {selectedRegistration ? <Tabs items={tabItems} /> : <Empty description="选择患者后开始接诊" />}
      </Card>
    </div>

    <Modal title="书写门诊病历" open={recordOpen} onCancel={() => setRecordOpen(false)} onOk={() => recordForm.submit()} okText="保存病历" cancelText="取消" confirmLoading={createRecord.isPending} width={720} destroyOnHidden>
      <Form<RecordFormValues> form={recordForm} layout="vertical" onFinish={(values) => createRecord.mutate(values)} initialValues={{ title: '门诊病历' }}>
        <Form.Item name="title" label="病历标题"><Input /></Form.Item>
        <Form.Item name="chiefComplaint" label="主诉" rules={[{ required: true, message: '请输入患者主诉' }]}><Input.TextArea rows={2} placeholder="症状、部位和持续时间" /></Form.Item>
        <Form.Item name="presentIllness" label="现病史"><Input.TextArea rows={3} /></Form.Item>
        <Form.Item name="diagnosisDesc" label="初步诊断"><Input.TextArea rows={2} /></Form.Item>
        <Form.Item name="treatmentPlan" label="诊疗计划"><Input.TextArea rows={3} /></Form.Item>
      </Form>
    </Modal>
    <Modal title="添加诊断" open={diagnosisOpen} onCancel={() => setDiagnosisOpen(false)} onOk={() => diagnosisForm.submit()} okText="保存诊断" cancelText="取消" confirmLoading={createDiagnosis.isPending} destroyOnHidden>
      <Form<DiagnosisFormValues> form={diagnosisForm} layout="vertical" onFinish={(values) => createDiagnosis.mutate(values)}>
        <Form.Item name="diagnosisName" label="诊断名称" rules={[{ required: true, message: '请输入诊断名称' }]}><Input placeholder="例如：上呼吸道感染" /></Form.Item>
        <Form.Item name="icdCode" label="ICD-10 编码"><Input placeholder="例如：J06.9" /></Form.Item>
        <Form.Item name="diagnosisDesc" label="诊断说明"><Input.TextArea rows={3} /></Form.Item>
        <Form.Item name="isPrimary" valuePropName="checked"><Checkbox>设为主诊断</Checkbox></Form.Item>
      </Form>
    </Modal>
    <Modal title="开立医嘱" open={orderOpen} onCancel={() => setOrderOpen(false)} onOk={() => orderForm.submit()} okText="开立医嘱" cancelText="取消" confirmLoading={createOrder.isPending} width={720} destroyOnHidden>
      <Form<OrderFormValues> form={orderForm} layout="vertical" className="clinical-order-form" onFinish={(values) => createOrder.mutate(values)} initialValues={{ orderType: 'MEDICINE', orderCategory: 'ROUTINE' }}>
        <Form.Item name="orderType" label="医嘱类型" rules={[{ required: true }]}><Select options={Object.entries(orderTypeText).map(([value, label]) => ({ value, label }))} /></Form.Item>
        <Form.Item name="orderCategory" label="医嘱类别" rules={[{ required: true }]}><Select options={[{ value: 'ROUTINE', label: '常规' }, { value: 'STAT', label: '紧急' }, { value: 'PRN', label: '必要时' }]} /></Form.Item>
        <Form.Item name="itemName" label="项目名称" rules={[{ required: true, message: '请输入医嘱项目' }]}><Input placeholder="药品、检查或治疗项目" /></Form.Item>
        <Form.Item name="spec" label="规格"><Input /></Form.Item>
        <Form.Item name="dose" label="单次剂量"><InputNumber min={0} precision={2} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="doseUnit" label="剂量单位"><Input placeholder="mg、ml 等" /></Form.Item>
        <Form.Item name="usageMethod" label="用法"><Input placeholder="口服、静滴等" /></Form.Item>
        <Form.Item name="frequency" label="频次"><Input placeholder="每日三次、立即执行等" /></Form.Item>
        <Form.Item name="days" label="天数"><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="quantity" label="数量"><InputNumber min={0} precision={2} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="quantityUnit" label="数量单位"><Input placeholder="盒、支、次等" /></Form.Item>
        <Form.Item name="unitPrice" label="单价"><InputNumber min={0} precision={2} prefix="¥" style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="remark" label="备注" className="form-span-2"><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>
  </>
}
