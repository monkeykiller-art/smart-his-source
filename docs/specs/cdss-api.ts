/**
 * CDSS 临床决策支持系统 - API 接口定义
 * 
 * 本文档定义 CDSS 服务的完整 API 接口，包括：
 * - TypeScript 类型定义（前端使用）
 * - OpenAPI 3.0 规范（后端实现参考）
 * - 请求/响应示例
 * 
 * 版本：1.0.0
 * 服务端口：8086
 * 基础路径：/api/cdss
 */

// ============================================================================
// 枚举类型
// ============================================================================

/**
 * 告警级别
 */
export enum AlertLevel {
  INFO = 'INFO',           // 信息提示，可忽略
  WARNING = 'WARNING',     // 警告，可关闭但记录
  CRITICAL = 'CRITICAL'    // 严重，强制弹窗，必须处理
}

/**
 * 药物相互作用级别
 */
export enum InteractionLevel {
  CONTRAINDICATED = 'CONTRAINDICATED',  // 禁忌
  MAJOR = 'MAJOR',                      // 严重
  MODERATE = 'MODERATE',                // 中度
  MINOR = 'MINOR'                       // 轻度
}

/**
 * 证据等级
 */
export enum EvidenceLevel {
  A = 'A',  // 高质量随机对照试验
  B = 'B',  // 低质量随机对照试验或观察性研究
  C = 'C',  // 病例系列或专家意见
  D = 'D'   // 经验或生理学原理
}

/**
 * 告警类型
 */
export enum AlertType {
  DDI = 'DDI',                          // 药物相互作用
  ALLERGY = 'ALLERGY',                  // 过敏
  DIAGNOSIS_DRUG = 'DIAGNOSIS_DRUG'    // 诊断-用药合理性
}

/**
 * 医生操作
 */
export enum DoctorAction {
  ACCEPTED = 'ACCEPTED',       // 接受建议（修改医嘱）
  OVERRIDDEN = 'OVERRIDDEN',   // 覆盖（继续）
  CANCELLED = 'CANCELLED'      // 取消医嘱
}

/**
 * 规则类型
 */
export enum RuleType {
  RECOMMENDED = 'RECOMMENDED',       // 推荐使用
  CONTRAINDICATED = 'CONTRAINDICATED', // 禁忌
  CAUTION = 'CAUTION'                // 谨慎使用
}

/**
 * 过敏交叉概率
 */
export enum CrossProbability {
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  LOW = 'LOW'
}

/**
 * 过敏严重程度
 */
export enum AllergySeverity {
  MILD = 'MILD',
  MODERATE = 'MODERATE',
  SEVERE = 'SEVERE',
  LIFE_THREATENING = 'LIFE_THREATENING'
}

/**
 * 过敏原类型
 */
export enum AllergenType {
  DRUG = 'DRUG',
  FOOD = 'FOOD',
  ENVIRONMENT = 'ENVIRONMENT'
}

// ============================================================================
// 基础类型
// ============================================================================

/**
 * 药品简要信息
 */
export interface DrugBrief {
  id: number;
  name: string;
  genericName?: string;
  code?: string;
}

/**
 * 过敏原信息
 */
export interface AllergenInfo {
  type: AllergenType;
  id: number;
  name: string;
  severity?: AllergySeverity;
}

// ============================================================================
// 药物相互作用检查
// ============================================================================

/**
 * 药物相互作用检查请求
 */
export interface InteractionCheckRequest {
  /** 患者 ID */
  patientId: number;
  
  /** 当前所有药品 ID 列表（含新开药品） */
  drugIds: number[];
  
  /** 就诊记录 ID（可选，用于记录日志） */
  encounterId?: number;
}

/**
 * 药物相互作用告警
 */
export interface InteractionAlert {
  /** 告警类型 */
  type: AlertType.DDI;
  
  /** 告警级别 */
  level: AlertLevel;
  
  /** 药品 A */
  drugA: DrugBrief;
  
  /** 药品 B */
  drugB: DrugBrief;
  
  /** 相互作用级别 */
  interactionLevel: InteractionLevel;
  
  /** 描述 */
  description: string;
  
  /** 作用机制 */
  mechanism?: string;
  
  /** 临床效应 */
  clinicalEffect?: string;
  
  /** 处理建议 */
  management?: string;
  
  /** 证据等级 */
  evidenceLevel?: EvidenceLevel;
}

/**
 * 药物相互作用检查响应
 */
export interface InteractionCheckResponse {
  /** 告警列表 */
  alerts: InteractionAlert[];
  
  /** 最高告警级别 */
  maxLevel: AlertLevel | null;
  
  /** 检查时间 */
  checkTime: string;
}

// ============================================================================
// 过敏交叉校验
// ============================================================================

/**
 * 过敏交叉校验请求
 */
export interface AllergyCheckRequest {
  /** 患者 ID */
  patientId: number;
  
  /** 待检查药品 ID */
  drugId: number;
  
  /** 就诊记录 ID（可选） */
  encounterId?: number;
}

/**
 * 患者过敏史
 */
export interface PatientAllergy {
  /** 过敏原类型 */
  type: AllergenType;
  
  /** 过敏原 ID */
  id: number;
  
  /** 过敏原名称 */
  name: string;
  
  /** 严重程度 */
  severity: AllergySeverity;
  
  /** 确认时间 */
  confirmedTime?: string;
}

/**
 * 过敏交叉告警
 */
export interface AllergyCrossAlert {
  /** 已知过敏原 */
  allergen: AllergenInfo;
  
  /** 交叉过敏药品 */
  crossDrug: DrugBrief;
  
  /** 交叉概率 */
  probability: CrossProbability;
  
  /** 描述 */
  description: string;
  
  /** 建议 */
  suggestion?: string;
}

/**
 * 过敏交叉校验响应
 */
export interface AllergyCheckResponse {
  /** 患者过敏史 */
  patientAllergies: PatientAllergy[];
  
  /** 交叉过敏告警 */
  crossAlerts: AllergyCrossAlert[];
  
  /** 是否有交叉过敏风险 */
  hasRisk: boolean;
  
  /** 检查时间 */
  checkTime: string;
}

// ============================================================================
// 诊断-用药合理性检查
// ============================================================================

/**
 * 诊断-用药合理性检查请求
 */
export interface DiagnosisDrugCheckRequest {
  /** ICD-10 诊断编码列表 */
  diagnosisCodes: string[];
  
  /** 待检查药品 ID */
  drugId: number;
  
  /** 患者 ID（可选，用于记录日志） */
  patientId?: number;
  
  /** 就诊记录 ID（可选） */
  encounterId?: number;
}

/**
 * 诊断-用药告警
 */
export interface DiagnosisDrugAlert {
  /** 告警类型 */
  type: AlertType.DIAGNOSIS_DRUG;
  
  /** 告警级别 */
  level: AlertLevel;
  
  /** 规则类型 */
  ruleType: RuleType;
  
  /** ICD-10 编码 */
  icd10Code: string;
  
  /** 诊断名称 */
  diagnosisName?: string;
  
  /** 药品信息 */
  drug: DrugBrief;
  
  /** 描述 */
  description: string;
  
  /** 证据等级 */
  evidenceLevel?: EvidenceLevel;
  
  /** 建议 */
  suggestion?: string;
}

/**
 * 诊断-用药合理性检查响应
 */
export interface DiagnosisDrugCheckResponse {
  /** 告警列表 */
  alerts: DiagnosisDrugAlert[];
  
  /** 最高告警级别 */
  maxLevel: AlertLevel | null;
  
  /** 检查时间 */
  checkTime: string;
}

// ============================================================================
// 综合检查（批量检查）
// ============================================================================

/**
 * 综合检查请求（一次性检查所有类型）
 */
export interface ComprehensiveCheckRequest {
  /** 患者 ID */
  patientId: number;
  
  /** 就诊记录 ID */
  encounterId?: number;
  
  /** 当前所有药品 ID 列表 */
  drugIds: number[];
  
  /** 诊断编码列表 */
  diagnosisCodes?: string[];
}

/**
 * 综合检查响应
 */
export interface ComprehensiveCheckResponse {
  /** 药物相互作用告警 */
  interactionAlerts: InteractionAlert[];
  
  /** 过敏交叉告警 */
  allergyAlerts: AllergyCrossAlert[];
  
  /** 诊断-用药告警 */
  diagnosisDrugAlerts: DiagnosisDrugAlert[];
  
  /** 所有告警（合并排序） */
  allAlerts: Array<InteractionAlert | AllergyCrossAlert | DiagnosisDrugAlert>;
  
  /** 最高告警级别 */
  maxLevel: AlertLevel | null;
  
  /** 检查时间 */
  checkTime: string;
}

// ============================================================================
// 告警日志
// ============================================================================

/**
 * 告警日志记录请求
 */
export interface AlertLogRequest {
  /** 患者 ID */
  patientId?: number;
  
  /** 就诊记录 ID */
  encounterId?: number;
  
  /** 告警类型 */
  alertType: AlertType;
  
  /** 告警级别 */
  alertLevel: AlertLevel;
  
  /** 涉及的药品 ID 数组 */
  drugIds: number[];
  
  /** 告警描述 */
  description: string;
  
  /** 医生操作 */
  doctorAction: DoctorAction;
  
  /** 覆盖原因（仅当 doctorAction = OVERRIDDEN 时必填） */
  overrideReason?: string;
  
  /** 医生 ID */
  doctorId: number;
}

/**
 * 告警日志记录响应
 */
export interface AlertLogResponse {
  /** 日志 ID */
  logId: number;
  
  /** 记录时间 */
  createdTime: string;
}

/**
 * 告警日志查询请求
 */
export interface AlertLogQueryRequest {
  /** 患者 ID */
  patientId?: number;
  
  /** 医生 ID */
  doctorId?: number;
  
  /** 告警类型 */
  alertType?: AlertType;
  
  /** 告警级别 */
  alertLevel?: AlertLevel;
  
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
 * 告警日志记录
 */
export interface AlertLog {
  /** 日志 ID */
  id: number;
  
  /** 患者 ID */
  patientId?: number;
  
  /** 就诊记录 ID */
  encounterId?: number;
  
  /** 告警类型 */
  alertType: AlertType;
  
  /** 告警级别 */
  alertLevel: AlertLevel;
  
  /** 涉及的药品 ID 数组 */
  drugIds: number[];
  
  /** 药品名称列表 */
  drugNames: string[];
  
  /** 告警描述 */
  description: string;
  
  /** 医生操作 */
  doctorAction: DoctorAction;
  
  /** 覆盖原因 */
  overrideReason?: string;
  
  /** 医生 ID */
  doctorId: number;
  
  /** 医生姓名 */
  doctorName: string;
  
  /** 记录时间 */
  createdTime: string;
}

/**
 * 告警日志查询响应
 */
export interface AlertLogQueryResponse {
  /** 日志列表 */
  logs: AlertLog[];
  
  /** 总数 */
  total: number;
  
  /** 当前页 */
  page: number;
  
  /** 每页大小 */
  size: number;
}

// ============================================================================
// 规则管理（管理员接口）
// ============================================================================

/**
 * 药物相互作用规则
 */
export interface DrugInteractionRule {
  /** 规则 ID */
  id: number;
  
  /** 药品 A ID */
  drugAId: number;
  
  /** 药品 A 名称 */
  drugAName: string;
  
  /** 药品 B ID */
  drugBId: number;
  
  /** 药品 B 名称 */
  drugBName: string;
  
  /** 相互作用级别 */
  interactionLevel: InteractionLevel;
  
  /** 描述 */
  description: string;
  
  /** 作用机制 */
  mechanism?: string;
  
  /** 临床效应 */
  clinicalEffect?: string;
  
  /** 处理建议 */
  management?: string;
  
  /** 证据等级 */
  evidenceLevel?: EvidenceLevel;
  
  /** 状态 */
  status: 'ACTIVE' | 'INACTIVE';
  
  /** 创建时间 */
  createdTime: string;
  
  /** 更新时间 */
  updatedTime: string;
}

/**
 * 创建药物相互作用规则请求
 */
export interface CreateDrugInteractionRuleRequest {
  drugAId: number;
  drugBId: number;
  interactionLevel: InteractionLevel;
  description: string;
  mechanism?: string;
  clinicalEffect?: string;
  management?: string;
  evidenceLevel?: EvidenceLevel;
}

/**
 * 更新药物相互作用规则请求
 */
export interface UpdateDrugInteractionRuleRequest {
  interactionLevel?: InteractionLevel;
  description?: string;
  mechanism?: string;
  clinicalEffect?: string;
  management?: string;
  evidenceLevel?: EvidenceLevel;
  status?: 'ACTIVE' | 'INACTIVE';
}

/**
 * 诊断-用药规则
 */
export interface DiagnosisDrugRule {
  /** 规则 ID */
  id: number;
  
  /** ICD-10 编码 */
  icd10Code: string;
  
  /** 诊断名称 */
  diagnosisName?: string;
  
  /** 药品 ID（可选） */
  drugId?: number;
  
  /** 药品名称 */
  drugName?: string;
  
  /** 药品分类 ID（可选） */
  drugClassId?: number;
  
  /** 药品分类名称 */
  drugClassName?: string;
  
  /** 规则类型 */
  ruleType: RuleType;
  
  /** 描述 */
  description?: string;
  
  /** 状态 */
  status: 'ACTIVE' | 'INACTIVE';
  
  /** 创建时间 */
  createdTime: string;
}

/**
 * 过敏交叉反应规则
 */
export interface AllergyCrossRule {
  /** 规则 ID */
  id: number;
  
  /** 过敏原类型 */
  allergenType: AllergenType;
  
  /** 过敏原 ID */
  allergenId: number;
  
  /** 过敏原名称 */
  allergenName: string;
  
  /** 交叉过敏原 ID */
  crossAllergenId: number;
  
  /** 交叉过敏原名称 */
  crossAllergenName: string;
  
  /** 交叉概率 */
  crossProbability: CrossProbability;
  
  /** 描述 */
  description?: string;
  
  /** 状态 */
  status: 'ACTIVE' | 'INACTIVE';
  
  /** 创建时间 */
  createdTime: string;
}

// ============================================================================
// API 接口定义
// ============================================================================

/**
 * CDSS API 接口
 */
export interface CdssApi {
  // ---------- 药物相互作用检查 ----------
  
  /**
   * 检查药物相互作用
   * POST /api/cdss/interactions/check
   */
  checkInteractions(request: InteractionCheckRequest): Promise<InteractionCheckResponse>;
  
  // ---------- 过敏交叉校验 ----------
  
  /**
   * 检查过敏交叉反应
   * POST /api/cdss/allergy/check
   */
  checkAllergy(request: AllergyCheckRequest): Promise<AllergyCheckResponse>;
  
  // ---------- 诊断-用药合理性 ----------
  
  /**
   * 检查诊断-用药合理性
   * POST /api/cdss/diagnosis-drug/check
   */
  checkDiagnosisDrug(request: DiagnosisDrugCheckRequest): Promise<DiagnosisDrugCheckResponse>;
  
  // ---------- 综合检查 ----------
  
  /**
   * 综合检查（一次性检查所有类型）
   * POST /api/cdss/check/comprehensive
   */
  checkComprehensive(request: ComprehensiveCheckRequest): Promise<ComprehensiveCheckResponse>;
  
  // ---------- 告警日志 ----------
  
  /**
   * 记录告警日志
   * POST /api/cdss/alert-logs
   */
  createAlertLog(request: AlertLogRequest): Promise<AlertLogResponse>;
  
  /**
   * 查询告警日志
   * GET /api/cdss/alert-logs
   */
  queryAlertLogs(params: AlertLogQueryRequest): Promise<AlertLogQueryResponse>;
  
  // ---------- 规则管理（管理员） ----------
  
  /**
   * 查询药物相互作用规则列表
   * GET /api/cdss/rules/interactions
   */
  queryInteractionRules(params?: { page?: number; size?: number }): Promise<{ rules: DrugInteractionRule[]; total: number }>;
  
  /**
   * 创建药物相互作用规则
   * POST /api/cdss/rules/interactions
   */
  createInteractionRule(request: CreateDrugInteractionRuleRequest): Promise<DrugInteractionRule>;
  
  /**
   * 更新药物相互作用规则
   * PUT /api/cdss/rules/interactions/{id}
   */
  updateInteractionRule(id: number, request: UpdateDrugInteractionRuleRequest): Promise<DrugInteractionRule>;
  
  /**
   * 删除药物相互作用规则
   * DELETE /api/cdss/rules/interactions/{id}
   */
  deleteInteractionRule(id: number): Promise<void>;
  
  /**
   * 查询诊断-用药规则列表
   * GET /api/cdss/rules/diagnosis-drug
   */
  queryDiagnosisDrugRules(params?: { page?: number; size?: number }): Promise<{ rules: DiagnosisDrugRule[]; total: number }>;
  
  /**
   * 查询过敏交叉规则列表
   * GET /api/cdss/rules/allergy-cross
   */
  queryAllergyCrossRules(params?: { page?: number; size?: number }): Promise<{ rules: AllergyCrossRule[]; total: number }>;
}

// ============================================================================
// 前端 Hook 类型（React Query）
// ============================================================================

/**
 * 使用药物相互作用检查
 */
export interface UseInteractionCheckResult {
  check: (drugIds: number[], encounterId?: number) => Promise<InteractionCheckResponse>;
  isLoading: boolean;
  data?: InteractionCheckResponse;
  error?: Error;
}

/**
 * 使用过敏交叉校验
 */
export interface UseAllergyCheckResult {
  check: (drugId: number, encounterId?: number) => Promise<AllergyCheckResponse>;
  isLoading: boolean;
  data?: AllergyCheckResponse;
  error?: Error;
}

/**
 * 使用综合检查
 */
export interface UseComprehensiveCheckResult {
  check: (drugIds: number[], diagnosisCodes?: string[], encounterId?: number) => Promise<ComprehensiveCheckResponse>;
  isLoading: boolean;
  data?: ComprehensiveCheckResponse;
  error?: Error;
}

// ============================================================================
// 前端组件 Props
// ============================================================================

/**
 * CDSS 告警弹窗 Props
 */
export interface CdssAlertModalProps {
  /** 是否显示 */
  visible: boolean;
  
  /** 告警列表 */
  alerts: Array<InteractionAlert | AllergyCrossAlert | DiagnosisDrugAlert>;
  
  /** 接受建议（修改医嘱） */
  onAccept: () => void;
  
  /** 覆盖告警（继续） */
  onOverride: (reason: string) => void;
  
  /** 取消医嘱 */
  onCancel: () => void;
}

/**
 * CDSS 告警卡片 Props
 */
export interface CdssAlertCardProps {
  /** 告警信息 */
  alert: InteractionAlert | AllergyCrossAlert | DiagnosisDrugAlert;
  
  /** 是否展开详情 */
  expanded?: boolean;
  
  /** 展开/收起回调 */
  onToggle?: () => void;
}

// ============================================================================
// OpenAPI 3.0 规范（摘要）
// ============================================================================

/**
 * OpenAPI 规范摘要
 * 
 * openapi: 3.0.3
 * info:
 *   title: CDSS 临床决策支持系统 API
 *   version: 1.0.0
 *   description: 提供药物相互作用、过敏交叉校验、诊断-用药合理性检查
 * 
 * servers:
 *   - url: http://localhost:8086/api/cdss
 *     description: 开发环境
 * 
 * paths:
 *   /interactions/check:
 *     post:
 *       summary: 检查药物相互作用
 *       tags: [相互作用检查]
 *       requestBody:
 *         required: true
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/InteractionCheckRequest'
 *       responses:
 *         '200':
 *           description: 检查成功
 *           content:
 *             application/json:
 *               schema:
 *                 $ref: '#/components/schemas/InteractionCheckResponse'
 * 
 *   /allergy/check:
 *     post:
 *       summary: 检查过敏交叉反应
 *       tags: [过敏校验]
 *       requestBody:
 *         required: true
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/AllergyCheckRequest'
 *       responses:
 *         '200':
 *           description: 检查成功
 *           content:
 *             application/json:
 *               schema:
 *                 $ref: '#/components/schemas/AllergyCheckResponse'
 * 
 *   /diagnosis-drug/check:
 *     post:
 *       summary: 检查诊断-用药合理性
 *       tags: [诊断-用药校验]
 *       requestBody:
 *         required: true
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DiagnosisDrugCheckRequest'
 *       responses:
 *         '200':
 *           description: 检查成功
 *           content:
 *             application/json:
 *               schema:
 *                 $ref: '#/components/schemas/DiagnosisDrugCheckResponse'
 * 
 *   /check/comprehensive:
 *     post:
 *       summary: 综合检查（一次性检查所有类型）
 *       tags: [综合检查]
 *       requestBody:
 *         required: true
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ComprehensiveCheckRequest'
 *       responses:
 *         '200':
 *           description: 检查成功
 *           content:
 *             application/json:
 *               schema:
 *                 $ref: '#/components/schemas/ComprehensiveCheckResponse'
 * 
 *   /alert-logs:
 *     post:
 *       summary: 记录告警日志
 *       tags: [告警日志]
 *       requestBody:
 *         required: true
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/AlertLogRequest'
 *       responses:
 *         '201':
 *           description: 记录成功
 *           content:
 *             application/json:
 *               schema:
 *                 $ref: '#/components/schemas/AlertLogResponse'
 *     get:
 *       summary: 查询告警日志
 *       tags: [告警日志]
 *       parameters:
 *         - name: patientId
 *           in: query
 *           schema:
 *             type: integer
 *         - name: doctorId
 *           in: query
 *           schema:
 *             type: integer
 *         - name: alertType
 *           in: query
 *           schema:
 *             $ref: '#/components/schemas/AlertType'
 *         - name: startDate
 *           in: query
 *           schema:
 *             type: string
 *             format: date
 *         - name: endDate
 *           in: query
 *           schema:
 *             type: string
 *             format: date
 *         - name: page
 *           in: query
 *           schema:
 *             type: integer
 *             default: 1
 *         - name: size
 *           in: query
 *           schema:
 *             type: integer
 *             default: 20
 *       responses:
 *         '200':
 *           description: 查询成功
 *           content:
 *             application/json:
 *               schema:
 *                 $ref: '#/components/schemas/AlertLogQueryResponse'
 * 
 * components:
 *   schemas:
 *     # 所有类型定义见上方 TypeScript 接口
 */

// ============================================================================
// 使用示例
// ============================================================================

/**
 * 示例 1：药物相互作用检查
 * 
 * ```typescript
 * const api = useCdssApi();
 * 
 * const response = await api.checkInteractions({
 *   patientId: 2099000000000000001,
 *   drugIds: [101, 102, 103],
 *   encounterId: 5001
 * });
 * 
 * if (response.maxLevel === AlertLevel.CRITICAL) {
 *   // 显示强制弹窗
 *   showCdssAlertModal(response.alerts);
 * } else if (response.maxLevel === AlertLevel.WARNING) {
 *   // 显示警告提示
 *   showWarning(response.alerts);
 * }
 * ```
 */

/**
 * 示例 2：综合检查
 * 
 * ```typescript
 * const response = await api.checkComprehensive({
 *   patientId: 2099000000000000001,
 *   encounterId: 5001,
 *   drugIds: [101, 102],
 *   diagnosisCodes: ['I10']  // 高血压
 * });
 * 
 * // 合并所有告警
 * const allAlerts = response.allAlerts;
 * 
 * // 按级别排序
 * allAlerts.sort((a, b) => {
 *   const levelOrder = { CRITICAL: 0, WARNING: 1, INFO: 2 };
 *   return levelOrder[a.level] - levelOrder[b.level];
 * });
 * ```
 */

/**
 * 示例 3：记录告警日志
 * 
 * ```typescript
 * // 医生覆盖了 CRITICAL 告警
 * await api.createAlertLog({
 *   patientId: 2099000000000000001,
 *   encounterId: 5001,
 *   alertType: AlertType.DDI,
 *   alertLevel: AlertLevel.CRITICAL,
 *   drugIds: [101, 102],
 *   description: '华法林与阿司匹林合用增加出血风险',
 *   doctorAction: DoctorAction.OVERRIDDEN,
 *   overrideReason: '患者机械瓣膜置换术后，必须抗凝+抗血小板',
 *   doctorId: 201
 * });
 * ```
 */
