import { EditOutlined, EyeOutlined, PlusOutlined, SearchOutlined, TeamOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Descriptions, Drawer, Form, Input, Modal, Select, Space, Table, Tag, message } from 'antd'
import type { TableColumnsType } from 'antd'
import { useState } from 'react'
import { patientApi } from '@/services/patientApi'
import type { Patient, PatientCreateRequest, PatientUpdateRequest } from '@/types/patient'
import { maskIdNumber, maskPhone } from '@/utils/maskSensitive'

const idTypes = [{ value: 'ID_CARD', label: '居民身份证' }, { value: 'PASSPORT', label: '护照' }, { value: 'OTHER', label: '其他证件' }]
const genderText: Record<number, string> = { 0: '未知', 1: '男', 2: '女', 9: '未说明' }

export function PatientPage() {
  const [keyword, setKeyword] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)
  const [createOpen, setCreateOpen] = useState(false)
  const [selected, setSelected] = useState<Patient | null>(null)
  const [editing, setEditing] = useState<Patient | null>(null)
  const [form] = Form.useForm<PatientCreateRequest>()
  const [editForm] = Form.useForm<PatientUpdateRequest>()
  const [messageApi, messageContext] = message.useMessage()
  const queryClient = useQueryClient()

  const patients = useQuery({ queryKey: ['patients', page, size, search], queryFn: () => patientApi.search({ page, size, keyword: search || undefined }) })
  const createPatient = useMutation({
    mutationFn: patientApi.create,
    onSuccess: async (patient) => {
      messageApi.success(`患者 ${patient.name} 建档成功`)
      setCreateOpen(false)
      form.resetFields()
      await queryClient.invalidateQueries({ queryKey: ['patients'] })
    },
    onError: () => messageApi.error('患者建档失败，请检查证件是否重复或服务是否可用。'),
  })
  const updatePatient = useMutation({
    mutationFn: ({ id, request }: { id: Patient['id']; request: PatientUpdateRequest }) => patientApi.update(id, request),
    onSuccess: async (patient) => {
      messageApi.success(`患者 ${patient.name} 的资料已更新`)
      setEditing(null)
      setSelected(patient)
      await queryClient.invalidateQueries({ queryKey: ['patients'] })
    },
    onError: () => messageApi.error('患者资料保存失败，请检查输入内容或服务连接。'),
  })

  const openEdit = (patient: Patient) => {
    setEditing(patient)
    editForm.setFieldsValue({
      name: patient.name,
      gender: patient.gender,
      birthDate: patient.birthDate,
      nationality: patient.nationality,
      nation: patient.nation,
      maritalStatus: patient.maritalStatus,
      occupation: patient.occupation,
      phone: patient.phone,
      phoneBackup: patient.phoneBackup,
      address: patient.address,
      bloodType: patient.bloodType,
      allergyHistory: patient.allergyHistory,
      insuranceType: patient.insuranceType,
      insuranceNo: patient.insuranceNo,
    })
  }

  const columns: TableColumnsType<Patient> = [
    { title: '患者主索引', dataIndex: 'empiNo', width: 160, render: (value) => <strong>{value}</strong> },
    { title: '姓名', dataIndex: 'name', width: 110 },
    { title: '性别', dataIndex: 'gender', width: 70, render: (value) => genderText[value] || '未知' },
    { title: '年龄', dataIndex: 'ageDisplay', width: 90, render: (value) => value || '—' },
    { title: '证件号码', dataIndex: 'idNo', width: 190, render: maskIdNumber },
    { title: '手机号', dataIndex: 'phone', width: 140, render: maskPhone },
    { title: '医保类型', dataIndex: 'insuranceType', width: 110, render: (value) => value || '自费' },
    { title: '状态', dataIndex: 'patientStatus', width: 90, render: (value) => <Tag color={value === 'ACTIVE' ? 'success' : 'default'}>{value === 'ACTIVE' ? '正常' : value || '未知'}</Tag> },
    { title: '操作', key: 'action', width: 90, fixed: 'right', render: (_, record) => <Button type="link" icon={<EyeOutlined />} onClick={() => setSelected(record)}>详情</Button> },
  ]

  const submitSearch = () => { setPage(1); setSearch(keyword.trim()) }

  return (
    <>
      {messageContext}
      <div className="page-heading patient-heading"><div><h1>患者服务</h1><p>管理患者主索引、身份信息和就诊联系方式。</p></div><Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>患者建档</Button></div>
      <Card className="patient-table-card">
        <div className="table-toolbar"><Input allowClear value={keyword} onChange={(event) => setKeyword(event.target.value)} onPressEnter={submitSearch} prefix={<SearchOutlined />} placeholder="搜索系统 ID、EMPI、姓名、证件号或手机号" /><Button onClick={submitSearch}>查询</Button></div>
        {patients.isError && <Alert type="error" showIcon message="患者列表加载失败" description="请确认网关和患者服务已经启动。" style={{ marginBottom: 16 }} />}
        <Table<Patient> rowKey="id" columns={columns} dataSource={patients.data?.records || []} loading={patients.isLoading} scroll={{ x: 1080 }} locale={{ emptyText: '未找到符合条件的患者' }} pagination={{ current: page, pageSize: size, total: patients.data?.total || 0, showSizeChanger: true, showTotal: (total) => `共 ${total} 位患者`, onChange: (nextPage, nextSize) => { setPage(nextPage); setSize(nextSize) } }} />
      </Card>

      <Modal title="新建患者档案" open={createOpen} width={720} onCancel={() => setCreateOpen(false)} onOk={() => form.submit()} confirmLoading={createPatient.isPending} okText="确认建档" cancelText="取消" destroyOnHidden>
        <Form<PatientCreateRequest> form={form} layout="vertical" onFinish={(values) => createPatient.mutate(values)} initialValues={{ gender: 0, idType: 'ID_CARD', nationality: '中国' }}>
          <div className="patient-form-grid">
            <Form.Item label="姓名" name="name" rules={[{ required: true, message: '请输入患者姓名' }]}><Input /></Form.Item>
            <Form.Item label="性别" name="gender"><Select options={[{ value: 0, label: '未知' }, { value: 1, label: '男' }, { value: 2, label: '女' }, { value: 9, label: '未说明' }]} /></Form.Item>
            <Form.Item label="出生日期" name="birthDate"><Input type="date" /></Form.Item>
            <Form.Item label="国籍" name="nationality"><Input /></Form.Item>
            <Form.Item label="证件类型" name="idType" rules={[{ required: true }]}><Select options={idTypes} /></Form.Item>
            <Form.Item label="证件号码" name="idNo" rules={[{ required: true, message: '请输入证件号码' }]}><Input /></Form.Item>
            <Form.Item label="手机号" name="phone" rules={[{ required: true, message: '请输入手机号' }, { pattern: /^1\d{10}$/, message: '请输入有效的手机号' }]}><Input /></Form.Item>
            <Form.Item label="备用电话" name="phoneBackup"><Input /></Form.Item>
            <Form.Item label="血型" name="bloodType"><Select allowClear options={['A', 'B', 'AB', 'O', 'UNKNOWN'].map((value) => ({ value, label: value }))} /></Form.Item>
            <Form.Item label="医保类型" name="insuranceType"><Select allowClear options={[{ value: 'SELF_PAY', label: '自费' }, { value: 'EMPLOYEE', label: '职工医保' }, { value: 'RESIDENT', label: '居民医保' }]} /></Form.Item>
            <Form.Item className="form-span-2" label="联系地址" name="address"><Input /></Form.Item>
            <Form.Item className="form-span-2" label="过敏史" name="allergyHistory"><Input.TextArea rows={3} placeholder="无过敏史请填写“无”" /></Form.Item>
          </div>
        </Form>
      </Modal>

      <Drawer title="患者档案" width={520} open={Boolean(selected)} onClose={() => setSelected(null)} extra={selected && <Button icon={<EditOutlined />} onClick={() => openEdit(selected)}>编辑资料</Button>}>
        {selected && <><div className="patient-profile"><div className="patient-avatar"><TeamOutlined /></div><div><h2>{selected.name}</h2><Space><Tag>{genderText[selected.gender]}</Tag><Tag color="cyan">{selected.empiNo}</Tag></Space></div></div><Descriptions column={1} bordered size="small" items={[{ key: 'patientId', label: '系统患者 ID', children: selected.id }, { key: 'birthDate', label: '出生日期', children: selected.birthDate || '—' }, { key: 'idNo', label: '证件号码', children: maskIdNumber(selected.idNo) }, { key: 'phone', label: '手机号', children: maskPhone(selected.phone) }, { key: 'insurance', label: '医保类型', children: selected.insuranceType || '自费' }, { key: 'insuranceNo', label: '医保编号', children: selected.insuranceNo || '未登记' }, { key: 'blood', label: '血型', children: selected.bloodType || '未登记' }, { key: 'allergy', label: '过敏史', children: selected.allergyHistory || '未登记' }, { key: 'address', label: '联系地址', children: selected.address || '未登记' }]} /></>}
      </Drawer>

      <Modal title="编辑患者资料" open={Boolean(editing)} width={720} onCancel={() => setEditing(null)} onOk={() => editForm.submit()} confirmLoading={updatePatient.isPending} okText="保存修改" cancelText="取消" destroyOnHidden>
        {editing && <Alert type="info" showIcon message={`患者主索引：${editing.empiNo}`} description={`身份凭证 ${maskIdNumber(editing.idNo)} 不能在此修改；如有错误，请按医院患者主索引更正流程处理。`} style={{ marginBottom: 18 }} />}
        <Form<PatientUpdateRequest> form={editForm} layout="vertical" onFinish={(request) => editing && updatePatient.mutate({ id: editing.id, request })}>
          <div className="patient-form-grid">
            <Form.Item label="姓名" name="name" rules={[{ required: true, message: '请输入患者姓名' }]}><Input /></Form.Item>
            <Form.Item label="性别" name="gender"><Select options={[{ value: 0, label: '未知' }, { value: 1, label: '男' }, { value: 2, label: '女' }, { value: 9, label: '未说明' }]} /></Form.Item>
            <Form.Item label="出生日期" name="birthDate"><Input type="date" /></Form.Item>
            <Form.Item label="国籍" name="nationality"><Input /></Form.Item>
            <Form.Item label="手机号" name="phone" rules={[{ required: true, message: '请输入手机号' }, { pattern: /^1\d{10}$/, message: '请输入有效的手机号' }]}><Input /></Form.Item>
            <Form.Item label="备用电话" name="phoneBackup"><Input /></Form.Item>
            <Form.Item label="血型" name="bloodType"><Select allowClear options={['A', 'B', 'AB', 'O', 'UNKNOWN'].map((value) => ({ value, label: value }))} /></Form.Item>
            <Form.Item label="医保类型" name="insuranceType"><Select allowClear options={[{ value: 'SELF_PAY', label: '自费' }, { value: 'EMPLOYEE', label: '职工医保' }, { value: 'RESIDENT', label: '居民医保' }]} /></Form.Item>
            <Form.Item className="form-span-2" label="医保编号" name="insuranceNo"><Input /></Form.Item>
            <Form.Item className="form-span-2" label="联系地址" name="address"><Input /></Form.Item>
            <Form.Item className="form-span-2" label="过敏史" name="allergyHistory"><Input.TextArea rows={3} placeholder="无过敏史请填写“无”" /></Form.Item>
          </div>
        </Form>
      </Modal>
    </>
  )
}
