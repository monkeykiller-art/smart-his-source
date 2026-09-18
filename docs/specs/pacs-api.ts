/**
 * PACS/RIS 医学影像系统 - API 接口定义
 *
 * 本文档定义 PACS 影像模块的完整 API 接口，包括：
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
 * 影像检查状态
 */
export enum StudyStatus {
  SCHEDULED = 'SCHEDULED',     // 已预约
  REGISTERED = 'REGISTERED',   // 已登记
  IN_PROGRESS = 'IN_PROGRESS', // 检查中
  COMPLETED = 'COMPLETED',     // 检查完成（待报告）
  REPORTED = 'REPORTED',       // 已报告（待审核）
  FINAL = 'FINAL',             // 已签发
  CANCELLED = 'CANCELLED'      // 已取消
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
 * 检查优先级
 */
export enum StudyPriority {
  ROUTINE = 'ROUTINE',   // 常规
  URGENT = 'URGENT',     // 紧急
  STAT = 'STAT'          // 特急
}

/**
 * 影像模态
 */
export enum Modality {
  CT = 'CT',     // CT
  MR = 'MR',     // 磁共振
  DR = 'DR',     // 数字化 X 线
  CR = 'CR',     // 计算机 X 线
  US = 'US',     // 超声
  NM = 'NM',     // 核医学
  PT = 'PT',     // PET
  DX = 'DX',     // 数字化 X 线摄影
  MG = 'MG',     // 乳腺钼靶
  RF = 'RF',     // 透视
 XA = 'XA'      // 血管造影
}

/**
 * 检查申请状态（来自 cli_exam_request）
 */
export enum ExamRequestStatus {
  APPLIED = 'APPLIED',
  SCHEDULED = 'SCHEDULED',
  REGISTERED = 'REGISTERED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  REPORTED = 'REPORTED',
  FINAL = 'FINAL',
  CANCELLED = 'CANCELLED'
}

/**
 * 打印状态
 */
export enum PrintStatus {
  NOT_PRINTED = 'NOT_PRINTED',
  PRINTED = 'PRINTED'
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
}

/**
 * 医生简要信息
 */
export interface DoctorBrief {
  id: number;
  name: string;
  title?: string;
  deptName?: string;
}

// ============================================================================
// 影像检查管理
// ============================================================================

/**
 * 创建影像检查请求
 */
export interface CreateImagingStudyRequest {
  /** 检查申请 ID（来自 cli_exam_request） */
  requestId: number;

  /** 影像模态 */
  modality: Modality;

  /** 检查部位 */
  bodyPart: string;

  /** 检查描述 */
  studyDescription: string;

  /** 优先级 */
  priority: StudyPriority;

  /** 预约日期 */
  scheduledDate: string;

  /** 临床诊断（可选） */
  clinicalDiagnosis?: string;

  /** 过敏史（可选） */
  allergyInfo?: string;
}

/**
 * 创建影像检查响应
 */
export interface CreateImagingStudyResponse {
  /** 检查 ID */
  id: number;

  /** DICOM StudyInstanceUID */
  studyInstanceUID: string;

  /** 状态 */
  status: StudyStatus;

  /** 创建时间 */
  createdTime: string;
}

/**
 * 影像检查详情
 */
export interface ImagingStudy {
  /** 检查 ID */
  id: number;

  /** 检查申请 ID */
  requestId: number;

  /** DICOM StudyInstanceUID */
  studyInstanceUID: string;

  /** 检查日期 */
  studyDate: string;

  /** 检查时间 */
  studyTime?: string;

  /** 影像模态 */
  modality: Modality;

  /** 检查部位 */
  bodyPart: string;

  /** 检查描述 */
  studyDescription: string;

  /** 患者信息 */
  patient: PatientBrief;

  /** 申请医生 */
  referringPhysician: DoctorBrief;

  /** 检查医生 */
  performingPhysician?: DoctorBrief;

  /** 医疗机构 */
  institutionName?: string;

  /** 检查状态 */
  status: StudyStatus;

  /** 优先级 */
  priority: StudyPriority;

  /** PACS 服务器地址 */
  pacsServerUrl?: string;

  /** PACS AE Title */
  pacsAeTitle?: string;

  /** 报告 ID */
  reportId?: number;

  /** 报告状态 */
  reportStatus?: ReportStatus;

  /** 序列数量 */
  seriesCount: number;

  /** 图像总数 */
  instanceCount: number;

  /** 创建时间 */
  createdTime: string;

  /** 更新时间 */
  updatedTime: string;
}

/**
 * 影像检查列表查询参数
 */
export interface ImagingStudyQueryParams {
  /** 患者 ID */
  patientId?: number;

  /** 影像模态 */
  modality?: Modality;

  /** 检查状态 */
  status?: StudyStatus;

  /** 优先级 */
  priority?: StudyPriority;

  /** 开始日期 */
  startDate?: string;

  /** 结束日期 */
  endDate?: string;

  /** 检查医生 ID */
  performingPhysicianId?: number;

  /** 申请医生 ID */
  referringPhysicianId?: number;

  /** 关键字（患者姓名、检查号） */
  keyword?: string;

  /** 页码 */
  page?: number;

  /** 每页大小 */
  size?: number;
}

/**
 * 影像检查列表响应
 */
export interface ImagingStudyQueryResponse {
  studies: ImagingStudy[];
  total: number;
  page: number;
  size: number;
}

/**
 * 登记检查请求
 */
export interface RegisterStudyRequest {
  /** 检查医生 ID */
  performingPhysicianId: number;

  /** 医疗机构名称 */
  institutionName?: string;

  /** 备注 */
  notes?: string;
}

/**
 * 取消检查请求
 */
export interface CancelStudyRequest {
  /** 取消原因 */
  reason: string;

  /** 操作人 ID */
  cancelledBy: number;
}

// ============================================================================
// 影像序列
// ============================================================================

/**
 * 影像序列
 */
export interface ImagingSeries {
  /** 序列 ID */
  id: number;

  /** 检查 ID */
  studyId: number;

  /** DICOM SeriesInstanceUID */
  seriesInstanceUID: string;

  /** 序列号 */
  seriesNumber: number;

  /** 序列描述 */
  seriesDescription: string;

  /** 序列模态 */
  modality: Modality;

  /** 序列部位 */
  bodyPart?: string;

  /** 图像数量 */
  instanceCount: number;

  /** 序列日期 */
  seriesDate?: string;

  /** 序列时间 */
  seriesTime?: string;

  /** SOP Class UID */
  sopClassUid?: string;

  /** 创建时间 */
  createdTime: string;
}

/**
 * 获取序列列表响应
 */
export interface ImagingSeriesListResponse {
  studyId: number;
  studyInstanceUID: string;
  series: ImagingSeries[];
}

// ============================================================================
// 影像查看器
// ============================================================================

/**
 * 影像查看器信息
 */
export interface ViewerInfo {
  /** 检查 ID */
  studyId: number;

  /** DICOM StudyInstanceUID */
  studyInstanceUID: string;

  /** OHIF Viewer URL */
  viewerUrl: string;

  /** PACS 服务器 DICOMweb URL */
  pacsServerUrl: string;

  /** 序列列表 */
  series: Array<{
    seriesInstanceUID: string;
    seriesDescription: string;
    instanceCount: number;
    modality: Modality;
  }>;

  /** 患者信息 */
  patient: PatientBrief;

  /** 检查信息 */
  studyDescription: string;
  studyDate: string;
  modality: Modality;
}

// ============================================================================
// 影像报告
// ============================================================================

/**
 * 创建影像报告请求
 */
export interface CreateImagingReportRequest {
  /** 检查 ID */
  studyId: number;

  /** 影像所见 */
  imagingFindings: string;

  /** 影像诊断 */
  imagingDiagnosis: string;

  /** 建议 */
  recommendations?: string;

  /** 是否危急发现 */
  criticalFinding: boolean;

  /** 报告模板 ID（可选） */
  templateId?: number;
}

/**
 * 影像报告详情
 */
export interface ImagingReport {
  /** 报告 ID */
  id: number;

  /** 检查 ID */
  studyId: number;

  /** 检查申请 ID */
  requestId: number;

  /** 报告编号 */
  reportNo: string;

  /** 报告时间 */
  reportTime: string;

  /** 患者信息 */
  patient: PatientBrief;

  /** 检查信息 */
  studyDescription: string;
  studyDate: string;
  modality: Modality;
  bodyPart: string;

  /** 影像所见 */
  imagingFindings: string;

  /** 影像诊断 */
  imagingDiagnosis: string;

  /** 建议 */
  recommendations?: string;

  /** 是否危急发现 */
  criticalFinding: boolean;

  /** 危急发现是否已确认 */
  criticalFindingConfirmed?: boolean;

  /** 危急发现确认人 */
  criticalFindingConfirmedBy?: DoctorBrief;

  /** 危急发现确认时间 */
  criticalFindingConfirmedTime?: string;

  /** 报告人 */
  reporter: DoctorBrief;

  /** 审核人 */
  reviewer?: DoctorBrief;

  /** 第二审核人 */
  reviewer2?: DoctorBrief;

  /** 审核时间 */
  reviewedTime?: string;

  /** 报告状态 */
  status: ReportStatus;

  /** 打印状态 */
  printed: boolean;

  /** 打印时间 */
  printedTime?: string;

  /** 打印次数 */
  printCount: number;

  /** 创建时间 */
  createdTime: string;

  /** 更新时间 */
  updatedTime: string;
}

/**
 * 审核报告请求
 */
export interface ReviewReportRequest {
  /** 审核人 ID */
  reviewerId: number;

  /** 第二审核人 ID（必要时） */
  reviewer2Id?: number;

  /** 审核结论 */
  conclusion: string;
}

/**
 * 修改报告请求（AMENDED）
 */
export interface AmendReportRequest {
  /** 修改原因 */
  amendReason: string;

  /** 修改后的影像所见 */
  imagingFindings?: string;

  /** 修改后的影像诊断 */
  imagingDiagnosis?: string;

  /** 修改后的建议 */
  recommendations?: string;

  /** 操作人 ID */
  amendedBy: number;
}

/**
 * 确认危急发现请求
 */
export interface ConfirmCriticalFindingRequest {
  /** 确认人 ID（临床医生） */
  confirmedBy: number;

  /** 确认时间 */
  confirmedTime: string;

  /** 处理措施 */
  action: string;
}

/**
 * 报告打印记录
 */
export interface ReportPrintRecord {
  /** 打印时间 */
  printedTime: string;

  /** 打印人 */
  printedBy: DoctorBrief;

  /** 打印次数 */
  printCount: number;
}

// ============================================================================
// 报告模板
// ============================================================================

/**
 * 报告模板
 */
export interface ReportTemplate {
  /** 模板 ID */
  id: number;

  /** 模板名称 */
  templateName: string;

  /** 影像模态 */
  modality: Modality;

  /** 检查部位 */
  bodyPart: string;

  /** 影像所见模板 */
  findingsTemplate: string;

  /** 常见诊断列表 */
  commonDiagnoses: string[];

  /** 常见建议列表 */
  commonRecommendations: string[];

  /** 状态 */
  status: 'ACTIVE' | 'INACTIVE';

  /** 创建时间 */
  createdTime: string;
}

/**
 * 创建报告模板请求
 */
export interface CreateReportTemplateRequest {
  templateName: string;
  modality: Modality;
  bodyPart: string;
  findingsTemplate: string;
  commonDiagnoses: string[];
  commonRecommendations: string[];
}

/**
 * 查询报告模板参数
 */
export interface ReportTemplateQueryParams {
  modality?: Modality;
  bodyPart?: string;
  status?: 'ACTIVE' | 'INACTIVE';
}

// ============================================================================
// 历史影像对比
// ============================================================================

/**
 * 创建历史对比请求
 */
export interface CreateComparisonRequest {
  /** 患者 ID */
  patientId: number;

  /** 当前检查 ID */
  currentStudyId: number;

  /** 历史检查 ID */
  previousStudyId: number;

  /** 对比说明 */
  comparisonNotes: string;
}

/**
 * 历史对比记录
 */
export interface ImagingComparison {
  /** 对比 ID */
  id: number;

  /** 患者 ID */
  patientId: number;

  /** 当前检查 */
  currentStudy: {
    id: number;
    studyInstanceUID: string;
    studyDate: string;
    modality: Modality;
    studyDescription: string;
  };

  /** 历史检查 */
  previousStudy: {
    id: number;
    studyInstanceUID: string;
    studyDate: string;
    modality: Modality;
    studyDescription: string;
  };

  /** 对比说明 */
  comparisonNotes: string;

  /** 创建人 */
  createdBy: DoctorBrief;

  /** 创建时间 */
  createdTime: string;
}

/**
 * 历史对比查询参数
 */
export interface ComparisonQueryParams {
  patientId: number;
  page?: number;
  size?: number;
}

/**
 * 历史对比查询响应
 */
export interface ComparisonQueryResponse {
  comparisons: ImagingComparison[];
  total: number;
  page: number;
  size: number;
}

/**
 * 患者历史检查列表（用于选择对比对象）
 */
export interface PatientStudyHistory {
  patientId: number;
  studies: Array<{
    id: number;
    studyInstanceUID: string;
    studyDate: string;
    modality: Modality;
    bodyPart: string;
    studyDescription: string;
    status: StudyStatus;
    reportStatus?: ReportStatus;
  }>;
}

// ============================================================================
// PACS 集成（DICOMweb 代理）
// ============================================================================

/**
 * PACS 查询参数（QIDO-RS 代理）
 */
export interface PacsQueryParams {
  /** 患者 ID（DICOM PatientID） */
  patientId?: string;

  /** 患者姓名 */
  patientName?: string;

  /** 研究日期范围 */
  studyDateRange?: [string, string];

  /** 影像模态 */
  modality?: Modality;

  /** 访问号 */
  accessionNumber?: string;

  /** StudyInstanceUID */
  studyInstanceUID?: string;

  /** 返回数量限制 */
  limit?: number;

  /** 偏移 */
  offset?: number;
}

/**
 * PACS Study 信息（来自 DICOMweb）
 */
export interface PacsStudy {
  studyInstanceUID: string;
  studyDate: string;
  studyTime?: string;
  modality: Modality;
  studyDescription: string;
  accessionNumber?: string;
  patientName: string;
  patientId: string;
  seriesCount: number;
  instanceCount: number;
}

/**
 * PACS 查询响应
 */
export interface PacsQueryResponse {
  studies: PacsStudy[];
  total: number;
}

/**
 * PACS 服务器状态
 */
export interface PacsServerStatus {
  /** 服务器 URL */
  serverUrl: string;

  /** 是否在线 */
  online: boolean;

  /** 响应时间（毫秒） */
  responseTimeMs?: number;

  /** 最后检查时间 */
  lastCheckTime: string;

  /** 错误信息 */
  errorMessage?: string;
}

// ============================================================================
// 影像工作台
// ============================================================================

/**
 * 工作台统计数据
 */
export interface ImagingDashboard {
  /** 待登记数量 */
  pendingRegister: number;

  /** 检查中数量 */
  inProgress: number;

  /** 待报告数量 */
  pendingReport: number;

  /** 待审核数量 */
  pendingReview: number;

  /** 今日完成数量 */
  completedToday: number;

  /** 危急发现列表 */
  criticalFindings: Array<{
    studyId: number;
    patientName: string;
    modality: Modality;
    studyDescription: string;
    reportedTime: string;
  }>;

  /** 工作量统计 */
  workloadStats: {
    /** 按模态统计 */
    byModality: Array<{
      modality: Modality;
      count: number;
    }>;

    /** 按医生统计 */
    byDoctor: Array<{
      doctorId: number;
      doctorName: string;
      reportCount: number;
    }>;
  };
}

/**
 * 工作台列表查询参数
 */
export interface WorklistQueryParams {
  /** 列表类型 */
  listType: 'PENDING_REGISTER' | 'IN_PROGRESS' | 'PENDING_REPORT' | 'PENDING_REVIEW';

  /** 影像模态 */
  modality?: Modality;

  /** 优先级 */
  priority?: StudyPriority;

  /** 日期 */
  date?: string;

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
 * PACS 影像模块 API 接口
 */
export interface ImagingApi {
  // ---------- 影像检查管理 ----------

  /**
   * 创建影像检查
   * POST /api/clinical/imaging/studies
   */
  createStudy(request: CreateImagingStudyRequest): Promise<CreateImagingStudyResponse>;

  /**
   * 查询影像检查列表
   * GET /api/clinical/imaging/studies
   */
  queryStudies(params: ImagingStudyQueryParams): Promise<ImagingStudyQueryResponse>;

  /**
   * 获取影像检查详情
   * GET /api/clinical/imaging/studies/{studyId}
   */
  getStudy(studyId: number): Promise<ImagingStudy>;

  /**
   * 登记检查
   * POST /api/clinical/imaging/studies/{studyId}/register
   */
  registerStudy(studyId: number, request: RegisterStudyRequest): Promise<void>;

  /**
   * 开始检查
   * POST /api/clinical/imaging/studies/{studyId}/start
   */
  startStudy(studyId: number): Promise<void>;

  /**
   * 完成检查（影像已上传）
   * POST /api/clinical/imaging/studies/{studyId}/complete
   */
  completeStudy(studyId: number): Promise<void>;

  /**
   * 取消检查
   * POST /api/clinical/imaging/studies/{studyId}/cancel
   */
  cancelStudy(studyId: number, request: CancelStudyRequest): Promise<void>;

  // ---------- 影像序列 ----------

  /**
   * 获取序列列表
   * GET /api/clinical/imaging/studies/{studyId}/series
   */
  getSeries(studyId: number): Promise<ImagingSeriesListResponse>;

  // ---------- 影像查看器 ----------

  /**
   * 获取影像查看器信息
   * GET /api/clinical/imaging/studies/{studyId}/viewer
   */
  getViewerInfo(studyId: number): Promise<ViewerInfo>;

  // ---------- 影像报告 ----------

  /**
   * 创建影像报告
   * POST /api/clinical/imaging/reports
   */
  createReport(request: CreateImagingReportRequest): Promise<ImagingReport>;

  /**
   * 获取报告详情
   * GET /api/clinical/imaging/reports/{reportId}
   */
  getReport(reportId: number): Promise<ImagingReport>;

  /**
   * 审核报告
   * POST /api/clinical/imaging/reports/{reportId}/review
   */
  reviewReport(reportId: number, request: ReviewReportRequest): Promise<void>;

  /**
   * 修改报告
   * POST /api/clinical/imaging/reports/{reportId}/amend
   */
  amendReport(reportId: number, request: AmendReportRequest): Promise<ImagingReport>;

  /**
   * 确认危急发现
   * POST /api/clinical/imaging/reports/{reportId}/confirm-critical
   */
  confirmCriticalFinding(reportId: number, request: ConfirmCriticalFindingRequest): Promise<void>;

  /**
   * 获取报告 PDF
   * GET /api/clinical/imaging/reports/{reportId}/pdf
   */
  getReportPdf(reportId: number): Promise<Blob>;

  /**
   * 记录报告打印
   * POST /api/clinical/imaging/reports/{reportId}/print
   */
  recordPrint(reportId: number): Promise<ReportPrintRecord>;

  // ---------- 报告模板 ----------

  /**
   * 查询报告模板列表
   * GET /api/clinical/imaging/report-templates
   */
  queryReportTemplates(params?: ReportTemplateQueryParams): Promise<ReportTemplate[]>;

  /**
   * 获取报告模板详情
   * GET /api/clinical/imaging/report-templates/{templateId}
   */
  getReportTemplate(templateId: number): Promise<ReportTemplate>;

  /**
   * 创建报告模板
   * POST /api/clinical/imaging/report-templates
   */
  createReportTemplate(request: CreateReportTemplateRequest): Promise<ReportTemplate>;

  /**
   * 更新报告模板
   * PUT /api/clinical/imaging/report-templates/{templateId}
   */
  updateReportTemplate(templateId: number, request: Partial<CreateReportTemplateRequest>): Promise<ReportTemplate>;

  /**
   * 删除报告模板
   * DELETE /api/clinical/imaging/report-templates/{templateId}
   */
  deleteReportTemplate(templateId: number): Promise<void>;

  // ---------- 历史影像对比 ----------

  /**
   * 获取患者历史检查列表
   * GET /api/clinical/imaging/patients/{patientId}/studies
   */
  getPatientStudyHistory(patientId: number): Promise<PatientStudyHistory>;

  /**
   * 创建历史对比记录
   * POST /api/clinical/imaging/comparisons
   */
  createComparison(request: CreateComparisonRequest): Promise<ImagingComparison>;

  /**
   * 查询历史对比记录
   * GET /api/clinical/imaging/comparisons
   */
  queryComparisons(params: ComparisonQueryParams): Promise<ComparisonQueryResponse>;

  /**
   * 获取对比详情
   * GET /api/clinical/imaging/comparisons/{comparisonId}
   */
  getComparison(comparisonId: number): Promise<ImagingComparison>;

  // ---------- PACS 集成 ----------

  /**
   * 查询 PACS 检查（QIDO-RS 代理）
   * GET /api/clinical/pacs/studies
   */
  queryPacsStudies(params: PacsQueryParams): Promise<PacsQueryResponse>;

  /**
   * 获取 PACS 服务器状态
   * GET /api/clinical/pacs/status
   */
  getPacsStatus(): Promise<PacsServerStatus>;

  // ---------- 工作台 ----------

  /**
   * 获取工作台统计
   * GET /api/clinical/imaging/dashboard
   */
  getDashboard(): Promise<ImagingDashboard>;

  /**
   * 获取工作台列表
   * GET /api/clinical/imaging/worklist
   */
  getWorklist(params: WorklistQueryParams): Promise<ImagingStudyQueryResponse>;
}

// ============================================================================
// 前端 Hook 类型（React Query）
// ============================================================================

export interface UseImagingStudiesResult {
  data?: ImagingStudyQueryResponse;
  isLoading: boolean;
  error?: Error;
  refetch: () => void;
}

export interface UseImagingViewerResult {
  data?: ViewerInfo;
  isLoading: boolean;
  error?: Error;
}

export interface UseImagingReportResult {
  data?: ImagingReport;
  isLoading: boolean;
  error?: Error;
  submitReport: (report: CreateImagingReportRequest) => Promise<void>;
  reviewReport: (request: ReviewReportRequest) => Promise<void>;
}

export interface UseImagingDashboardResult {
  data?: ImagingDashboard;
  isLoading: boolean;
  error?: Error;
  refetch: () => void;
}

// ============================================================================
// 前端组件 Props
// ============================================================================

/**
 * OHIF 影像查看器组件
 */
export interface OhifViewerProps {
  /** DICOM StudyInstanceUID */
  studyInstanceUID: string;

  /** PACS DICOMweb URL */
  pacsServerUrl: string;

  /** 撰写报告回调 */
  onReport?: () => void;

  /** 容器宽度 */
  width?: string | number;

  /** 容器高度 */
  height?: string | number;
}

/**
 * 报告编辑器组件
 */
export interface ReportEditorProps {
  /** 检查 ID */
  studyId: number;

  /** 报告模板（可选） */
  template?: ReportTemplate;

  /** 已有报告（编辑模式） */
  existingReport?: ImagingReport;

  /** 提交回调 */
  onSubmit: (report: CreateImagingReportRequest) => void;

  /** 保存草稿回调 */
  onSaveDraft?: (report: CreateImagingReportRequest) => void;
}

/**
 * 历史对比组件
 */
export interface HistoryComparisonProps {
  /** 患者 ID */
  patientId: number;

  /** 患者历史检查列表 */
  studies: PatientStudyHistory['studies'];

  /** 发起对比回调 */
  onCompare: (currentId: number, previousId: number) => void;
}

/**
 * 危急发现提醒组件
 */
export interface CriticalFindingAlertProps {
  /** 危急发现列表 */
  criticalFindings: ImagingDashboard['criticalFindings'];

  /** 确认回调 */
  onConfirm: (studyId: number) => void;
}

/**
 * 工作台统计卡片组件
 */
export interface DashboardStatsProps {
  dashboard: ImagingDashboard;
}

// ============================================================================
// OpenAPI 3.0 规范（摘要）
// ============================================================================

/**
 * OpenAPI 规范摘要
 *
 * openapi: 3.0.3
 * info:
 *   title: PACS 影像模块 API
 *   version: 1.0.0
 *   description: 医学影像检查管理、DICOMweb 集成、报告管理
 *
 * servers:
 *   - url: http://localhost:8083/api/clinical
 *     description: 开发环境
 *
 * tags:
 *   - name: 影像检查管理
 *   - name: 影像序列
 *   - name: 影像查看器
 *   - name: 影像报告
 *   - name: 报告模板
 *   - name: 历史对比
 *   - name: PACS 集成
 *   - name: 工作台
 *
 * paths:
 *   /imaging/studies:
 *     post:
 *       tags: [影像检查管理]
 *       summary: 创建影像检查
 *     get:
 *       tags: [影像检查管理]
 *       summary: 查询影像检查列表
 *
 *   /imaging/studies/{studyId}:
 *     get:
 *       tags: [影像检查管理]
 *       summary: 获取检查详情
 *
 *   /imaging/studies/{studyId}/register:
 *     post:
 *       tags: [影像检查管理]
 *       summary: 登记检查
 *
 *   /imaging/studies/{studyId}/start:
 *     post:
 *       tags: [影像检查管理]
 *       summary: 开始检查
 *
 *   /imaging/studies/{studyId}/complete:
 *     post:
 *       tags: [影像检查管理]
 *       summary: 完成检查
 *
 *   /imaging/studies/{studyId}/cancel:
 *     post:
 *       tags: [影像检查管理]
 *       summary: 取消检查
 *
 *   /imaging/studies/{studyId}/series:
 *     get:
 *       tags: [影像序列]
 *       summary: 获取序列列表
 *
 *   /imaging/studies/{studyId}/viewer:
 *     get:
 *       tags: [影像查看器]
 *       summary: 获取查看器信息
 *
 *   /imaging/reports:
 *     post:
 *       tags: [影像报告]
 *       summary: 创建报告
 *
 *   /imaging/reports/{reportId}:
 *     get:
 *       tags: [影像报告]
 *       summary: 获取报告详情
 *
 *   /imaging/reports/{reportId}/review:
 *     post:
 *       tags: [影像报告]
 *       summary: 审核报告
 *
 *   /imaging/reports/{reportId}/amend:
 *     post:
 *       tags: [影像报告]
 *       summary: 修改报告
 *
 *   /imaging/reports/{reportId}/confirm-critical:
 *     post:
 *       tags: [影像报告]
 *       summary: 确认危急发现
 *
 *   /imaging/reports/{reportId}/pdf:
 *     get:
 *       tags: [影像报告]
 *       summary: 获取报告 PDF
 *       produces: application/pdf
 *
 *   /imaging/reports/{reportId}/print:
 *     post:
 *       tags: [影像报告]
 *       summary: 记录打印
 *
 *   /imaging/report-templates:
 *     get:
 *       tags: [报告模板]
 *       summary: 查询模板列表
 *     post:
 *       tags: [报告模板]
 *       summary: 创建模板
 *
 *   /imaging/report-templates/{templateId}:
 *     get:
 *       tags: [报告模板]
 *       summary: 获取模板详情
 *     put:
 *       tags: [报告模板]
 *       summary: 更新模板
 *     delete:
 *       tags: [报告模板]
 *       summary: 删除模板
 *
 *   /imaging/patients/{patientId}/studies:
 *     get:
 *       tags: [历史对比]
 *       summary: 获取患者历史检查列表
 *
 *   /imaging/comparisons:
 *     post:
 *       tags: [历史对比]
 *       summary: 创建对比记录
 *     get:
 *       tags: [历史对比]
 *       summary: 查询对比记录
 *
 *   /imaging/comparisons/{comparisonId}:
 *     get:
 *       tags: [历史对比]
 *       summary: 获取对比详情
 *
 *   /pacs/studies:
 *     get:
 *       tags: [PACS 集成]
 *       summary: 查询 PACS 检查（QIDO-RS 代理）
 *
 *   /pacs/status:
 *     get:
 *       tags: [PACS 集成]
 *       summary: 获取 PACS 服务器状态
 *
 *   /imaging/dashboard:
 *     get:
 *       tags: [工作台]
 *       summary: 获取工作台统计
 *
 *   /imaging/worklist:
 *     get:
 *       tags: [工作台]
 *       summary: 获取工作台列表
 */

// ============================================================================
// 使用示例
// ============================================================================

/**
 * 示例 1：创建影像检查并登记
 *
 * ```typescript
 * const api = useImagingApi();
 *
 * // 创建检查
 * const study = await api.createStudy({
 *   requestId: 501,
 *   modality: Modality.CT,
 *   bodyPart: '胸部',
 *   studyDescription: '胸部 CT 平扫',
 *   priority: StudyPriority.ROUTINE,
 *   scheduledDate: '2026-09-20'
 * });
 *
 * // 登记
 * await api.registerStudy(study.id, {
 *   performingPhysicianId: 301,
 *   institutionName: 'XX 医院影像科'
 * });
 * ```
 */

/**
 * 示例 2：打开影像查看器
 *
 * ```typescript
 * const viewerInfo = await api.getViewerInfo(601);
 *
 * // 嵌入 OHIF Viewer
 * <OhifViewer
 *   studyInstanceUID={viewerInfo.studyInstanceUID}
 *   pacsServerUrl={viewerInfo.pacsServerUrl}
 *   onReport={() => navigate(`/imaging/report?studyId=601`)}
 * />
 * ```
 */

/**
 * 示例 3：撰写报告
 *
 * ```typescript
 * // 加载模板
 * const templates = await api.queryReportTemplates({
 *   modality: Modality.CT,
 *   bodyPart: '胸部'
 * });
 *
 * // 创建报告
 * const report = await api.createReport({
 *   studyId: 601,
 *   imagingFindings: '双肺纹理增多，未见明显实质性病变。',
 *   imagingDiagnosis: '胸部 CT 平扫未见明显异常。',
 *   recommendations: '建议定期复查。',
 *   criticalFinding: false,
 *   templateId: templates[0].id
 * });
 *
 * // 审核
 * await api.reviewReport(report.id, {
 *   reviewerId: 402,
 *   conclusion: '报告审核通过'
 * });
 * ```
 */

/**
 * 示例 4：历史对比
 *
 * ```typescript
 * // 获取患者历史
 * const history = await api.getPatientStudyHistory(patientId);
 *
 * // 创建对比
 * const comparison = await api.createComparison({
 *   patientId,
 *   currentStudyId: 601,
 *   previousStudyId: 580,
 *   comparisonNotes: '与 2026-06-15 胸部 CT 对比，病灶无明显变化。'
 * });
 * ```
 */
