import { AlertOutlined, MedicineBoxOutlined, PlusOutlined, PrinterOutlined, ReloadOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, DatePicker, Form, Input, InputNumber, message, Modal, Select, Space, Statistic, Table, Tabs, Tag } from 'antd'
import type { TableColumnsType } from 'antd'
import dayjs from 'dayjs'
import { useState } from 'react'
import { pharmaApi } from '@/services/pharmaApi'
import { useAuthStore } from '@/stores/authStore'
import type { Dispense, DispenseCreateRequest, DrugCatalog, DrugCatalogSaveRequest, InventoryBatch, InventoryOperationRequest, InventoryOperationType, RxReview } from '@/types/pharma'

const operationLabels: Record<InventoryOperationType, string> = { INBOUND: '入库', OUTBOUND: '出库', STOCKTAKE: '盘点', RETURN: '退药', LOSS: '报损' }
const dosageFormOptions = ['片剂', '胶囊', '颗粒剂', '口服液', '注射剂', '输液剂', '软膏剂', '喷雾剂', '滴眼剂'].map((value) => ({ value, label: value }))
const unitOptions = ['盒', '瓶', '袋', '支', '片', '粒', '包', '毫升', '克'].map((value) => ({ value, label: value }))
const antibioticLevelOptions = [{ value: 'NONE', label: '非抗菌药' }, { value: '非限制级', label: '非限制级' }, { value: '限制级', label: '限制级' }, { value: '特殊级', label: '特殊级' }]

export function PharmaPage() {
  const queryClient = useQueryClient()
  const session = useAuthStore((state) => state.session)
  const [messageApi, contextHolder] = message.useMessage()
  const [keyword, setKeyword] = useState('')
  const [drugOpen, setDrugOpen] = useState(false)
  const [inventoryOpen, setInventoryOpen] = useState(false)
  const [dispenseOpen, setDispenseOpen] = useState(false)
  const [selectedDrug, setSelectedDrug] = useState<DrugCatalog>()
  const [operationType, setOperationType] = useState<InventoryOperationType>('INBOUND')
  const [patientId, setPatientId] = useState<number>()
  const [selectedReview, setSelectedReview] = useState<RxReview>()
  const [rejectReason, setRejectReason] = useState('')
  const [drugForm] = Form.useForm<DrugCatalogSaveRequest>()
  const [inventoryForm] = Form.useForm<InventoryOperationRequest & { productionDate?: dayjs.Dayjs; expiryDate?: dayjs.Dayjs }>()
  const [dispenseForm] = Form.useForm<DispenseCreateRequest & { drugId: number; quantity: number; unit: string }>()

  const drugs = useQuery({ queryKey: ['pharma-drugs', keyword], queryFn: () => pharmaApi.queryDrugs({ page: 1, size: 100, keyword: keyword || undefined }) })
  const batches = useQuery({ queryKey: ['pharma-batches'], queryFn: () => pharmaApi.listBatches() })
  const nearExpiry = useQuery({ queryKey: ['pharma-near-expiry'], queryFn: () => pharmaApi.listNearExpiry('OPD', 90) })
  const dispenses = useQuery({ queryKey: ['pharma-dispenses', patientId], queryFn: () => pharmaApi.listDispenses(patientId!), enabled: Boolean(patientId) })
  const reviews = useQuery({ queryKey: ['pharma-reviews'], queryFn: () => pharmaApi.queryReviews({ page: 1, size: 100 }) })
  const refresh = () => Promise.all([
    queryClient.invalidateQueries({ queryKey: ['pharma-drugs'] }), queryClient.invalidateQueries({ queryKey: ['pharma-batches'] }),
    queryClient.invalidateQueries({ queryKey: ['pharma-near-expiry'] }), queryClient.invalidateQueries({ queryKey: ['pharma-dispenses'] }),
  ])
  const saveDrug = useMutation({ mutationFn: (value: DrugCatalogSaveRequest) => selectedDrug ? pharmaApi.updateDrug(selectedDrug.id, value) : pharmaApi.createDrug(value), onSuccess: async () => { messageApi.success('药品目录已保存'); setDrugOpen(false); drugForm.resetFields(); await refresh() } })
  const operate = useMutation({ mutationFn: (value: InventoryOperationRequest) => pharmaApi.operateInventory(value), onSuccess: async () => { messageApi.success(`${operationLabels[operationType]}完成`); setInventoryOpen(false); inventoryForm.resetFields(); await refresh() } })
  const dispense = useMutation({ mutationFn: pharmaApi.dispense, onSuccess: async () => { messageApi.success('发药完成，库存流水已生成'); setDispenseOpen(false); dispenseForm.resetFields(); await refresh() } })
  const approveReview = useMutation({ mutationFn: (id: number) => pharmaApi.approveReview(id, session?.userId, session?.realName || session?.username), onSuccess: async (value) => { messageApi.success('处方审核已通过'); setSelectedReview(value); await queryClient.invalidateQueries({ queryKey: ['pharma-reviews'] }) } })
  const rejectReviewMutation = useMutation({ mutationFn: ({ id, reason }: { id: number; reason: string }) => pharmaApi.rejectReview(id, reason, session?.userId, session?.realName || session?.username), onSuccess: async (value) => { messageApi.success('处方已驳回'); setSelectedReview(value); setRejectReason(''); await queryClient.invalidateQueries({ queryKey: ['pharma-reviews'] }) } })
  const loadReview = useMutation({ mutationFn: pharmaApi.getReview, onSuccess: setSelectedReview })

  const openDrug = (drug?: DrugCatalog) => {
    setSelectedDrug(drug); setDrugOpen(true)
    if (drug) drugForm.setFieldsValue(drug)
    else drugForm.setFieldsValue({ conversionFactor: 1, purchasePrice: 0, retailPrice: 0, prescriptionType: 'RX', isActive: 1 })
  }
  const openInventory = (type: InventoryOperationType, batch?: InventoryBatch) => {
    setOperationType(type); setInventoryOpen(true)
    inventoryForm.setFieldsValue({ operationType: type, drugId: batch?.drugId, batchId: batch?.id, warehouseCode: batch?.warehouseCode || 'OPD', quantity: type === 'STOCKTAKE' ? batch?.availableQuantity : undefined })
  }
  const submitInventory = (value: InventoryOperationRequest & { productionDate?: dayjs.Dayjs; expiryDate?: dayjs.Dayjs }) => operate.mutate({ ...value, operationType, productionDate: value.productionDate?.format('YYYY-MM-DD'), expiryDate: value.expiryDate?.format('YYYY-MM-DD'), operatorId: session?.userId, operatorName: session?.realName || session?.username })

  const drugColumns: TableColumnsType<DrugCatalog> = [
    { title: '药品编码', dataIndex: 'drugCode', width: 120 }, { title: '通用名', dataIndex: 'genericName', width: 150 },
    { title: '剂型/规格', render: (_, r) => `${r.dosageForm} · ${r.strength}`, width: 150 }, { title: '生产厂家', dataIndex: 'manufacturer' },
    { title: '零售价', dataIndex: 'retailPrice', render: (v) => `¥${Number(v).toFixed(4)}`, width: 100 },
    { title: '状态', dataIndex: 'isActive', render: (v) => <Tag color={v === 1 ? 'success' : 'default'}>{v === 1 ? '启用' : '停用'}</Tag>, width: 75 },
    { title: '操作', render: (_, r) => <Button size="small" onClick={() => openDrug(r)}>编辑</Button>, width: 70 },
  ]
  const batchColumns: TableColumnsType<InventoryBatch> = [
    { title: '药品', render: (_, r) => `${r.drugName || r.drugId} ${r.drugCode || ''}`, width: 180 }, { title: '库房', dataIndex: 'warehouseCode', width: 80 },
    { title: '批号', dataIndex: 'batchNo', width: 110 }, { title: '有效期', dataIndex: 'expiryDate', render: (v, r) => <Tag color={r.expired ? 'error' : r.nearExpiry ? 'warning' : 'success'}>{v}</Tag>, width: 110 },
    { title: '库存', dataIndex: 'availableQuantity', width: 90 },
    { title: '操作', render: (_, r) => <Space><Button size="small" onClick={() => openInventory('OUTBOUND', r)}>出库</Button><Button size="small" onClick={() => openInventory('STOCKTAKE', r)}>盘点</Button><Button size="small" danger onClick={() => openInventory('LOSS', r)}>报损</Button></Space>, width: 190 },
  ]
  const dispenseColumns: TableColumnsType<Dispense> = [
    { title: '发药单号', dataIndex: 'dispenseNo' }, { title: '处方 ID', dataIndex: 'prescriptionId' }, { title: '状态', dataIndex: 'dispenseStatus', render: (v) => <Tag color={v === 'DISPENSED' ? 'success' : 'default'}>{v}</Tag> },
    { title: '发药时间', dataIndex: 'dispenseTime' }, { title: '药师', dataIndex: 'pharmacistName' },
    { title: '操作', render: () => <Button size="small" icon={<PrinterOutlined />} onClick={() => window.print()}>打印</Button> },
  ]
  const reviewColumns: TableColumnsType<RxReview> = [
    { title: '审核单号', dataIndex: 'reviewNo' }, { title: '患者 ID', dataIndex: 'patientId' }, { title: '医嘱 ID', dataIndex: 'orderId' },
    { title: '提醒', render: (_, r) => <Space><Tag color="warning">警告 {r.warningCount}</Tag><Tag color="error">错误 {r.errorCount}</Tag></Space> },
    { title: '状态', dataIndex: 'reviewStatus', render: (v) => <Tag color={v === 'APPROVED' ? 'success' : v === 'REJECTED' ? 'error' : 'processing'}>{v}</Tag> },
    { title: '操作', render: (_, r) => <Button size="small" loading={loadReview.isPending && loadReview.variables === r.id} onClick={() => loadReview.mutate(r.id)}>审核/打印</Button> },
  ]

  return <>
    {contextHolder}
    <div className="page-heading patient-heading"><div><h1>药房与库存</h1><p>维护药品目录、批次库存和门诊发药，所有库存变化均生成追溯流水。</p></div><Button icon={<ReloadOutlined />} onClick={refresh}>刷新</Button></div>
    {nearExpiry.data?.length ? <Alert type="warning" showIcon icon={<AlertOutlined />} message={`有 ${nearExpiry.data.length} 个批次将在 90 天内到期`} description="请优先发放近效期批次，并及时处理无法使用的库存。" /> : null}
    <div className="metric-grid pharma-metrics"><Card><Statistic title="目录药品" value={drugs.data?.total || 0} suffix="种" /></Card><Card><Statistic title="库存批次" value={batches.data?.length || 0} suffix="批" /></Card><Card><Statistic title="近效期" value={nearExpiry.data?.length || 0} suffix="批" /></Card></div>
    <Card>
      <Tabs items={[
        { key: 'catalog', label: '药品目录', children: <><Space className="table-toolbar"><Input.Search allowClear placeholder="编码、名称或拼音" onSearch={setKeyword} /><Button type="primary" icon={<PlusOutlined />} onClick={() => openDrug()}>新增药品</Button></Space><Table rowKey="id" size="small" loading={drugs.isLoading} columns={drugColumns} dataSource={drugs.data?.records || []} pagination={false} /></> },
        { key: 'inventory', label: '库存管理', children: <><Space className="table-toolbar"><Button type="primary" icon={<PlusOutlined />} onClick={() => openInventory('INBOUND')}>药品入库</Button></Space><Table rowKey="id" size="small" loading={batches.isLoading} columns={batchColumns} dataSource={batches.data || []} pagination={false} /></> },
        { key: 'dispense', label: '门诊发药', children: <><Space className="table-toolbar"><InputNumber min={1} placeholder="患者数字 ID" value={patientId} onChange={(v) => setPatientId(v || undefined)} /><Button type="primary" icon={<MedicineBoxOutlined />} onClick={() => setDispenseOpen(true)}>处方发药</Button></Space>{!patientId ? <Alert type="info" showIcon message="输入患者数字 ID 查询发药记录" /> : <Table rowKey="id" size="small" loading={dispenses.isLoading} columns={dispenseColumns} dataSource={dispenses.data || []} pagination={false} />}</> },
        { key: 'review', label: '处方审核', children: <Table rowKey="id" size="small" loading={reviews.isLoading} columns={reviewColumns} dataSource={reviews.data?.records || []} pagination={false} /> },
      ]} />
    </Card>

    <Modal title={selectedDrug ? '编辑药品' : '新增药品'} open={drugOpen} onCancel={() => setDrugOpen(false)} onOk={() => drugForm.submit()} confirmLoading={saveDrug.isPending} width={720}>
      <Form form={drugForm} layout="vertical" onFinish={(v) => saveDrug.mutate(v)} className="pharma-grid-form">
        <Form.Item name="drugCode" label="药品编码" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="genericName" label="通用名" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="tradeName" label="商品名"><Input /></Form.Item><Form.Item name="pinyinCode" label="拼音码"><Input /></Form.Item>
        <Form.Item name="dosageForm" label="剂型" rules={[{ required: true }]}><Select showSearch options={dosageFormOptions} /></Form.Item><Form.Item name="strength" label="规格" rules={[{ required: true }]}><Input placeholder="如：500mg" /></Form.Item>
        <Form.Item name="manufacturer" label="生产厂家" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="approvalNo" label="批准文号"><Input /></Form.Item>
        <Form.Item name="packageUnit" label="包装单位" rules={[{ required: true }]}><Select showSearch options={unitOptions} /></Form.Item><Form.Item name="minUnit" label="最小单位" rules={[{ required: true }]}><Select showSearch options={unitOptions} /></Form.Item>
        <Form.Item name="conversionFactor" label="包装换算" rules={[{ required: true }]}><InputNumber min={0.0001} precision={4} /></Form.Item><Form.Item name="purchasePrice" label="进价" rules={[{ required: true }]}><InputNumber min={0} precision={4} /></Form.Item>
        <Form.Item name="retailPrice" label="零售价" rules={[{ required: true }]}><InputNumber min={0} precision={4} /></Form.Item><Form.Item name="prescriptionType" label="处方属性" rules={[{ required: true }]}><Select options={[{ value: 'RX', label: '处方药' }, { value: 'OTC', label: '非处方药' }]} /></Form.Item>
        <Form.Item name="antibioticLevel" label="抗菌药级别"><Select allowClear options={antibioticLevelOptions} placeholder="请选择（可选）" /></Form.Item>
      </Form>
    </Modal>

    <Modal title={operationLabels[operationType]} open={inventoryOpen} onCancel={() => setInventoryOpen(false)} onOk={() => inventoryForm.submit()} confirmLoading={operate.isPending}>
      <Form form={inventoryForm} layout="vertical" onFinish={submitInventory}>
        <Form.Item name="drugId" label="药品" rules={[{ required: true }]}><Select showSearch optionFilterProp="label" options={(drugs.data?.records || []).map((d) => ({ value: d.id, label: `${d.drugCode} ${d.genericName} ${d.strength}` }))} /></Form.Item>
        <Form.Item name="warehouseCode" label="库房" rules={[{ required: true }]}><Input /></Form.Item>
        {operationType !== 'INBOUND' && <Form.Item name="batchId" label="批次" rules={[{ required: true }]}><Select options={(batches.data || []).map((b) => ({ value: b.id, label: `${b.batchNo} · 库存 ${b.availableQuantity} · ${b.expiryDate}` }))} /></Form.Item>}
        {operationType === 'INBOUND' && <><Form.Item name="batchNo" label="批号" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="productionDate" label="生产日期"><DatePicker /></Form.Item><Form.Item name="expiryDate" label="有效期" rules={[{ required: true }]}><DatePicker /></Form.Item><Form.Item name="unitCost" label="批次成本" rules={[{ required: true }]}><InputNumber min={0} precision={4} /></Form.Item></>}
        <Form.Item name="quantity" label={operationType === 'STOCKTAKE' ? '盘点实数' : '数量'} rules={[{ required: true }]}><InputNumber min={0} precision={4} /></Form.Item><Form.Item name="reason" label="原因/备注"><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>

    <Modal title="处方审核后发药" open={dispenseOpen} onCancel={() => setDispenseOpen(false)} onOk={() => dispenseForm.submit()} confirmLoading={dispense.isPending}>
      <Alert type="info" showIcon message="仅已审核通过的处方可发药；系统自动按先到期先发分配批次。" />
      <Form form={dispenseForm} layout="vertical" initialValues={{ warehouseCode: 'OPD', unit: '盒' }} onFinish={(v) => dispense.mutate({ prescriptionId: v.prescriptionId, rxReviewId: v.rxReviewId, patientId: v.patientId, warehouseCode: v.warehouseCode, pharmacistId: session?.userId, pharmacistName: session?.realName || session?.username, items: [{ drugId: v.drugId, quantity: v.quantity, unit: v.unit }] })}>
        <Form.Item name="patientId" label="患者数字 ID" rules={[{ required: true }]}><InputNumber min={1} /></Form.Item><Form.Item name="prescriptionId" label="处方 ID" rules={[{ required: true }]}><InputNumber min={1} /></Form.Item>
        <Form.Item name="rxReviewId" label="审核记录 ID" rules={[{ required: true }]}><InputNumber min={1} /></Form.Item><Form.Item name="warehouseCode" label="发药库房" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="drugId" label="药品" rules={[{ required: true }]}><Select showSearch optionFilterProp="label" options={(drugs.data?.records || []).filter((d) => d.isActive === 1).map((d) => ({ value: d.id, label: `${d.drugCode} ${d.genericName} ${d.strength}` }))} /></Form.Item>
        <Form.Item name="quantity" label="发药数量" rules={[{ required: true }]}><InputNumber min={0.0001} precision={4} /></Form.Item><Form.Item name="unit" label="单位" rules={[{ required: true }]}><Input /></Form.Item>
      </Form>
    </Modal>
    <Modal title="处方审核单" open={Boolean(selectedReview)} onCancel={() => setSelectedReview(undefined)} footer={<Space><Button icon={<PrinterOutlined />} onClick={() => window.print()}>打印审核单</Button>{selectedReview?.reviewStatus !== 'APPROVED' && selectedReview?.reviewStatus !== 'REJECTED' && <><Input placeholder="驳回原因" value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} /><Button danger disabled={!rejectReason.trim()} onClick={() => rejectReviewMutation.mutate({ id: selectedReview!.id, reason: rejectReason.trim() })}>驳回</Button><Button type="primary" onClick={() => approveReview.mutate(selectedReview!.id)}>通过</Button></>}</Space>} width={720}>
      {selectedReview && <div className="pharma-review-print"><h2>处方审核单 {selectedReview.reviewNo}</h2><p>患者 ID：{selectedReview.patientId}　医嘱 ID：{selectedReview.orderId || '—'}　审核状态：{selectedReview.reviewStatus}</p><Table rowKey="id" size="small" pagination={false} dataSource={selectedReview.items || []} columns={[{ title: '级别', dataIndex: 'alertLevel' }, { title: '药品', render: (_, r) => [r.drugNameA, r.drugNameB].filter(Boolean).join(' / ') }, { title: '审核提示', dataIndex: 'alertDesc' }, { title: '建议', dataIndex: 'suggestion' }]} /></div>}
    </Modal>
  </>
}
