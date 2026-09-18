/**
 * LIS 检验信息系统 - API 接口定义
 *
 * 本文档定义 LIS 检验模块的完整 API 接口，包括：
 * - TypeScript 类型定义（前端使用）
 * - OpenAPI 3.0 规范摘要（后端实现参考）
 * - 请求/响应示例
 *
 * 模块挂载在 his-clinical 服务（端口 8083）
 * 基础路径：/api/clinical
 * 版本：1.0.0
 */

// ============================================================================
// 枚举类型
// ============================================================================

/**
 * 样本状态
 */
export enum SampleStatus {
  PENDING = 'PENDING',           // 待采集
  COLLECTED = 'COLLECTED',       // 已采集
  RECEIVED = 'RECEIVED',         // 已接收
  IN_PROGRESS = 'IN_PROGRESS',   // 检验中
  COMPLETED = 'COMPLETED',       // 已完成
  REJECTED = 'REJECTED'          // 已拒收
}

/**
 * 结果状态
 */
export enum ResultStatus {
  PRELIMINARY = 'PRELIMINARY',   // 初步结果
  REVIEWED = 'REVIEWED',         // 已审核
  FINAL = 'FINAL',               // 最终结果
  AMENDED = 'AMENDED'            // 已修改
}

/**
 * 报告状态
 */
export enum ReportStatus {
  DRAFT = 'DRAFT',
  FINAL = 'FINAL',
  AMENDED = 'AMENDED',
  CANCELLED = 'CANCELLED'
}

/**
 * 检验申请状态
 */
export enum ExamRequestStatus {
  APPLIED = 'APPLIED',
  SAMPLE_COLLECTED = 'SAMPLE_COLLECTED',
  SAMPLE_RECEIVED = 'SAMPLE_RECEIVED',
  IN_PROGRESS = 'IN_PROGRESS',
  RESULT_REPORTED = 'RESULT_REPORTED',
  REPORT_REVIEWED = 'REPORT_REVIEWED',
  FINAL = 'FINAL',
  CANCELLED = 'CANCELLED'
}

/**
 * 样本类型
 */
export enum SampleType {
  BLOOD = 'BLOOD',             // 血液
  URINE = 'URINE',             // 尿液
  STOOL = 'STOOL',             // 粪便
  CSF = 'CSF',                 // 脑脊液
  BODY_FLUID = 'BODY_FLUID',   // 体液
  SECRETION = 'SECRETION',     // 分泌物
  EXUDATE = 'EXUDATE',         // 渗出液
  TISSUE = 'TISSUE'            // 组织
}

/**
 * 容器类型
 */
export enum ContainerType {
  EDTA_VACUUM = 'EDTA_VACUUM',         // EDTA 真空管（紫头，血常规）
  CITRATE_VACUUM = 'CITRATE_VACUUM',   // 枸橼酸钠真空管（蓝头，凝血）
  GEL_VACUUM = 'GEL_VACUUM',           // 促凝剂+分离胶管（黄头，生化）
  HEPARIN_VACUUM = 'HEPARIN_VACUUM',   // 肝素真空管（绿头，急诊生化）
  PLAIN_VACUUM = 'PLAIN_VACUUM',       // 普通真空管（红头，血清）
  URINE_CUP = 'URINE_CUP',             // 尿杯
  STOOL_CUP = 'STOOL_CUP',             // 便杯
  CULTURE_BOTTLE = 'CULTURE_BOTTLE'    // 培养瓶
}

/**
 * 检验项目分类
 */
export enum ExamItemCategory {
  HEMATOLOGY = 'HEMATOLOGY',         // 血常规
  BIOCHEMISTRY = 'BIOCHEMISTRY',     // 生化
  COAGULATION = 'COAGULATION',       // 凝血
  IMMUNOLOGY = 'IMMUNOLOGY',         // 免疫
  URINALYSIS = 'URINALYSIS',         // 尿常规
  ENDOCRINE = 'ENDOCRINE',           // 内分泌
  TUMOR_MARKER = 'TUMOR_MARKER',     // 肿瘤标志物
  BLOOD_GAS = 'BLOOD_GAS',           // 血气分析
  MICROBIOLOGY = 'MICROBIOLOGY'      // 微生物
}

// ============================================================================
// 基础类型
// ============================================================================

/**
 * 患者简要信息
 */
export interface PatientBrief {
  id: number;
  name: string;
  gender: string;
  birthDate: string;
  age: number;
  outpatientNo?: string;
  inpatientNo?: string;
  bedNo?: string;
  wardName?: string;
}

/**
 * 医生/技师简要信息
 */
export interface StaffBrief {
  id: number;
  name: string;
  title?: string;
  deptName?: string;
}

// ============================================================================
// 检验项目参考数据
// ============================================================================

/**
 * 检验项目
 */
export interface ExamItem {
  /** 项目 ID */
  id: number;

  /** 项目编码 */
  itemCode: string;

  /** 项目名称 */
  itemName: string;

  /** 分类 */
  itemCategory: ExamItemCategory;

  /** 样本类型 */
  sampleType: SampleType;

  /** 容器类型 */
  containerType: ContainerType;

  /** 参考范围 */
  referenceRange: string;

  /** 单位 */
  unit: string;

  /** 价格 */
  price: number;

  /** 周转时间（分钟） */
  tatMinutes: number;

  /** 状态 */
  status: 'ACTIVE' | 'INACTIVE';

  /** 创建时间 */
  createdTime: string;
}

/**
 * 检验项目查询参数
 */
export interface ExamItemQueryParams {
  category?: ExamItemCategory;
  sampleType?: SampleType;
  keyword?: string;
  status?: 'ACTIVE' | 'INACTIVE';
  page?: number;
  size?: number;
}

/**
 * 检验项目查询响应
 */
export interface ExamItemQueryResponse {
  items: ExamItem[];
  total: number;
  page: number;
  size: number;
}

// ============================================================================
// 样本管理
// ============================================================================

/**
 * 创建样本请求
 */
export interface CreateSampleRequest {
  /** 样本类型 */
  sampleType: SampleType;

  /** 容器类型 */
  containerType: ContainerType;

  /** 数量 */
  quantity: number;

  /** 备注 */
  notes?: string;
}

/**
 * 创建样本响应
 */
export interface CreateSampleResponse {
  /** 样本 ID 列表 */
  sampleIds: number[];

  /** 条码列表 */
  barcodes: string[];

  /** 是否需要打印标签 */
  printLabel: boolean;
}

/**
 * 样本详情
 */
export interface ExamSample {
  /** 样本 ID */
  id: number;

  /** 申请 ID */
  requestId: number;

  /** 样本条码 */
  sampleBarcode: string;

  /** 样本类型 */
  sampleType: SampleType;

  /** 容器类型 */
  containerType: ContainerType;

  /** 采样时间 */
  collectionTime?: string;

  /** 采样人 */
  collectedBy?: StaffBrief;

  /** 接收时间 */
  receivedTime?: string;

  /** 接收人 */
  receivedBy?: StaffBrief;

  /** 样本状态 */
  status: SampleStatus;

  /** 拒收原因 */
  rejectionReason?: string;

  /** 存放位置 */
  storageLocation?: string;

  /** 备注 */
  notes?: string;

  /** 患者信息（冗余） */
  patient: PatientBrief;

  /** 检验项目名称 */
  examName: string;

  /** 申请医生 */
  requestingDoctor: StaffBrief;

  /** 优先级 */
  priority: 'ROUTINE' | 'URGENT' | 'STAT';

  /** 创建时间 */
  createdTime: string;

  /** 更新时间 */
  updatedTime: string;
}

/**
 * 样本列表查询参数
 */
export interface SampleQueryParams {
  /** 样本状态 */
  status?: SampleStatus;

  /** 日期 */
  date?: string;

  /** 开始日期 */
  startDate?: string;

  /** 结束日期 */
  endDate?: string;

  /** 样本类型 */
  sampleType?: SampleType;

  /** 关键字（条码、患者姓名） */
  keyword?: string;

  /** 页码 */
  page?: number;

  /** 每页大小 */
  size?: number;
}

/**
 * 样本列表查询响应
 */
export interface SampleQueryResponse {
  samples: ExamSample[];
  total: number;
  page: number;
  size: number;
}

/**
 * 接收样本请求
 */
export interface ReceiveSampleRequest {
  /** 接收人 ID */
  receivedBy: number;

  /** 接收时间 */
  receivedTime: string;

  /** 存放位置 */
  storageLocation?: string;

  /** 备注 */
  notes?: string;
}

/**
 * 拒收样本请求
 */
export interface RejectSampleRequest {
  /** 拒收原因 */
  rejectionReason: string;

  /** 操作人 ID */
  rejectedBy: number;
}

/**
 * 按条码查询样本
 */
export interface SampleByBarcodeResponse {
  /** 样本信息 */
  sample: ExamSample;

  /** 关联的检验项目列表 */
  examItems: Array<{
    itemCode: string;
    itemName: string;
    sampleType: SampleType;
    referenceRange: string;
    unit: string;
  }>;
}

// ============================================================================
// 条码管理
// ============================================================================

/**
 * 批量生成条码请求
 */
export interface GenerateBarcodeRequest {
  /** 申请 ID 列表 */
  requestIds: number[];
}

/**
 * 条码信息
 */
export interface BarcodeInfo {
  /** 申请 ID */
  requestId: number;

  /** 样本 ID */
  sampleId: number;

  /** 条码 */
  barcode: string;

  /** 患者姓名 */
  patientName: string;

  /** 检验项目名称 */
  examName: string;

  /** 样本类型 */
  sampleType: SampleType;

  /** 容器类型 */
  containerType: ContainerType;
}

/**
 * 批量生成条码响应
 */
export interface GenerateBarcodeResponse {
  barcodes: BarcodeInfo[];
}

/**
 * 条码标签 PDF 信息
 */
export interface SampleLabel {
  /** 患者姓名 */
  patientName: string;

  /** 性别 */
  gender: string;

  /** 年龄 */
  age: number;

  /** 住院号/门诊号 */
  patientNo: string;

  /** 条码 */
  barcode: string;

  /** 检验项目名称 */
  examName: string;

  /** 样本类型 */
  sampleType: SampleType;

  /** 容器类型 */
  containerType: ContainerType;

  /** 采样时间 */
  collectionTime: string;

  /** 申请医生 */
  doctorName: string;

  /** 床位号（住院） */
  bedNo?: string;
}

// ============================================================================
// 检验结果
// ============================================================================

/**
 * 单项检验结果
 */
export interface ExamResultItem {
  /** 项目编码 */
  itemCode: string;

  /** 项目名称 */
  itemName?: string;

  /** 结果值 */
  resultValue: string;

  /** 单位 */
  unit: string;

  /** 参考范围 */
  referenceRange: string;

  /** 是否异常 */
  abnormal: boolean;

  /** 异常标记（如 H/L/HH/LL） */
  abnormalFlag?: string;

  /** 是否危急值 */
  criticalValue?: boolean;
}

/**
 * 批量录入结果请求
 */
export interface SubmitResultsRequest {
  /** 申请 ID */
  requestId: number;

  /** 结果列表 */
  results: ExamResultItem[];

  /** 仪器编号（预留） */
  instrumentCode?: string;

  /** 检验技师 ID */
  technicianId: number;

  /** 备注 */
  notes?: string;
}

/**
 * 检验结果详情
 */
export interface ExamResult {
  /** 结果 ID */
  id: number;

  /** 申请 ID */
  requestId: number;

  /** 患者信息 */
  patient: PatientBrief;

  /** 检验项目名称 */
  examName: string;

  /** 样本条码 */
  sampleBarcode: string;

  /** 结果列表 */
  items: ExamResultItem[];

  /** 结果状态 */
  resultStatus: ResultStatus;

  /** 仪器编号 */
  instrumentCode?: string;

  /** 检验技师 */
  technician: StaffBrief;

  /** 审核人 */
  reviewer?: StaffBrief;

  /** 审核时间 */
  reviewedTime?: string;

  /** 第二审核人 */
  reviewer2?: StaffBrief;

  /** 第二审核时间 */
  reviewer2Time?: string;

  /** 是否已生成报告 */
  reportGenerated: boolean;

  /** 报告 ID */
  reportId?: number;

  /** 录入时间 */
  createdTime: string;

  /** 更新时间 */
  updatedTime: string;
}

/**
 * 结果列表查询参数
 */
export interface ResultQueryParams {
  /** 申请 ID */
  requestId?: number;

  /** 结果状态 */
  resultStatus?: ResultStatus;

  /** 技师 ID */
  technicianId?: number;

  /** 开始日期 */
  startDate?: string;

  /** 结束日期 */
  endDate?: string;

  /** 页码 */
  page?: number;

  /** 每页大小 */
  size?: number;
}

/**
 * 结果列表查询响应
 */
export interface ResultQueryResponse {
  results: ExamResult[];
  total: number;
  page: number;
  size: number;
}

// ============================================================================
// 结果审核
// ============================================================================

/**
 * 审核结果请求
 */
export interface ReviewResultRequest {
  /** 审核人 ID */
  reviewerId: number;

  /** 第二审核人 ID（危急值或特殊项目需要） */
  reviewer2Id?: number;

  /** 审核结论 */
  conclusion: string;

  /** 是否危急值 */
  criticalValue: boolean;
}

/**
 * 确认危急值请求
 */
export interface ConfirmCriticalValueRequest {
  /** 确认人 ID（临床医生） */
  confirmedBy: number;

  /** 确认时间 */
  confirmedTime: string;

  /** 处理措施 */
  action: string;
}

/**
 * 修改结果请求
 */
export interface AmendResultRequest {
  /** 修改原因 */
  amendReason: string;

  /** 修改后的结果列表 */
  results: ExamResultItem[];

  /** 操作人 ID */
  amendedBy: number;
}

// ============================================================================
// 检验报告
// ============================================================================

/**
 * 生成报告请求
 */
export interface GenerateReportRequest {
  /** 报告人 ID */
  reporterId: number;

  /** 审核人 ID */
  reviewerId: number;

  /** 结论/诊断建议 */
  conclusion?: string;
}

/**
 * 生成报告响应
 */
export interface GenerateReportResponse {
  /** 报告 ID */
  reportId: number;

  /** 报告编号 */
  reportNo: string;

  /** 报告时间 */
  reportTime: string;

  /** 状态 */
  status: ReportStatus;
}

/**
 * 检验报告详情
 */
export interface ExamReport {
  /** 报告 ID */
  id: number;

  /** 申请 ID */
  requestId: number;

  /** 报告编号 */
  reportNo: string;

  /** 报告时间 */
  reportTime: string;

  /** 患者信息 */
  patient: PatientBrief;

  /** 检验项目名称 */
  examName: string;

  /** 结果摘要 */
  resultSummary: string;

  /** 结果明细 */
  resultItems: ExamResultItem[];

  /** 结论/诊断建议 */
  conclusion?: string;

  /** 是否危急值 */
  criticalValue: boolean;

  /** 危急值是否已确认 */
  criticalValueConfirmed?: boolean;

  /** 危急值确认人 */
  criticalValueConfirmedBy?: StaffBrief;

  /** 危急值确认时间 */
  criticalValueConfirmedTime?: string;

  /** 报告人 */
  reporter: StaffBrief;

  /** 审核人 */
  reviewer: StaffBrief;

  /** 第二审核人 */
  reviewer2?: StaffBrief;

  /** 打印状态 */
  printed: boolean;

  /** 打印时间 */
  printedTime?: string;

  /** 打印人 */
  printedBy?: StaffBrief;

  /** 打印次数 */
  printCount: number;

  /** 报告状态 */
  status: ReportStatus;

  /** 创建时间 */
  createdTime: string;

  /** 更新时间 */
  updatedTime: string;
}

/**
 * 报告列表查询参数
 */
export interface ReportQueryParams {
  /** 患者 ID */
  patientId?: number;

  /** 报告状态 */
  status?: ReportStatus;

  /** 开始日期 */
  startDate?: string;

  /** 结束日期 */
  endDate?: string;

  /** 关键字（报告编号、患者姓名） */
  keyword?: string;

  /** 页码 */
  page?: number;

  /** 每页大小 */
  size?: number;
}

/**
 * 报告列表查询响应
 */
export interface ReportQueryResponse {
  reports: ExamReport[];
  total: number;
  page: number;
  size: number;
}

/**
 * 报告打印记录
 */
export interface ReportPrintRecord {
  printedTime: string;
  printedBy: StaffBrief;
  printCount: number;
}

// ============================================================================
// 检验科工作台
// ============================================================================

/**
 * 危急值信息
 */
export interface CriticalValueInfo {
  /** 申请 ID */
  requestId: number;

  /** 结果 ID */
  resultId: number;

  /** 患者姓名 */
  patientName: string;

  /** 患者 ID */
  patientId: number;

  /** 项目编码 */
  itemCode: string;

  /** 项目名称 */
  itemName: string;

  /** 结果值 */
  resultValue: string;

  /** 单位 */
  unit: string;

  /** 参考范围 */
  referenceRange: string;

  /** 报告时间 */
  reportedTime: string;

  /** 是否已确认 */
  confirmed: boolean;
}

/**
 * TAT 统计
 */
export interface TatStats {
  /** 平均周转时间（分钟） */
  averageMinutes: number;

  /** 达标率（%） */
  withinTarget: number;

  /** 按分类统计 */
  byCategory: Array<{
    category: ExamItemCategory;
    averageMinutes: number;
    withinTarget: number;
  }>;
}

/**
 * 工作台统计数据
 */
export interface LabDashboard {
  /** 待接收样本数 */
  pendingReceive: number;

  /** 检验中样本数 */
  inProgress: number;

  /** 待审核结果数 */
  pendingReview: number;

  /** 今日完成数 */
  completedToday: number;

  /** 危急值列表 */
  criticalValues: CriticalValueInfo[];

  /** TAT 统计 */
  tatStats: TatStats;

  /** 工作量统计 */
  workloadStats: {
    /** 按技师统计 */
    byTechnician: Array<{
      technicianId: number;
      technicianName: string;
      completedCount: number;
    }>;

    /** 按分类统计 */
    byCategory: Array<{
      category: ExamItemCategory;
      count: number;
    }>;
  };
}

/**
 * 工作台列表类型
 */
export type WorklistType =
  | 'PENDING_RECEIVE'
  | 'IN_PROGRESS'
  | 'PENDING_REVIEW'
  | 'COMPLETED_TODAY';

/**
 * 工作台列表查询参数
 */
export interface WorklistQueryParams {
  /** 列表类型 */
  listType: WorklistType;

  /** 样本类型 */
  sampleType?: SampleType;

  /** 关键字 */
  keyword?: string;

  /** 页码 */
  page?: number;

  /** 每页大小 */
  size?: number;
}

// ============================================================================
// API 接口定义
// ============================================================================

/**
 * LIS 检验模块 API 接口
 */
export interface LisApi {
  // ---------- 样本管理 ----------

  /**
   * 为申请创建样本（自动生成条码）
   * POST /api/clinical/exam-requests/{requestId}/samples
   */
  createSamples(requestId: number, request: CreateSampleRequest): Promise<CreateSampleResponse>;

  /**
   * 查询样本列表
   * GET /api/clinical/exam-samples
   */
  querySamples(params: SampleQueryParams): Promise<SampleQueryResponse>;

  /**
   * 获取样本详情
   * GET /api/clinical/exam-samples/{sampleId}
   */
  getSample(sampleId: number): Promise<ExamSample>;

  /**
   * 按条码查询样本
   * GET /api/clinical/exam-samples/barcode/{barcode}
   */
  getSampleByBarcode(barcode: string): Promise<SampleByBarcodeResponse>;

  /**
   * 接收样本
   * POST /api/clinical/exam-samples/{sampleId}/receive
   */
  receiveSample(sampleId: number, request: ReceiveSampleRequest): Promise<void>;

  /**
   * 拒收样本
   * POST /api/clinical/exam-samples/{sampleId}/reject
   */
  rejectSample(sampleId: number, request: RejectSampleRequest): Promise<void>;

  // ---------- 条码管理 ----------

  /**
   * 批量生成条码
   * POST /api/clinical/exam-samples/barcode/generate
   */
  generateBarcodes(request: GenerateBarcodeRequest): Promise<GenerateBarcodeResponse>;

  /**
   * 获取样本标签 PDF
   * GET /api/clinical/exam-samples/{sampleId}/label
   */
  getSampleLabel(sampleId: number): Promise<Blob>;

  /**
   * 批量打印标签
   * POST /api/clinical/exam-samples/barcode/print
   */
  printLabels(sampleIds: number[]): Promise<Blob>;

  // ---------- 检验结果 ----------

  /**
   * 批量录入检验结果
   * POST /api/clinical/exam-results
   */
  submitResults(request: SubmitResultsRequest): Promise<ExamResult>;

  /**
   * 查询结果列表
   * GET /api/clinical/exam-results
   */
  queryResults(params: ResultQueryParams): Promise<ResultQueryResponse>;

  /**
   * 获取结果详情
   * GET /api/clinical/exam-results/{resultId}
   */
  getResult(resultId: number): Promise<ExamResult>;

  /**
   * 修改结果
   * POST /api/clinical/exam-results/{resultId}/amend
   */
  amendResult(resultId: number, request: AmendResultRequest): Promise<ExamResult>;

  // ---------- 结果审核 ----------

  /**
   * 审核结果
   * POST /api/clinical/exam-results/{resultId}/review
   */
  reviewResult(resultId: number, request: ReviewResultRequest): Promise<void>;

  /**
   * 确认危急值
   * POST /api/clinical/exam-results/{resultId}/confirm-critical
   */
  confirmCriticalValue(resultId: number, request: ConfirmCriticalValueRequest): Promise<void>;

  // ---------- 报告管理 ----------

  /**
   * 生成报告
   * POST /api/clinical/exam-reports/{requestId}/generate
   */
  generateReport(requestId: number, request: GenerateReportRequest): Promise<GenerateReportResponse>;

  /**
   * 查询报告列表
   * GET /api/clinical/exam-reports
   */
  queryReports(params: ReportQueryParams): Promise<ReportQueryResponse>;

  /**
   * 获取报告详情
   * GET /api/clinical/exam-reports/{reportId}
   */
  getReport(reportId: number): Promise<ExamReport>;

  /**
   * 获取报告 PDF
   * GET /api/clinical/exam-reports/{reportId}/pdf
   */
  getReportPdf(reportId: number): Promise<Blob>;

  /**
   * 记录报告打印
   * POST /api/clinical/exam-reports/{reportId}/print
   */
  recordReportPrint(reportId: number): Promise<ReportPrintRecord>;

  // ---------- 检验项目参考数据 ----------

  /**
   * 查询检验项目列表
   * GET /api/clinical/exam-items
   */
  queryExamItems(params?: ExamItemQueryParams): Promise<ExamItemQueryResponse>;

  /**
   * 获取检验项目详情
   * GET /api/clinical/exam-items/{itemId}
   */
  getExamItem(itemId: number): Promise<ExamItem>;

  /**
   * 按编码查询检验项目
   * GET /api/clinical/exam-items/code/{itemCode}
   */
  getExamItemByCode(itemCode: string): Promise<ExamItem>;

  // ---------- 工作台 ----------

  /**
   * 获取工作台统计
   * GET /api/clinical/lab/dashboard
   */
  getDashboard(): Promise<LabDashboard>;

  /**
   * 获取工作台列表
   * GET /api/clinical/lab/worklist
   */
  getWorklist(params: WorklistQueryParams): Promise<SampleQueryResponse>;
}

// ============================================================================
// 前端 Hook 类型（React Query）
// ============================================================================

export interface UseLabDashboardResult {
  data?: LabDashboard;
  isLoading: boolean;
  error?: Error;
  refetch: () => void;
}

export interface UseSamplesResult {
  data?: SampleQueryResponse;
  isLoading: boolean;
  error?: Error;
  refetch: () => void;
}

export interface UseSampleByBarcodeResult {
  data?: SampleByBarcodeResponse;
  isLoading: boolean;
  error?: Error;
}

export interface UseExamResultResult {
  data?: ExamResult;
  isLoading: boolean;
  error?: Error;
  submitResults: (request: SubmitResultsRequest) => Promise<void>;
  reviewResult: (request: ReviewResultRequest) => Promise<void>;
}

export interface UseExamReportResult {
  data?: ExamReport;
  isLoading: boolean;
  error?: Error;
}

// ============================================================================
// 前端组件 Props
// ============================================================================

/**
 * 条码扫描组件
 */
export interface BarcodeScannerProps {
  /** 扫描回调 */
  onScan: (barcode: string) => void;

  /** 自动聚焦 */
  autoFocus?: boolean;

  /** 占位文本 */
  placeholder?: string;
}

/**
 * 结果录入表单
 */
export interface ResultEntryFormProps {
  /** 检验项目列表 */
  examItems: ExamItem[];

  /** 已有结果（编辑模式） */
  existingResults?: ExamResultItem[];

  /** 提交回调 */
  onSubmit: (results: ExamResultItem[]) => void;
}

/**
 * 危急值提醒组件
 */
export interface CriticalValueAlertProps {
  /** 危急值列表 */
  criticalValues: CriticalValueInfo[];

  /** 确认回调 */
  onConfirm: (resultId: number) => void;
}

/**
 * 报告预览组件
 */
export interface ReportPreviewProps {
  /** 报告信息 */
  report: ExamReport;

  /** 打印回调 */
  onPrint: () => void;
}

/**
 * 工作台统计卡片
 */
export interface DashboardStatsProps {
  dashboard: LabDashboard;
}

/**
 * 样本接收确认弹窗
 */
export interface SampleReceiveConfirmProps {
  /** 样本信息 */
  sample: ExamSample;

  /** 确认回调 */
  onConfirm: (request: ReceiveSampleRequest) => void;

  /** 取消回调 */
  onCancel: () => void;
}

/**
 * 拒收原因录入弹窗
 */
export interface RejectReasonModalProps {
  /** 样本信息 */
  sample: ExamSample;

  /** 确认回调 */
  onConfirm: (reason: string) => void;

  /** 取消回调 */
  onCancel: () => void;
}

// ============================================================================
// OpenAPI 3.0 规范（摘要）
// ============================================================================

/**
 * OpenAPI 规范摘要
 *
 * openapi: 3.0.3
 * info:
 *   title: LIS 检验信息系统 API
 *   version: 1.0.0
 *   description: 样本管理、条码、结果录入审核、报告管理、工作台
 *
 * servers:
 *   - url: http://localhost:8083/api/clinical
 *     description: 开发环境
 *
 * tags:
 *   - name: 样本管理
 *   - name: 条码管理
 *   - name: 检验结果
 *   - name: 结果审核
 *   - name: 报告管理
 *   - name: 检验项目
 *   - name: 工作台
 *
 * paths:
 *   /exam-requests/{requestId}/samples:
 *     post:
 *       tags: [样本管理]
 *       summary: 创建样本（自动生成条码）
 *
 *   /exam-samples:
 *     get:
 *       tags: [样本管理]
 *       summary: 查询样本列表
 *
 *   /exam-samples/{sampleId}:
 *     get:
 *       tags: [样本管理]
 *       summary: 获取样本详情
 *
 *   /exam-samples/barcode/{barcode}:
 *     get:
 *       tags: [样本管理]
 *       summary: 按条码查询样本
 *
 *   /exam-samples/{sampleId}/receive:
 *     post:
 *       tags: [样本管理]
 *       summary: 接收样本
 *
 *   /exam-samples/{sampleId}/reject:
 *     post:
 *       tags: [样本管理]
 *       summary: 拒收样本
 *
 *   /exam-samples/barcode/generate:
 *     post:
 *       tags: [条码管理]
 *       summary: 批量生成条码
 *
 *   /exam-samples/{sampleId}/label:
 *     get:
 *       tags: [条码管理]
 *       summary: 获取样本标签 PDF
 *       produces: application/pdf
 *
 *   /exam-samples/barcode/print:
 *     post:
 *       tags: [条码管理]
 *       summary: 批量打印标签
 *       produces: application/pdf
 *
 *   /exam-results:
 *     post:
 *       tags: [检验结果]
 *       summary: 批量录入结果
 *     get:
 *       tags: [检验结果]
 *       summary: 查询结果列表
 *
 *   /exam-results/{resultId}:
 *     get:
 *       tags: [检验结果]
 *       summary: 获取结果详情
 *
 *   /exam-results/{resultId}/amend:
 *     post:
 *       tags: [检验结果]
 *       summary: 修改结果
 *
 *   /exam-results/{resultId}/review:
 *     post:
 *       tags: [结果审核]
 *       summary: 审核结果
 *
 *   /exam-results/{resultId}/confirm-critical:
 *     post:
 *       tags: [结果审核]
 *       summary: 确认危急值
 *
 *   /exam-reports/{requestId}/generate:
 *     post:
 *       tags: [报告管理]
 *       summary: 生成报告
 *
 *   /exam-reports:
 *     get:
 *       tags: [报告管理]
 *       summary: 查询报告列表
 *
 *   /exam-reports/{reportId}:
 *     get:
 *       tags: [报告管理]
 *       summary: 获取报告详情
 *
 *   /exam-reports/{reportId}/pdf:
 *     get:
 *       tags: [报告管理]
 *       summary: 获取报告 PDF
 *       produces: application/pdf
 *
 *   /exam-reports/{reportId}/print:
 *     post:
 *       tags: [报告管理]
 *       summary: 记录报告打印
 *
 *   /exam-items:
 *     get:
 *       tags: [检验项目]
 *       summary: 查询检验项目列表
 *
 *   /exam-items/{itemId}:
 *     get:
 *       tags: [检验项目]
 *       summary: 获取项目详情
 *
 *   /exam-items/code/{itemCode}:
 *     get:
 *       tags: [检验项目]
 *       summary: 按编码查询项目
 *
 *   /lab/dashboard:
 *     get:
 *       tags: [工作台]
 *       summary: 获取工作台统计
 *
 *   /lab/worklist:
 *     get:
 *       tags: [工作台]
 *       summary: 获取工作台列表
 */

// ============================================================================
// 使用示例
// ============================================================================

/**
 * 示例 1：样本接收流程
 *
 * ```typescript
 * const api = useLisApi();
 *
 * // 扫码查询样本
 * const sampleInfo = await api.getSampleByBarcode('LAB20260918001');
 *
 * // 确认接收
 * await api.receiveSample(sampleInfo.sample.id, {
 *   receivedBy: 301,
 *   receivedTime: new Date().toISOString(),
 *   storageLocation: '冰箱A-2层',
 *   notes: '样本质量良好'
 * });
 * ```
 */

/**
 * 示例 2：结果录入与审核
 *
 * ```typescript
 * // 批量录入结果
 * const result = await api.submitResults({
 *   requestId: 501,
 *   results: [
 *     { itemCode: 'WBC', resultValue: '8.5', unit: '10^9/L', referenceRange: '4.0-10.0', abnormal: false },
 *     { itemCode: 'RBC', resultValue: '4.2', unit: '10^12/L', referenceRange: '3.5-5.5', abnormal: false },
 *     { itemCode: 'HGB', resultValue: '125', unit: 'g/L', referenceRange: '110-160', abnormal: false }
 *   ],
 *   instrumentCode: 'SYSMEX-XN1000',
 *   technicianId: 401
 * });
 *
 * // 审核结果
 * await api.reviewResult(result.id, {
 *   reviewerId: 402,
 *   conclusion: '结果正常',
 *   criticalValue: false
 * });
 * ```
 */

/**
 * 示例 3：危急值处理
 *
 * ```typescript
 * // 发现危急值（如血钾 6.5 mmol/L）
 * // 系统自动弹窗提醒
 *
 * // 检验医师审核
 * await api.reviewResult(resultId, {
 *   reviewerId: 402,
 *   reviewer2Id: 403,  // 双人审核
 *   conclusion: '血钾危急值，已复核',
 *   criticalValue: true
 * });
 *
 * // 通知临床医生确认
 * await api.confirmCriticalValue(resultId, {
 *   confirmedBy: 201,
 *   confirmedTime: new Date().toISOString(),
 *   action: '已通知主管医生，调整用药方案'
 * });
 * ```
 */

/**
 * 示例 4：生成报告
 *
 * ```typescript
 * // 生成报告
 * const report = await api.generateReport(501, {
 *   reporterId: 401,
 *   reviewerId: 402,
 *   conclusion: '血常规各项指标正常'
 * });
 *
 * // 获取报告 PDF
 * const pdfBlob = await api.getReportPdf(report.reportId);
 * const url = URL.createObjectURL(pdfBlob);
 * window.open(url);
 * ```
 */

/**
 * 示例 5：工作台看板
 *
 * ```typescript
 * const dashboard = await api.getDashboard();
 *
 * // 显示统计
 * console.log(`待接收: ${dashboard.pendingReceive}`);
 * console.log(`检验中: ${dashboard.inProgress}`);
 * console.log(`待审核: ${dashboard.pendingReview}`);
 * console.log(`今日完成: ${dashboard.completedToday}`);
 *
 * // 危急值提醒
 * if (dashboard.criticalValues.length > 0) {
 *   showCriticalValueAlert(dashboard.criticalValues);
 * }
 * ```
 */
