import { CheckCircleOutlined, DeleteOutlined, EditOutlined, FileDoneOutlined, MedicineBoxOutlined, PlusOutlined, ReloadOutlined, RightOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Checkbox, Descriptions, Empty, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, Tooltip, message } from 'antd'
import type { TableColumnsType } from 'antd'
import dayjs from 'dayjs'
import { useEffect, useMemo, useState } from 'react'
import { clinicalApi } from '@/services/clinicalApi'
import { patientApi } from '@/services/patientApi'
import { registrationApi } from '@/services/registrationApi'
import { useAuthStore } from '@/stores/authStore'
import type { ClinicalOrder, CommonPhrase, Diagnosis, ExamRequest, Icd10Item, MedicalRecord, MedicalRecordUpdateRequest, RecordTemplate, RecordTemplateContent } from '@/types/clinical'
import { maskPhone } from '@/utils/maskSensitive'
import { canCloseEncounter, ordersForEncounter } from './clinicalWorkflow'

interface RecordFormValues extends MedicalRecordUpdateRequest { chiefComplaint: string }
interface DiagnosisFormValues { diagnosisName: string; icdCode?: string; icd10Id?: number; diagnosisDesc?: string; isPrimary?: boolean }
interface OrderFormValues {
  orderType: string; orderCategory: string; itemName: string; spec?: string; dose?: number; doseUnit?: string
  usageMethod?: string; frequency?: string; days?: number; quantity?: number; quantityUnit?: string; unitPrice?: number; remark?: string
}
interface ExamFormValues { requestType: string; itemName: string; itemType: string; quantity?: number; unitPrice?: number; bodyPart?: string; clinicalInfo?: string; isUrgent?: boolean }
interface ExamResultValues { resultSummary: string; reportNo?: string; reportUrl?: string; isCritical?: boolean }

const recordStatus: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'warning' }, SIGNED: { text: '已签署', color: 'success' }, ARCHIVED: { text: '已归档', color: 'default' },
}
const examStatus: Record<string, { text: string; color: string }> = {
  SUBMITTED: { text: '已提交', color: 'blue' }, ACCEPTED: { text: '已接收', color: 'cyan' }, IN_PROGRESS: { text: '执行中', color: 'processing' }, REPORTED: { text: '已报告', color: 'success' }, CANCELLED: { text: '已取消', color: 'default' },
}
const encounterStatus: Record<string, { text: string; color: string }> = {
  PLANNED: { text: '待接诊', color: 'warning' }, IN_PROGRESS: { text: '接诊中', color: 'processing' }, CLOSED: { text: '已结束', color: 'default' },
}
const orderStatus: Record<string, { text: string; color: string }> = {
  SUBMITTED: { text: '已提交', color: 'processing' },
  DRAFT: { text: '草稿', color: 'default' }, PENDING: { text: '待审核', color: 'warning' }, VERIFIED: { text: '已审核', color: 'processing' },
  EXECUTING: { text: '执行中', color: 'blue' }, COMPLETED: { text: '已完成', color: 'success' }, CANCELLED: { text: '已取消', color: 'default' }, STOPPED: { text: '已停止', color: 'default' },
}
const orderTypeText: Record<string, string> = {
  MEDICINE: '药品', EXAM: '检查', LAB: '检验', TREATMENT: '治疗', SURGERY: '手术', NURSING: '护理', OTHER: '其他',
}

export function ClinicalPage() {
  const session = useAuthStore((state) => state.session)
  const queryClient = useQueryClient()
  const [requestedRegistrationId, setRequestedRegistrationId] = useState<number>()
  const [recordOpen, setRecordOpen] = useState(false)
  const [editingRecord, setEditingRecord] = useState<MedicalRecord | null>(null)
  const [diagnosisOpen, setDiagnosisOpen] = useState(false)
  const [orderOpen, setOrderOpen] = useState(false)
  const [examOpen, setExamOpen] = useState(false)
  const [examResultOpen, setExamResultOpen] = useState(false)
  const [selectedExam, setSelectedExam] = useState<ExamRequest | null>(null)
  const [icdKeyword, setIcdKeyword] = useState('')
  const [recordForm] = Form.useForm<RecordFormValues>()
  const [diagnosisForm] = Form.useForm<DiagnosisFormValues>()
  const [orderForm] = Form.useForm<OrderFormValues>()
  const [examForm] = Form.useForm<ExamFormValues>()
  const [examResultForm] = Form.useForm<ExamResultValues>()
  const [draftSavedAt, setDraftSavedAt] = useState<string>()
  const [messageApi, messageContext] = message.useMessage()
  const selectedOrderType = Form.useWatch('orderType', orderForm)
  const today = dayjs().format('YYYY-MM-DD')

  const registrations = useQuery({
    queryKey: ['registrations', 'clinical', today, session?.deptId],
    queryFn: () => registrationApi.queryRegistrations({ page: 1, size: 100, regDate: today, deptId: session?.deptId }),
  })
  const registrationRows = useMemo(() => (registrations.data?.records || []).filter((item) => item.regStatus === 'ACTIVE'), [registrations.data])
  const selectedRegistrationId = registrationRows.some((item) => item.id === requestedRegistrationId) ? requestedRegistrationId : registrationRows[0]?.id
  const selectedRegistration = registrationRows.find((item) => item.id === selectedRegistrationId)

  const patient = useQuery({
    queryKey: ['patient-detail', selectedRegistration?.patientId],
    queryFn: () => patientApi.getById(selectedRegistration!.patientId),
    enabled: Boolean(selectedRegistration?.patientId),
  })
  const encounter = useQuery({
    queryKey: ['encounter', selectedRegistrationId],
    queryFn: () => registrationApi.getEncounterByRegistration(selectedRegistrationId!),
    enabled: Boolean(selectedRegistrationId), retry: false,
  })
  const encounterId = encounter.data?.id || selectedRegistration?.encounterId
  const currentEncounterStatus = encounter.data?.encounterStatus || (encounterId ? 'PLANNED' : undefined)
  const isInProgress = currentEncounterStatus === 'IN_PROGRESS'
  const isClosed = currentEncounterStatus === 'CLOSED'

  const records = useQuery({
    queryKey: ['clinical-records', encounterId], queryFn: () => clinicalApi.listRecordsByEncounter(encounterId!), enabled: Boolean(encounterId),
  })
  const diagnoses = useQuery({
    queryKey: ['clinical-diagnoses', encounterId], queryFn: () => clinicalApi.listDiagnoses(encounterId!), enabled: Boolean(encounterId),
  })
  const orders = useQuery({
    queryKey: ['clinical-orders', selectedRegistration?.patientId], queryFn: () => clinicalApi.listOrders(selectedRegistration!.patientId), enabled: Boolean(selectedRegistration?.patientId),
    refetchInterval: (query) => query.state.data?.some((order) => order.orderStatus === 'SUBMITTED' && !order.billId) ? 30000 : false,
  })
  const examRequests = useQuery({ queryKey: ['clinical-exam-requests', selectedRegistration?.patientId], queryFn: () => clinicalApi.listExamRequests(selectedRegistration!.patientId), enabled: Boolean(selectedRegistration?.patientId) })
  const icdOptions = useQuery({
    queryKey: ['icd10', icdKeyword], queryFn: () => clinicalApi.searchIcd10(icdKeyword), enabled: diagnosisOpen && icdKeyword.trim().length > 0,
  })
  const commonPhrases = useQuery({
    queryKey: ['common-phrases'], queryFn: () => clinicalApi.listCommonPhrases(), enabled: recordOpen,
  })
  const recordTemplatesQuery = useQuery({
    queryKey: ['record-templates'], queryFn: () => clinicalApi.listRecordTemplates(1), enabled: recordOpen && !editingRecord,
  })
  const phraseFieldMap: Record<string, keyof RecordFormValues> = {
    CHIEF_COMPLAINT: 'chiefComplaint', PRESENT_ILLNESS: 'presentIllness', PHYSICAL_EXAM: 'physicalExam', TREATMENT_PLAN: 'treatmentPlan',
  }
  const phraseFieldLabel: Record<string, string> = {
    CHIEF_COMPLAINT: '主诉', PRESENT_ILLNESS: '现病史', PHYSICAL_EXAM: '查体', TREATMENT_PLAN: '诊疗计划',
  }
  const insertPhrase = (phrase: CommonPhrase) => {
    const field = phraseFieldMap[phrase.phraseType]
    if (!field) return
    const current = (recordForm.getFieldValue(field) as string) || ''
    recordForm.setFieldsValue({ [field]: current ? `${current}\n${phrase.phraseContent}` : phrase.phraseContent })
  }
  const applyTemplate = (template: RecordTemplate) => {
    try {
      const content: RecordTemplateContent = JSON.parse(template.templateContent || '{}')
      recordForm.setFieldsValue({
        chiefComplaint: content.chiefComplaint || '',
        presentIllness: content.presentIllness || '',
        physicalExam: content.physicalExam || '',
        diagnosisDesc: content.diagnosisDesc || '',
        treatmentPlan: content.treatmentPlan || '',
      })
    } catch {
      message.error('模板内容解析失败')
    }
  }
  const encounterOrders = useMemo(() => ordersForEncounter(orders.data || [], encounterId), [orders.data, encounterId])
  const canClose = canCloseEncounter(currentEncounterStatus, records.data || [], diagnoses.data || [])

  const refreshClinicalData = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['registrations'] }), queryClient.invalidateQueries({ queryKey: ['patient-detail'] }),
      queryClient.invalidateQueries({ queryKey: ['encounter'] }), queryClient.invalidateQueries({ queryKey: ['clinical-records'] }),
      queryClient.invalidateQueries({ queryKey: ['clinical-diagnoses'] }), queryClient.invalidateQueries({ queryKey: ['clinical-orders'] }),
      queryClient.invalidateQueries({ queryKey: ['clinical-exam-requests'] }),
    ])
  }
  const startEncounter = useMutation({
    mutationFn: () => registrationApi.openEncounter(selectedRegistration!.id),
    onSuccess: async () => { messageApi.success('已开始接诊'); await refreshClinicalData() },
    onError: () => messageApi.error('开始接诊失败，请确认挂号有效且患者尚未结束就诊。'),
  })
  const closeEncounter = useMutation({
    mutationFn: () => registrationApi.closeEncounter(encounterId!),
    onSuccess: async () => { messageApi.success('本次接诊已结束'); await refreshClinicalData() },
    onError: () => messageApi.error('结束接诊失败，请刷新后检查就诊状态。'),
  })
  const saveRecord = useMutation({
    mutationFn: (values: RecordFormValues) => editingRecord
      ? clinicalApi.updateRecord(editingRecord.id, values)
      : clinicalApi.createRecord({
          ...values, patientId: selectedRegistration!.patientId, encounterId, deptId: selectedRegistration!.deptId,
          doctorId: selectedRegistration!.doctorId || session!.userId, recordType: 'OUTPATIENT',
        }),
    onSuccess: async () => { if (!editingRecord && encounterId) localStorage.removeItem(`clinical-record-draft:${encounterId}`); messageApi.success(editingRecord ? '病历草稿已更新' : '门诊病历草稿已保存'); setRecordOpen(false); setEditingRecord(null); recordForm.resetFields(); setDraftSavedAt(undefined); await refreshClinicalData() },
    onError: () => messageApi.error('病历保存失败，请确认病历仍为草稿并检查临床服务。'),
  })
  const signRecord = useMutation({
    mutationFn: clinicalApi.signRecord,
    onSuccess: async () => { messageApi.success('病历已签署'); await refreshClinicalData() },
    onError: () => messageApi.error('病历签署失败，请确认病历仍为草稿。'),
  })
  const createDiagnosis = useMutation({
    mutationFn: (values: DiagnosisFormValues) => clinicalApi.createDiagnosis({
      patientId: selectedRegistration!.patientId, encounterId, doctorId: selectedRegistration!.doctorId || session!.userId,
      diagnosisName: values.diagnosisName, icdCode: values.icdCode, icd10Id: values.icd10Id, diagnosisDesc: values.diagnosisDesc,
      diagnosisType: 'WESTERN', isPrimary: values.isPrimary ? 1 : 0, isConfirmed: 1, onsetDate: today,
    }),
    onSuccess: async () => { messageApi.success('诊断已添加'); setDiagnosisOpen(false); setIcdKeyword(''); diagnosisForm.resetFields(); await refreshClinicalData() },
    onError: () => messageApi.error('诊断保存失败，请检查就诊信息。'),
  })
  const deleteDiagnosis = useMutation({
    mutationFn: clinicalApi.deleteDiagnosis,
    onSuccess: async () => { messageApi.success('诊断已删除'); await refreshClinicalData() }, onError: () => messageApi.error('诊断删除失败。'),
  })
  const createOrder = useMutation({
    mutationFn: (values: OrderFormValues) => clinicalApi.createOrder({
      patientId: selectedRegistration!.patientId, encounterId, deptId: selectedRegistration!.deptId,
      doctorId: selectedRegistration!.doctorId || session!.userId, orderType: values.orderType, orderCategory: values.orderCategory,
      isStat: values.orderCategory === 'STAT' ? 1 : 0, remark: values.remark,
      items: [{ itemName: values.itemName, itemType: values.orderType, spec: values.spec, dose: values.dose, doseUnit: values.doseUnit, usageMethod: values.usageMethod, frequency: values.frequency, days: values.days, quantity: values.quantity, quantityUnit: values.quantityUnit, unitPrice: values.unitPrice }],
    }),
    onSuccess: async () => { messageApi.success('医嘱已开立'); setOrderOpen(false); orderForm.resetFields(); await refreshClinicalData() },
    onError: () => messageApi.error('医嘱开立失败，请检查项目和患者信息。'),
  })
  const createExamRequest = useMutation({
    mutationFn: (values: ExamFormValues) => clinicalApi.createExamRequest({ patientId: selectedRegistration!.patientId, encounterId, deptId: selectedRegistration!.deptId, doctorId: selectedRegistration!.doctorId || session!.userId, requestType: values.requestType, isUrgent: values.isUrgent ? 1 : 0, clinicalInfo: values.clinicalInfo, items: [{ itemName: values.itemName, itemType: values.itemType, quantity: values.quantity || 1, unitPrice: values.unitPrice || 0, bodyPart: values.bodyPart }] }),
    onSuccess: async () => { messageApi.success('检验检查申请已提交'); setExamOpen(false); examForm.resetFields(); await refreshClinicalData() }, onError: () => messageApi.error('检验检查申请失败，请检查项目和患者信息。'),
  })
  const updateExamStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) => clinicalApi.updateExamRequestStatus(id, status),
    onSuccess: async () => { messageApi.success('检验检查状态已更新'); await refreshClinicalData() },
    onError: () => messageApi.error('状态更新失败，请确认当前状态允许该操作。'),
  })
  const reportExamRequest = useMutation({
    mutationFn: (values: ExamResultValues) => clinicalApi.reportExamRequest(selectedExam!.id, values.resultSummary, values.reportNo, values.reportUrl, values.isCritical ? 1 : 0),
    onSuccess: async () => { messageApi.success('检验检查结果已提交审核'); setExamResultOpen(false); setSelectedExam(null); examResultForm.resetFields(); await refreshClinicalData() },
    onError: () => messageApi.error('结果提交失败，请确认申请处于执行中状态。'),
  })
  const acknowledgeCriticalExam = useMutation({
    mutationFn: (id: number) => clinicalApi.acknowledgeCriticalExam(id, session!.userId),
    onSuccess: async () => { messageApi.success('危急值已确认'); await refreshClinicalData() },
    onError: () => messageApi.error('危急值确认失败。'),
  })
  const cancelOrder = useMutation({
    mutationFn: (id: number) => clinicalApi.cancelOrder(id, '医生撤销'),
    onSuccess: async () => { messageApi.success('医嘱已取消'); await refreshClinicalData() },
    onError: () => messageApi.error('医嘱取消失败，请确认账单尚未收款且运营服务可用。'),
  })
  const submitOrder = useMutation({
    mutationFn: (id: number) => clinicalApi.submitOrder(id),
    onSuccess: async (order) => {
      messageApi.success(order.billId ? '医嘱已提交，费用已入账' : '医嘱已提交，费用待入账，请稍后刷新')
      await refreshClinicalData()
      await queryClient.invalidateQueries({ queryKey: ['operations-bills'] })
    },
    onError: () => messageApi.error('提交失败，请核对数量、单价和医嘱状态。'),
  })

  const openNewRecord = () => {
    setEditingRecord(null)
    recordForm.setFieldsValue({ title: '门诊病历', allergyHistory: patient.data?.allergyHistory })
    if (encounterId) {
      const saved = localStorage.getItem(`clinical-record-draft:${encounterId}`)
      if (saved) {
        try { recordForm.setFieldsValue(JSON.parse(saved) as RecordFormValues); setDraftSavedAt('已恢复自动保存草稿') } catch { localStorage.removeItem(`clinical-record-draft:${encounterId}`) }
      }
    }
    setRecordOpen(true)
  }
  const openRecordEditor = (record: MedicalRecord) => { setEditingRecord(record); recordForm.setFieldsValue(record); setRecordOpen(true) }
  const selectIcd = (id: number) => {
    const selected = icdOptions.data?.find((item) => item.id === id)
    if (selected) diagnosisForm.setFieldsValue({ icd10Id: selected.id, icdCode: selected.icdCode, diagnosisName: selected.icdName })
  }
  const recordDraft = Form.useWatch([], recordForm)
  useEffect(() => {
    if (!recordOpen || editingRecord || !encounterId || !recordDraft?.chiefComplaint) return
    const timer = window.setTimeout(() => {
      localStorage.setItem(`clinical-record-draft:${encounterId}`, JSON.stringify(recordDraft))
      setDraftSavedAt(`自动保存于 ${dayjs().format('HH:mm:ss')}`)
    }, 800)
    return () => window.clearTimeout(timer)
  }, [recordDraft, recordOpen, editingRecord, encounterId])

  const recordColumns: TableColumnsType<MedicalRecord> = [
    { title: '病历号', dataIndex: 'recordNo', width: 150 }, { title: '标题', dataIndex: 'title', width: 130, render: (value) => value || '门诊病历' },
    { title: '主诉', dataIndex: 'chiefComplaint', ellipsis: true }, { title: '初步诊断', dataIndex: 'diagnosisDesc', ellipsis: true },
    { title: '状态', dataIndex: 'recordStatus', width: 90, render: (value) => { const item = recordStatus[value]; return <Tag color={item?.color}>{item?.text || value}</Tag> } },
    { title: '操作', key: 'action', width: 150, render: (_, row) => row.recordStatus === 'DRAFT' && !isClosed ? <Space size={0}><Button type="link" icon={<EditOutlined />} onClick={() => openRecordEditor(row)}>编辑</Button><Popconfirm title="签署后将不可继续修改，确认签署？" onConfirm={() => signRecord.mutate(row.id)}><Button type="link" icon={<FileDoneOutlined />}>签署</Button></Popconfirm></Space> : '—' },
  ]
  const diagnosisColumns: TableColumnsType<Diagnosis> = [
    { title: '诊断', dataIndex: 'diagnosisName', render: (value, row) => <Space><strong>{value}</strong>{row.isPrimary === 1 && <Tag color="red">主诊断</Tag>}</Space> },
    { title: 'ICD-10', dataIndex: 'icdCode', width: 120, render: (value) => value || '—' }, { title: '说明', dataIndex: 'diagnosisDesc', ellipsis: true, render: (value) => value || '—' },
    { title: '操作', key: 'action', width: 90, render: (_, row) => !isClosed ? <Popconfirm title="确认删除该诊断？" onConfirm={() => deleteDiagnosis.mutate(row.id)}><Button danger type="link" icon={<DeleteOutlined />}>删除</Button></Popconfirm> : '—' },
  ]
  const orderColumns: TableColumnsType<ClinicalOrder> = [
    { title: '医嘱号', dataIndex: 'orderNo', width: 150 }, { title: '类型', dataIndex: 'orderType', width: 90, render: (value) => orderTypeText[value] || value },
    { title: '项目', key: 'items', render: (_, row) => row.items?.map((item) => item.itemName).join('、') || '—' },
    { title: '状态', dataIndex: 'orderStatus', width: 100, render: (value) => { const item = orderStatus[value]; return <Tag color={item?.color}>{item?.text || value}</Tag> } },
    { title: '费用', key: 'billing', width: 110, render: (_, row) => row.orderStatus === 'CANCELLED' ? '已取消' : row.billId ? <Tag color="green">已入账</Tag> : row.orderStatus === 'DRAFT' ? '未提交' : <Tag color="gold">待入账</Tag> },
    { title: '开立时间', dataIndex: 'orderTime', width: 160, render: (value) => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—' },
    { title: '操作', key: 'action', width: 170, render: (_, row) => !isClosed && !['CANCELLED', 'COMPLETED', 'STOPPED'].includes(row.orderStatus) ? <Space size={0}>
      {['DRAFT', 'SUBMITTED'].includes(row.orderStatus) && !row.billId && <Button type="link" loading={submitOrder.isPending} onClick={() => submitOrder.mutate(row.id)}>{row.orderStatus === 'DRAFT' ? '提交收费' : '重试入账'}</Button>}
      <Popconfirm title="确认取消该医嘱？已收款账单须先退费。" onConfirm={() => cancelOrder.mutate(row.id)}><Button danger type="link" loading={cancelOrder.isPending}>取消</Button></Popconfirm>
    </Space> : '—' },
  ]
  const examColumns: TableColumnsType<ExamRequest> = [
    { title: '申请单号', dataIndex: 'requestNo', width: 150 }, { title: '类型', dataIndex: 'requestType', width: 90, render: (value) => value === 'LAB' ? '检验' : '检查' },
    { title: '项目', key: 'items', render: (_, row) => row.items?.map((item) => item.itemName).join('、') || '—' }, { title: '状态', dataIndex: 'requestStatus', width: 100, render: (value) => <Tag color={examStatus[value]?.color}>{examStatus[value]?.text || value}</Tag> },
    { title: '结果', dataIndex: 'resultSummary', ellipsis: true, render: (value, row) => <Space>{value || '待回写'}{row.isCritical === 1 && <Tag color="error">危急值</Tag>}</Space> },
    { title: '操作', key: 'actions', width: 280, render: (_, row) => <Space size={0}>{row.requestStatus === 'SUBMITTED' && <Button type="link" onClick={() => updateExamStatus.mutate({ id: row.id, status: 'ACCEPTED' })}>接收</Button>}{row.requestStatus === 'ACCEPTED' && <Button type="link" onClick={() => updateExamStatus.mutate({ id: row.id, status: 'IN_PROGRESS' })}>开始执行</Button>}{row.requestStatus === 'IN_PROGRESS' && <Button type="link" onClick={() => { setSelectedExam(row); setExamResultOpen(true) }}>录入结果</Button>}{row.isCritical === 1 && row.criticalAcknowledged !== 1 && <Button danger type="link" onClick={() => acknowledgeCriticalExam.mutate(row.id)}>确认危急值</Button>}{!['REPORTED', 'CANCELLED'].includes(row.requestStatus) && <Popconfirm title="确认取消申请？" onConfirm={() => updateExamStatus.mutate({ id: row.id, status: 'CANCELLED' })}><Button danger type="link">取消</Button></Popconfirm>}</Space> },
  ]
  const tabItems = [
    { key: 'record', label: `病历 ${records.data?.length || 0}`, children: <Table<MedicalRecord> rowKey="id" columns={recordColumns} dataSource={records.data || []} loading={records.isLoading} pagination={false} scroll={{ x: 850 }} locale={{ emptyText: '尚未书写本次门诊病历' }} /> },
    { key: 'diagnosis', label: `诊断 ${diagnoses.data?.length || 0}`, children: <Table<Diagnosis> rowKey="id" columns={diagnosisColumns} dataSource={diagnoses.data || []} loading={diagnoses.isLoading} pagination={false} locale={{ emptyText: '尚未录入诊断' }} /> },
    { key: 'order', label: `医嘱 ${encounterOrders.length}`, children: <Table<ClinicalOrder> rowKey="id" columns={orderColumns} dataSource={encounterOrders} loading={orders.isLoading} pagination={false} scroll={{ x: 850 }} locale={{ emptyText: '尚未开立医嘱' }} /> },
    { key: 'exam', label: `检验检查 ${examRequests.data?.length || 0}`, children: <Table<ExamRequest> rowKey="id" columns={examColumns} dataSource={examRequests.data || []} loading={examRequests.isLoading} pagination={false} scroll={{ x: 850 }} locale={{ emptyText: '尚未提交检验检查申请' }} /> },
  ]
  const statusMeta = currentEncounterStatus ? encounterStatus[currentEncounterStatus] : undefined
  const allergyHistory = patient.data?.allergyHistory?.trim()

  return <>
    {messageContext}
    <div className="page-heading patient-heading"><div><h1>临床接诊</h1><p>按就诊状态完成病历、诊断、医嘱和接诊归档。</p></div><Button icon={<ReloadOutlined />} onClick={() => refreshClinicalData()}>刷新诊疗数据</Button></div>
    {registrations.isError && <Alert type="error" showIcon message="今日挂号加载失败" description="请确认网关和患者服务已启动。" style={{ marginBottom: 16 }} />}
    <div className="clinical-layout">
      <Card className="clinical-patient-card" title="今日候诊患者" extra={<Tag>{registrationRows.length} 人</Tag>}>
        <Select className="clinical-patient-select" placeholder="请选择患者" loading={registrations.isLoading} value={selectedRegistrationId} onChange={setRequestedRegistrationId} options={registrationRows.map((item) => ({ value: item.id, label: `${item.visitSeq}号 · ${item.patientName} · ${item.payStatus === 'PAID' ? '已缴费' : '待缴费'}` }))} />
        {selectedRegistration ? <>
          <Descriptions column={1} size="small" className="clinical-patient-detail">
            <Descriptions.Item label="患者">{selectedRegistration.patientName} {patient.data?.ageDisplay && <span className="clinical-secondary">· {patient.data.ageDisplay}</span>}</Descriptions.Item>
            <Descriptions.Item label="联系电话">{patient.data?.phone ? maskPhone(patient.data.phone) : '—'}</Descriptions.Item>
            <Descriptions.Item label="挂号单">{selectedRegistration.regNo}</Descriptions.Item>
            <Descriptions.Item label="科室/医生">{selectedRegistration.deptName} · {selectedRegistration.doctorName}</Descriptions.Item>
            <Descriptions.Item label="就诊序号">第 {selectedRegistration.visitSeq} 号</Descriptions.Item>
            <Descriptions.Item label="缴费状态"><Tag color={selectedRegistration.payStatus === 'PAID' ? 'success' : 'warning'}>{selectedRegistration.payStatus === 'PAID' ? '已缴费' : '待缴费'}</Tag></Descriptions.Item>
            <Descriptions.Item label="就诊状态"><Tag color={statusMeta?.color}>{statusMeta?.text || '状态未知'}</Tag></Descriptions.Item>
          </Descriptions>
          {allergyHistory && allergyHistory !== '无' && <Alert className="clinical-allergy-alert" type="warning" showIcon message="过敏史" description={allergyHistory} />}
          <div className="clinical-encounter-actions">
            {currentEncounterStatus === 'PLANNED' && <Button block type="primary" disabled={selectedRegistration.payStatus !== 'PAID'} loading={startEncounter.isPending} onClick={() => startEncounter.mutate()}>开始接诊 <RightOutlined /></Button>}
            {isInProgress && <Tooltip title={canClose ? undefined : '至少需要一条已签署病历和一条诊断'}><span><Popconfirm disabled={!canClose} title="确认结束本次接诊？" description="结束后不能继续修改病历、诊断或医嘱。" onConfirm={() => closeEncounter.mutate()}><Button block disabled={!canClose} loading={closeEncounter.isPending} icon={<CheckCircleOutlined />}>结束接诊</Button></Popconfirm></span></Tooltip>}
            {selectedRegistration.payStatus !== 'PAID' && <div className="clinical-action-hint">患者完成挂号缴费后方可开始接诊。</div>}
          </div>
        </> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="今日暂无可接诊挂号" />}
      </Card>
      <Card className="clinical-workspace-card" title="诊疗工作区" extra={<Space wrap><Button icon={<PlusOutlined />} disabled={!isInProgress} onClick={openNewRecord}>写病历</Button><Button icon={<PlusOutlined />} disabled={!isInProgress} onClick={() => setDiagnosisOpen(true)}>加诊断</Button><Button icon={<PlusOutlined />} disabled={!isInProgress} onClick={() => setExamOpen(true)}>检验检查</Button><Button type="primary" icon={<MedicineBoxOutlined />} disabled={!isInProgress} onClick={() => setOrderOpen(true)}>开医嘱</Button></Space>}>
        {encounter.isError && <Alert type="error" showIcon message="就诊信息加载失败" description="请确认患者服务可用，或刷新后重新选择患者。" style={{ marginBottom: 12 }} />}
        {selectedRegistration ? <>{currentEncounterStatus === 'PLANNED' && <Alert type="info" showIcon message="当前患者尚未开始接诊" description="开始接诊后可以书写病历、添加诊断和开立医嘱。" style={{ marginBottom: 12 }} />}{isClosed && <Alert type="success" showIcon message="本次接诊已经结束" description="病历、诊断和医嘱已转为只读查看。" style={{ marginBottom: 12 }} />}<Tabs items={tabItems} /></> : <Empty description="选择患者后开始接诊" />}
      </Card>
    </div>

    <Modal title={editingRecord ? '编辑病历草稿' : '书写门诊病历'} open={recordOpen} onCancel={() => { setRecordOpen(false); setEditingRecord(null); recordForm.resetFields() }} onOk={() => recordForm.submit()} okText="保存草稿" cancelText="取消" confirmLoading={saveRecord.isPending} width={780} destroyOnHidden>
      <Form<RecordFormValues> form={recordForm} layout="vertical" onFinish={(values) => saveRecord.mutate(values)}>
        {!editingRecord && <Form.Item label="病历模板"><Select allowClear placeholder="选择模板后自动填充，可继续修改" loading={recordTemplatesQuery.isLoading} options={(recordTemplatesQuery.data || []).map((t) => ({ value: t.id, label: t.templateName }))} onChange={(id) => { const tpl = recordTemplatesQuery.data?.find((t) => t.id === id); if (tpl) applyTemplate(tpl) }} /></Form.Item>}
        {!editingRecord && draftSavedAt && <Alert type="info" showIcon message={draftSavedAt} description="填写内容会自动保存在当前浏览器，保存病历后自动清除。" style={{ marginBottom: 12 }} />}
        {commonPhrases.data && commonPhrases.data.length > 0 && <Card size="small" title="常用短语" style={{ marginBottom: 12 }} styles={{ body: { padding: '8px 12px' } }}>
          {Object.entries(phraseFieldLabel).filter(([type]) => commonPhrases.data!.some((p) => p.phraseType === type)).map(([type, label]) => (
            <div key={type} style={{ marginBottom: 4 }}><span className="clinical-secondary" style={{ marginRight: 8 }}>{label}：</span>
              {commonPhrases.data!.filter((p) => p.phraseType === type).map((phrase) => (
                <Tag key={phrase.id} style={{ cursor: 'pointer', marginBottom: 4 }} onClick={() => insertPhrase(phrase)} title={phrase.phraseContent}>{phrase.phraseName}</Tag>
              ))}
            </div>
          ))}
        </Card>}
        <div className="clinical-record-form">
          <Form.Item name="title" label="病历标题"><Input /></Form.Item>
          <Form.Item name="chiefComplaint" label="主诉" rules={[{ required: true, message: '请输入患者主诉' }]}><Input.TextArea rows={2} placeholder="症状、部位和持续时间" /></Form.Item>
          <Form.Item name="presentIllness" label="现病史"><Input.TextArea rows={3} placeholder="本次疾病发生、发展及诊疗经过" /></Form.Item>
          <Form.Item name="pastHistory" label="既往史"><Input.TextArea rows={2} placeholder="既往疾病、手术和输血史" /></Form.Item>
          <Form.Item name="allergyHistory" label="过敏史"><Input.TextArea rows={2} placeholder="药物、食物及其他过敏信息" /></Form.Item>
          <Form.Item name="physicalExam" label="体格检查"><Input.TextArea rows={3} placeholder="生命体征及阳性、阴性体征" /></Form.Item>
          <Form.Item name="auxiliaryExam" label="辅助检查"><Input.TextArea rows={2} placeholder="检验、影像及其他检查结果" /></Form.Item>
          <Form.Item name="diagnosisDesc" label="初步诊断"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="treatmentPlan" label="诊疗计划"><Input.TextArea rows={3} /></Form.Item>
        </div>
      </Form>
    </Modal>
    <Modal title="录入检验检查结果" open={examResultOpen} onCancel={() => { setExamResultOpen(false); setSelectedExam(null); examResultForm.resetFields() }} onOk={() => examResultForm.submit()} okText="提交审核" cancelText="取消" confirmLoading={reportExamRequest.isPending} destroyOnHidden>
      <Form<ExamResultValues> form={examResultForm} layout="vertical" onFinish={(values) => reportExamRequest.mutate(values)}>
        <Form.Item name="reportNo" label="报告编号"><Input /></Form.Item>
        <Form.Item name="reportUrl" label="报告地址"><Input placeholder="可选：PACS 或报告系统链接" /></Form.Item>
        <Form.Item name="resultSummary" label="结果摘要" rules={[{ required: true, message: '请输入结果摘要' }]}><Input.TextArea rows={5} placeholder="填写检验或检查结果，提交后进入已报告状态" /></Form.Item>
        <Form.Item name="isCritical" valuePropName="checked"><Checkbox>标记为危急值</Checkbox></Form.Item>
      </Form>
    </Modal>
    <Modal title="提交检验检查申请" open={examOpen} onCancel={() => { setExamOpen(false); examForm.resetFields() }} onOk={() => examForm.submit()} okText="提交申请" cancelText="取消" confirmLoading={createExamRequest.isPending} destroyOnHidden>
      <Form<ExamFormValues> form={examForm} layout="vertical" onFinish={(values) => createExamRequest.mutate(values)} initialValues={{ requestType: 'LAB', itemType: 'LAB', quantity: 1 }}>
        <Form.Item name="requestType" label="申请类型" rules={[{ required: true }]}><Select options={[{ value: 'LAB', label: '检验' }, { value: 'EXAM', label: '检查' }]} onChange={(value) => examForm.setFieldValue('itemType', value)} /></Form.Item>
        <Form.Item name="itemType" hidden><Input /></Form.Item>
        <Form.Item name="itemName" label="项目名称" rules={[{ required: true, message: '请输入检验或检查项目' }]}><Input placeholder="例如：血常规、胸部 CT" /></Form.Item>
        <Form.Item name="bodyPart" label="检查部位"><Input /></Form.Item><Form.Item name="quantity" label="数量"><InputNumber min={1} precision={0} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="unitPrice" label="单价"><InputNumber min={0} precision={2} prefix="¥" style={{ width: '100%' }} /></Form.Item><Form.Item name="clinicalInfo" label="临床信息"><Input.TextArea rows={3} /></Form.Item>
        <Form.Item name="isUrgent" valuePropName="checked"><Checkbox>加急申请</Checkbox></Form.Item>
      </Form>
    </Modal>
    <Modal title="添加诊断" open={diagnosisOpen} onCancel={() => { setDiagnosisOpen(false); setIcdKeyword(''); diagnosisForm.resetFields() }} onOk={() => diagnosisForm.submit()} okText="保存诊断" cancelText="取消" confirmLoading={createDiagnosis.isPending} destroyOnHidden>
      <Form<DiagnosisFormValues> form={diagnosisForm} layout="vertical" onFinish={(values) => createDiagnosis.mutate(values)}>
        <Form.Item label="ICD-10 诊断库"><Select showSearch filterOption={false} allowClear placeholder="输入诊断名称、拼音或编码检索" onSearch={(value) => setIcdKeyword(value.trim())} onChange={selectIcd} loading={icdOptions.isFetching} notFoundContent={icdKeyword ? '未找到匹配诊断，可在下方手工填写' : '请输入检索内容'} options={(icdOptions.data || []).map((item: Icd10Item) => ({ value: item.id, label: `${item.icdCode} · ${item.icdName}` }))} /></Form.Item>
        <Form.Item name="diagnosisName" label="诊断名称" rules={[{ required: true, message: '请选择或输入诊断名称' }]}><Input placeholder="例如：急性上呼吸道感染" /></Form.Item>
        <Form.Item name="icdCode" label="ICD-10 编码"><Input placeholder="例如：J06.9" /></Form.Item>
        <Form.Item name="icd10Id" hidden><InputNumber /></Form.Item>
        <Form.Item name="diagnosisDesc" label="诊断说明"><Input.TextArea rows={3} /></Form.Item>
        <Form.Item name="isPrimary" valuePropName="checked"><Checkbox>设为主诊断</Checkbox></Form.Item>
      </Form>
    </Modal>
    <Modal title="开立医嘱" open={orderOpen} onCancel={() => { setOrderOpen(false); orderForm.resetFields() }} onOk={() => orderForm.submit()} okText="开立医嘱" cancelText="取消" confirmLoading={createOrder.isPending} width={720} destroyOnHidden>
      <Form<OrderFormValues> form={orderForm} layout="vertical" className="clinical-order-form" onFinish={(values) => createOrder.mutate(values)} initialValues={{ orderType: 'MEDICINE', orderCategory: 'ROUTINE' }}>
        <Form.Item name="orderType" label="医嘱类型" rules={[{ required: true }]}><Select options={Object.entries(orderTypeText).map(([value, label]) => ({ value, label }))} /></Form.Item>
        <Form.Item name="orderCategory" label="医嘱类别" rules={[{ required: true }]}><Select options={[{ value: 'ROUTINE', label: '常规' }, { value: 'STAT', label: '紧急' }, { value: 'PRN', label: '必要时' }]} /></Form.Item>
        <Form.Item name="itemName" label="项目名称" rules={[{ required: true, message: '请输入医嘱项目' }]}><Input placeholder="药品、检查或治疗项目" /></Form.Item>
        <Form.Item name="spec" label="规格"><Input /></Form.Item>
        <Form.Item name="dose" label="单次剂量" rules={selectedOrderType === 'MEDICINE' ? [{ required: true, message: '药品医嘱需要填写剂量' }] : []}><InputNumber min={0.01} precision={2} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="doseUnit" label="剂量单位" rules={selectedOrderType === 'MEDICINE' ? [{ required: true, message: '请输入剂量单位' }] : []}><Input placeholder="mg、ml 等" /></Form.Item>
        <Form.Item name="usageMethod" label="用法"><Input placeholder="口服、静滴等" /></Form.Item>
        <Form.Item name="frequency" label="频次"><Input placeholder="每日三次、立即执行等" /></Form.Item>
        <Form.Item name="days" label="天数"><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="quantity" label="数量" rules={selectedOrderType === 'MEDICINE' ? [{ required: true, message: '药品医嘱需要填写数量' }] : []}><InputNumber min={0.01} precision={2} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="quantityUnit" label="数量单位"><Input placeholder="盒、支、次等" /></Form.Item>
        <Form.Item name="unitPrice" label="单价" rules={[{ required: true, message: '请填写单价，免费项目填写 0' }]}><InputNumber min={0} precision={2} prefix="¥" style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="remark" label="备注" className="form-span-2"><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>
  </>
}
