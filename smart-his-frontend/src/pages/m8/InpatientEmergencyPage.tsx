import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, DatePicker, Descriptions, Drawer, Form, Input, InputNumber, message, Modal, Select, Space, Table, Tabs, Tag } from 'antd'
import type { TableColumnsType } from 'antd'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { m8Api } from '@/services/m8Api'
import { useAuthStore } from '@/stores/authStore'
import { hasAnyPermission } from '@/security/authz'
import type { Admission, EmergencyTriage, ObservationRecord, ResuscitationRecord, SurgeryCase } from '@/types/m8'

type TransferValues = { targetDeptId: number; targetWardId: number; targetBedId: number; reason: string }
type DischargeValues = { dischargeType: string; dischargeSummary: string }
type TriageValues = { patientId: number; triageLevel: number; chiefComplaint: string; vitalSigns?: string }
type SurgeryValues = { surgeryName: string; plannedStartTime: Dayjs; surgeonId: number }
type ScheduleValues = { plannedStartTime: Dayjs; operatingRoom: string; anesthetistId: number; anesthesiaMethod: string }
type ObservationValues = { bedNo: string; diagnosis: string; treatmentPlan?: string }
type ResuscitationValues = { procedures?: string; medications?: string }

const admissionStatus: Record<string, { text: string; color: string }> = {
  PLANNED: { text: '待入院', color: 'gold' }, ADMITTED: { text: '在院', color: 'green' },
  DISCHARGED: { text: '已出院', color: 'default' }, CANCELLED: { text: '已取消', color: 'red' },
}
const triageMeta = { 1: ['Ⅰ级 濒危', 'red'], 2: ['Ⅱ级 危重', 'volcano'], 3: ['Ⅲ级 急症', 'gold'], 4: ['Ⅳ级 非急症', 'green'] } as const

export function InpatientEmergencyPage() {
  const client = useQueryClient()
  const session = useAuthStore(state => state.session)
  const [admissionPage, setAdmissionPage] = useState(1)
  const [selectedAdmission, setSelectedAdmission] = useState<Admission>()
  const [transferOpen, setTransferOpen] = useState(false)
  const [dischargeOpen, setDischargeOpen] = useState(false)
  const [surgeryOpen, setSurgeryOpen] = useState(false)
  const [triageOpen, setTriageOpen] = useState(false)
  const [surgeryApplyOpen, setSurgeryApplyOpen] = useState(false)
  const [surgeryScheduleOpen, setSurgeryScheduleOpen] = useState(false)
  const [selectedSurgery, setSelectedSurgery] = useState<SurgeryCase>()
  const [selectedTriage, setSelectedTriage] = useState<EmergencyTriage>()
  const [emergencyCareOpen, setEmergencyCareOpen] = useState(false)
  const [resuscitationOpen, setResuscitationOpen] = useState(false)
  const [observationOpen, setObservationOpen] = useState(false)
  const [error, setError] = useState('')
  const [transferForm] = Form.useForm<TransferValues>()
  const [dischargeForm] = Form.useForm<DischargeValues>()
  const [triageForm] = Form.useForm<TriageValues>()

  const admissions = useQuery({ queryKey: ['m8-admissions', admissionPage], queryFn: () => m8Api.admissions({ page: admissionPage, size: 20 }) })
  const emergency = useQuery({ queryKey: ['m8-emergency'], queryFn: m8Api.emergencyQueue })
  const beds = useQuery({ queryKey: ['m8-beds'], queryFn: () => m8Api.beds(undefined, 'AVAILABLE') })
  const surgeries = useQuery({ queryKey: ['m8-surgeries', selectedAdmission?.id], queryFn: () => m8Api.surgeries(selectedAdmission!.id), enabled: surgeryOpen && !!selectedAdmission })
  const resuscitations = useQuery({ queryKey: ['m8-resuscitations', selectedTriage?.patientId], queryFn: () => m8Api.resuscitations(selectedTriage!.patientId), enabled: emergencyCareOpen && !!selectedTriage })
  const observations = useQuery({ queryKey: ['m8-observations', selectedTriage?.patientId], queryFn: () => m8Api.observations(selectedTriage!.patientId), enabled: emergencyCareOpen && !!selectedTriage })
  const refreshAdmissions = () => client.invalidateQueries({ queryKey: ['m8-admissions'] })

  const admit = useMutation({ mutationFn: m8Api.admit, onSuccess: refreshAdmissions, onError: () => setError('办理入院失败，请检查患者状态和服务连接。') })
  const transfer = useMutation({ mutationFn: (values: TransferValues) => m8Api.transfer(selectedAdmission!.id, values), onSuccess: async () => { setTransferOpen(false); transferForm.resetFields(); await refreshAdmissions() }, onError: () => setError('转科转床失败，仅在院患者可操作，目标位置不能与当前位置相同。') })
  const discharge = useMutation({ mutationFn: (values: DischargeValues) => m8Api.discharge(selectedAdmission!.id, values), onSuccess: async () => { setDischargeOpen(false); dischargeForm.resetFields(); await refreshAdmissions() }, onError: () => setError('出院归档失败，请检查患者状态和出院小结。') })
  const triage = useMutation({ mutationFn: (values: TriageValues) => m8Api.createTriage({ ...values, triageNurseId: session?.userId ?? 1, triageNurseName: session?.realName }), onSuccess: async () => { setTriageOpen(false); triageForm.resetFields(); await client.invalidateQueries({ queryKey: ['m8-emergency'] }) }, onError: () => setError('分诊登记失败，请检查患者编号、分级和主诉。') })
  const updateTriage = useMutation({ mutationFn: ({ id, status }: { id: number; status: EmergencyTriage['triageStatus'] }) => m8Api.updateTriageStatus(id, status), onSuccess: () => client.invalidateQueries({ queryKey: ['m8-emergency'] }), onError: () => setError('急诊状态更新失败，已完成或取消记录不能重新开启。') })
  const refreshSurgeries = () => client.invalidateQueries({ queryKey: ['m8-surgeries'] })
  const refreshCare = () => Promise.all([client.invalidateQueries({ queryKey: ['m8-resuscitations'] }), client.invalidateQueries({ queryKey: ['m8-observations'] })])
  const applySurgery = useMutation({ mutationFn: (values: SurgeryValues) => m8Api.applySurgery({ admissionId: selectedAdmission!.id, surgeryName: values.surgeryName, plannedStartTime: values.plannedStartTime.format('YYYY-MM-DDTHH:mm:ss'), surgeonId: values.surgeonId }), onSuccess: async () => { setSurgeryApplyOpen(false); await refreshSurgeries() }, onError: () => setError('手术申请失败，请检查住院状态和计划时间。') })
  const scheduleSurgery = useMutation({ mutationFn: (values: ScheduleValues) => m8Api.scheduleSurgery(selectedSurgery!.id, { ...values, plannedStartTime: values.plannedStartTime.format('YYYY-MM-DDTHH:mm:ss') }), onSuccess: async () => { setSurgeryScheduleOpen(false); await refreshSurgeries() }, onError: () => setError('手术排程失败，请检查手术状态和麻醉信息。') })
  const surgeryAction = useMutation({ mutationFn: ({ id, action }: { id: number; action: 'start' | 'complete' }) => m8Api.surgeryAction(id, action, action === 'complete' ? { operativeNote: '手术顺利完成，无特殊情况。' } : undefined), onSuccess: refreshSurgeries, onError: () => setError('手术状态流转失败，请按申请、排程、开始、完成的顺序操作。') })
  const createAdmissionBill = useMutation({ mutationFn: m8Api.createAdmissionBill, onSuccess: bill => message.success(`住院费用账户 ${bill.billNo} 已就绪`), onError: () => setError('住院费用账户创建失败。') })
  const startResuscitation = useMutation({ mutationFn: (values: ResuscitationValues) => m8Api.startResuscitation({ ...values, triageId: selectedTriage!.id, patientId: selectedTriage!.patientId }), onSuccess: async () => { setResuscitationOpen(false); await refreshCare() }, onError: () => setError('抢救记录创建失败。') })
  const completeResuscitation = useMutation({ mutationFn: (record: ResuscitationRecord) => m8Api.completeResuscitation(record.id, { outcome: 'STABLE', outcomeSummary: '患者生命体征稳定，结束抢救。' }), onSuccess: refreshCare, onError: () => setError('完成抢救失败。') })
  const admitObservation = useMutation({ mutationFn: (values: ObservationValues) => m8Api.admitObservation({ ...values, triageId: selectedTriage!.id, patientId: selectedTriage!.patientId }), onSuccess: async () => { setObservationOpen(false); await refreshCare() }, onError: () => setError('留观登记失败。') })
  const dischargeObservation = useMutation({ mutationFn: (record: ObservationRecord) => m8Api.dischargeObservation(record.id, '病情稳定，结束留观。'), onSuccess: refreshCare, onError: () => setError('结束留观失败。') })

  const admissionColumns: TableColumnsType<Admission> = [
    { title: '住院号', dataIndex: 'admissionNo' },
    { title: '患者', render: (_, row) => `${row.patientName || '患者'} · ${row.patientId}` },
    { title: '科/病区/床', render: (_, row) => `${row.deptId} / ${row.wardId || '—'} / ${row.bedId || '—'}` },
    { title: '诊断', dataIndex: 'preliminaryDiagnosis', render: value => value || '—' },
    { title: '状态', dataIndex: 'admissionStatus', render: value => <Tag color={admissionStatus[value]?.color}>{admissionStatus[value]?.text || value}</Tag> },
    { title: '操作', width: 300, render: (_, row) => <Space wrap>
      {row.admissionStatus === 'PLANNED' && <Button onClick={() => admit.mutate(row.id)}>办理入院</Button>}
      {row.admissionStatus === 'ADMITTED' && <><Button onClick={() => { setSelectedAdmission(row); setTransferOpen(true) }}>转科/转床</Button><Button onClick={() => { setSelectedAdmission(row); setSurgeryOpen(true) }}>手术</Button>{hasAnyPermission(session, ['operations:bill:create']) && <Button onClick={() => createAdmissionBill.mutate(row)}>住院费用</Button>}<Button danger onClick={() => { setSelectedAdmission(row); setDischargeOpen(true) }}>出院</Button></>}
      {row.admissionStatus === 'DISCHARGED' && <Tag>已归档只读</Tag>}
    </Space> },
  ]
  const emergencyColumns: TableColumnsType<EmergencyTriage> = [
    { title: '分诊号', dataIndex: 'triageNo' }, { title: '患者编号', dataIndex: 'patientId' },
    { title: '级别', dataIndex: 'triageLevel', sorter: (a, b) => a.triageLevel - b.triageLevel, render: value => { const meta = triageMeta[value as keyof typeof triageMeta]; return <Tag color={meta?.[1]}>{meta?.[0]}</Tag> } },
    { title: '主诉', dataIndex: 'chiefComplaint' }, { title: '分诊时间', dataIndex: 'triageTime', render: value => dayjs(value).format('YYYY-MM-DD HH:mm') },
    { title: '状态', dataIndex: 'triageStatus' },
    { title: '操作', render: (_, row) => <Space wrap>{row.triageStatus === 'WAITING' && <Button onClick={() => updateTriage.mutate({ id: row.id, status: 'IN_TREATMENT' })}>接诊</Button>}{row.triageStatus === 'IN_TREATMENT' && <Button onClick={() => updateTriage.mutate({ id: row.id, status: 'OBSERVATION' })}>转留观</Button>}{!['COMPLETED', 'CANCELLED'].includes(row.triageStatus) && <Button onClick={() => { setSelectedTriage(row); setEmergencyCareOpen(true) }}>抢救/留观</Button>}{!['COMPLETED', 'CANCELLED'].includes(row.triageStatus) && <Button onClick={() => updateTriage.mutate({ id: row.id, status: 'COMPLETED' })}>完成</Button>}</Space> },
  ]
  const surgeryColumns: TableColumnsType<SurgeryCase> = [
    { title: '手术号', dataIndex: 'surgeryNo' }, { title: '术式', dataIndex: 'surgeryName' },
    { title: '计划时间', dataIndex: 'plannedStartTime', render: value => dayjs(value).format('YYYY-MM-DD HH:mm') },
    { title: '手术间', dataIndex: 'operatingRoom', render: value => value || '待排期' }, { title: '状态', dataIndex: 'surgeryStatus' },
    { title: '操作', render: (_, row) => <Space>{row.surgeryStatus === 'APPLIED' && <Button onClick={() => { setSelectedSurgery(row); setSurgeryScheduleOpen(true) }}>排程</Button>}{row.surgeryStatus === 'SCHEDULED' && <Button onClick={() => surgeryAction.mutate({ id: row.id, action: 'start' })}>开始</Button>}{row.surgeryStatus === 'IN_PROGRESS' && <Button onClick={() => surgeryAction.mutate({ id: row.id, action: 'complete' })}>完成</Button>}</Space> },
  ]

  return <>
    <div className="page-heading"><div><h1>住院与急诊</h1><p>入院、转科转床、出院归档、手术和急诊分级队列。</p></div></div>
    {error && <Alert type="error" showIcon closable message={error} onClose={() => setError('')} />}
    <Tabs items={[
      { key: 'inpatient', label: '住院管理', children: <Card title="住院患者"><Table rowKey="id" columns={admissionColumns} dataSource={admissions.data?.records || []} loading={admissions.isLoading} locale={{ emptyText: admissions.isError ? '住院服务连接失败' : '暂无住院记录' }} pagination={{ current: admissionPage, pageSize: 20, total: admissions.data?.total || 0, onChange: setAdmissionPage, showSizeChanger: false }} /></Card> },
      { key: 'emergency', label: '急诊队列', children: <Card title="急诊分级" extra={<Button type="primary" onClick={() => setTriageOpen(true)}>新增分诊</Button>}><Table rowKey="id" columns={emergencyColumns} dataSource={emergency.data || []} loading={emergency.isLoading} pagination={false} locale={{ emptyText: emergency.isError ? '急诊服务连接失败' : '当前无候诊患者' }} /></Card> },
    ]} />
    <Modal title="转科/转床" open={transferOpen} onCancel={() => setTransferOpen(false)} onOk={() => transferForm.submit()} confirmLoading={transfer.isPending}>
      <Form form={transferForm} layout="vertical" onFinish={values => transfer.mutate(values)}><Form.Item name="targetBedId" label="目标床位" rules={[{ required: true }]}><Select loading={beds.isLoading} options={(beds.data || []).map(bed => ({ value: bed.id, label: `${bed.wardName} ${bed.bedNo}床（${bed.bedType}）` }))} onChange={id => { const bed = beds.data?.find(item => item.id === id); if (bed) transferForm.setFieldsValue({ targetDeptId: bed.deptId, targetWardId: bed.wardId }) }} /></Form.Item><Form.Item name="targetDeptId" label="目标科室 ID" rules={[{ required: true }]}><InputNumber disabled style={{ width: '100%' }} /></Form.Item><Form.Item name="targetWardId" label="目标病区 ID" rules={[{ required: true }]}><InputNumber disabled style={{ width: '100%' }} /></Form.Item><Form.Item name="reason" label="转科原因" rules={[{ required: true }]}><Input.TextArea /></Form.Item></Form>
    </Modal>
    <Modal title="办理出院并归档" open={dischargeOpen} onCancel={() => setDischargeOpen(false)} onOk={() => dischargeForm.submit()} confirmLoading={discharge.isPending}><Form form={dischargeForm} layout="vertical" onFinish={values => discharge.mutate(values)}><Form.Item name="dischargeType" label="出院类型" initialValue="NORMAL" rules={[{ required: true }]}><Select options={[{ value: 'NORMAL', label: '正常出院' }, { value: 'TRANSFER', label: '转院' }, { value: 'DEATH', label: '死亡' }]} /></Form.Item><Form.Item name="dischargeSummary" label="出院小结" rules={[{ required: true }]}><Input.TextArea rows={5} /></Form.Item></Form></Modal>
    <Modal title="新增急诊分诊" open={triageOpen} onCancel={() => setTriageOpen(false)} onOk={() => triageForm.submit()} confirmLoading={triage.isPending}><Form form={triageForm} layout="vertical" onFinish={values => triage.mutate(values)}><Form.Item name="patientId" label="患者编号" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item><Form.Item name="triageLevel" label="分诊级别" rules={[{ required: true }]}><Select options={[1, 2, 3, 4].map(value => ({ value, label: triageMeta[value as keyof typeof triageMeta][0] }))} /></Form.Item><Form.Item name="chiefComplaint" label="主诉" rules={[{ required: true }]}><Input.TextArea /></Form.Item><Form.Item name="vitalSigns" label="生命体征"><Input placeholder="体温、脉搏、血压、血氧" /></Form.Item></Form></Modal>
    <Drawer width={900} title={`手术管理 · ${selectedAdmission?.admissionNo || ''}`} open={surgeryOpen} onClose={() => setSurgeryOpen(false)} extra={<Button type="primary" onClick={() => setSurgeryApplyOpen(true)}>申请手术</Button>}><Descriptions size="small" column={2} items={[{ key: 'patient', label: '患者', children: selectedAdmission?.patientName || selectedAdmission?.patientId }, { key: 'status', label: '住院状态', children: '在院' }]} /><Table style={{ marginTop: 16 }} rowKey="id" columns={surgeryColumns} dataSource={surgeries.data || []} loading={surgeries.isLoading} pagination={false} /></Drawer>
    <Modal title="申请手术" open={surgeryApplyOpen} onCancel={() => setSurgeryApplyOpen(false)} footer={null}><Form layout="vertical" onFinish={values => applySurgery.mutate(values)}><Form.Item name="surgeryName" label="手术名称" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="plannedStartTime" label="计划时间" rules={[{ required: true }]}><DatePicker showTime style={{ width: '100%' }} /></Form.Item><Form.Item name="surgeonId" label="主刀医生 ID" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item><Button htmlType="submit" type="primary" loading={applySurgery.isPending}>提交申请</Button></Form></Modal>
    <Modal title="手术排程" open={surgeryScheduleOpen} onCancel={() => setSurgeryScheduleOpen(false)} footer={null}><Form layout="vertical" onFinish={values => scheduleSurgery.mutate(values)} initialValues={{ plannedStartTime: selectedSurgery ? dayjs(selectedSurgery.plannedStartTime) : undefined }}><Form.Item name="plannedStartTime" label="手术时间" rules={[{ required: true }]}><DatePicker showTime style={{ width: '100%' }} /></Form.Item><Form.Item name="operatingRoom" label="手术间" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="anesthetistId" label="麻醉医生 ID" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item><Form.Item name="anesthesiaMethod" label="麻醉方式" rules={[{ required: true }]}><Input /></Form.Item><Button htmlType="submit" type="primary" loading={scheduleSurgery.isPending}>确认排程</Button></Form></Modal>
    <Drawer width={920} title={`急诊处置 · ${selectedTriage?.triageNo || ''}`} open={emergencyCareOpen} onClose={() => setEmergencyCareOpen(false)} extra={<Space><Button danger onClick={() => setResuscitationOpen(true)}>发起抢救</Button><Button onClick={() => setObservationOpen(true)}>登记留观</Button></Space>}><h3>抢救记录</h3><Table rowKey="id" size="small" pagination={false} dataSource={resuscitations.data || []} columns={[{ title: '开始时间', dataIndex: 'startTime', render: value => dayjs(value).format('YYYY-MM-DD HH:mm') }, { title: '处置', dataIndex: 'procedures' }, { title: '状态', dataIndex: 'resuscitationStatus' }, { title: '操作', render: (_, row: ResuscitationRecord) => row.resuscitationStatus === 'IN_PROGRESS' ? <Button onClick={() => completeResuscitation.mutate(row)}>结束抢救</Button> : '已归档' }]} /><h3 style={{ marginTop: 24 }}>留观记录</h3><Table rowKey="id" size="small" pagination={false} dataSource={observations.data || []} columns={[{ title: '床号', dataIndex: 'bedNo' }, { title: '诊断', dataIndex: 'diagnosis' }, { title: '状态', dataIndex: 'observationStatus' }, { title: '操作', render: (_, row: ObservationRecord) => row.observationStatus === 'ADMITTED' ? <Button onClick={() => dischargeObservation.mutate(row)}>结束留观</Button> : '已归档' }]} /></Drawer>
    <Modal title="发起抢救" open={resuscitationOpen} onCancel={() => setResuscitationOpen(false)} footer={null}><Form layout="vertical" onFinish={values => startResuscitation.mutate(values)}><Form.Item name="procedures" label="抢救措施"><Input.TextArea /></Form.Item><Form.Item name="medications" label="用药记录"><Input.TextArea /></Form.Item><Button htmlType="submit" danger type="primary" loading={startResuscitation.isPending}>确认发起</Button></Form></Modal>
    <Modal title="登记留观" open={observationOpen} onCancel={() => setObservationOpen(false)} footer={null}><Form layout="vertical" onFinish={values => admitObservation.mutate(values)}><Form.Item name="bedNo" label="留观床号" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="diagnosis" label="诊断" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="treatmentPlan" label="治疗计划"><Input.TextArea /></Form.Item><Button htmlType="submit" type="primary" loading={admitObservation.isPending}>确认留观</Button></Form></Modal>
  </>
}
