# LIS 检验信息系统对接技术规格

> 版本：1.0-draft  
> 状态：待评审  
> 编制日期：2026-09-18  
> 依赖：M5 检验模块（ExamRequestController 已完成申请和结果回写）

---

## 一、概述

### 1.1 目标

完善检验科工作流程，支持样本接收、条码管理、检验执行、结果审核和报告打印。

### 1.2 范围

- 检验科工作台（样本接收、分类、状态看板）
- 条码管理（申请自动生成条码、扫码接收）
- 检验执行（手工录入、仪器对接预留）
- 结果审核（双人审核、危急值确认）
- 检验报告打印

### 1.3 不在范围

- 仪器数据自动对接（ASTM/LIS2-A2 协议）- 第 2 批
- 质控管理（Levey-Jennings 图）- 第 3 批
- 微生物检验专项流程

---

## 二、架构设计

### 2.1 服务划分

扩展现有 `his-clinical` 服务，新增检验科工作台模块（不新增独立服务，因与医嘱紧密关联）。

```
his-clinical (端口 8083)
  ├── controller/
  │   ├── ExamRequestController      # 已有：检验申请
  │   ├── ExamSampleController       # 新增：样本管理
  │   ├── ExamResultController       # 已有：结果回写（增强）
  │   └── ExamReportController       # 新增：报告管理
  ├── service/
  │   ├── ExamRequestService
  │   ├── ExamSampleService          # 新增
  │   ├── ExamResultService
  │   └── ExamReportService          # 新增
  └── repository/
      ├── ExamSampleRepository       # 新增
      └── ExamReportRepository       # 新增
```

### 2.2 前端新增页面

```
/lab                          # 检验科工作台
/lab/receive                  # 样本接收
/lab/processing               # 检验执行
/lab/review                   # 结果审核
/lab/report                   # 报告管理
```

### 2.3 调用链路

```
临床医生 → POST /api/clinical/exam-requests (开立申请)
  ↓
检验科工作台 → GET /api/clinical/exam-samples?status=PENDING
  ↓
样本接收 → POST /api/clinical/exam-samples/{id}/receive (扫码/手工)
  ↓
检验执行 → POST /api/clinical/exam-results (录入结果)
  ↓
结果审核 → POST /api/clinical/exam-results/{id}/review (双人审核)
  ↓
报告生成 → POST /api/clinical/exam-reports/{requestId}/generate
  ↓
临床医生/患者 → GET /api/clinical/exam-reports/{id} (查看报告)
```

---

## 三、数据库设计

### 3.1 新增表（schema: clinical）

```sql
-- 检验样本表
CREATE TABLE clinical.cli_exam_sample (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES clinical.cli_exam_request(id),
    sample_barcode VARCHAR(50) UNIQUE NOT NULL,  -- 样本条码
    sample_type VARCHAR(50) NOT NULL,             -- BLOOD, URINE, STOOL, etc.
    container_type VARCHAR(30),                   -- 容器类型（真空管、尿杯等）
    collection_time TIMESTAMP,                    -- 采样时间
    collected_by BIGINT,                          -- 采样人
    received_time TIMESTAMP,                      -- 接收时间
    received_by BIGINT,                           -- 接收人
    sample_status VARCHAR(20) NOT NULL,           -- PENDING, COLLECTED, RECEIVED, IN_PROGRESS, COMPLETED, REJECTED
    rejection_reason TEXT,                        -- 拒收原因
    storage_location VARCHAR(50),                 -- 存放位置（冰箱、货架）
    notes TEXT,
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 检验结果增强表（已有 cli_exam_result，新增字段）
ALTER TABLE clinical.cli_exam_result ADD COLUMN IF NOT EXISTS result_status VARCHAR(20) DEFAULT 'PRELIMINARY';
ALTER TABLE clinical.cli_exam_result ADD COLUMN IF NOT EXISTS reviewed_by BIGINT;
ALTER TABLE clinical.cli_exam_result ADD COLUMN IF NOT EXISTS reviewed_time TIMESTAMP;
ALTER TABLE clinical.cli_exam_result ADD COLUMN IF NOT EXISTS reviewer2_by BIGINT;  -- 第二审核人
ALTER TABLE clinical.cli_exam_result ADD COLUMN IF NOT EXISTS reviewer2_time TIMESTAMP;
ALTER TABLE clinical.cli_exam_result ADD COLUMN IF NOT EXISTS report_generated BOOLEAN DEFAULT FALSE;
ALTER TABLE clinical.cli_exam_result ADD COLUMN IF NOT EXISTS instrument_code VARCHAR(50);  -- 仪器编号（预留）

-- 检验报告表
CREATE TABLE clinical.cli_exam_report (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES clinical.cli_exam_request(id),
    report_no VARCHAR(50) UNIQUE NOT NULL,          -- 报告编号
    report_time TIMESTAMP NOT NULL,                 -- 报告时间
    
    -- 报告内容
    result_summary TEXT,                            -- 结果摘要
    conclusion TEXT,                                -- 结论/诊断建议
    critical_value BOOLEAN DEFAULT FALSE,           -- 是否危急值
    critical_value_confirmed BOOLEAN,               -- 危急值是否已确认
    critical_value_confirmed_by BIGINT,
    critical_value_confirmed_time TIMESTAMP,
    
    -- 电子签名
    reporter_id BIGINT NOT NULL,                    -- 报告人
    reviewer_id BIGINT NOT NULL,                    -- 审核人
    reviewer2_id BIGINT,                            -- 第二审核人（必要时）
    
    -- 打印记录
    printed BOOLEAN DEFAULT FALSE,
    printed_time TIMESTAMP,
    printed_by BIGINT,
    print_count INT DEFAULT 0,
    
    status VARCHAR(20) DEFAULT 'DRAFT',             -- DRAFT, FINAL, AMENDED, CANCELLED
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 检验项目参考表（如已有可复用）
CREATE TABLE IF NOT EXISTS clinical.cli_exam_item (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(30) UNIQUE NOT NULL,          -- 项目编码
    item_name VARCHAR(100) NOT NULL,                -- 项目名称
    item_category VARCHAR(50),                      -- 分类（生化、血常规、尿常规等）
    sample_type VARCHAR(50),                        -- 样本类型
    container_type VARCHAR(30),                     -- 容器类型
    reference_range VARCHAR(100),                   -- 参考范围
    unit VARCHAR(30),                               -- 单位
    price DECIMAL(10,2),                            -- 价格
    tat_minutes INT,                                -- 周转时间（分钟）
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_sample_request ON clinical.cli_exam_sample(request_id);
CREATE INDEX idx_sample_barcode ON clinical.cli_exam_sample(sample_barcode);
CREATE INDEX idx_sample_status ON clinical.cli_exam_sample(sample_status);
CREATE INDEX idx_result_status ON clinical.cli_exam_result(result_status);
CREATE INDEX idx_report_request ON clinical.cli_exam_report(request_id);
CREATE INDEX idx_report_no ON clinical.cli_exam_report(report_no);
```

### 3.2 状态机

```
检验申请状态：
APPLIED → SAMPLE_COLLECTED → SAMPLE_RECEIVED → IN_PROGRESS → RESULT_REPORTED → REPORT_REVIEWED → FINAL

样本状态：
PENDING → COLLECTED → RECEIVED → IN_PROGRESS → COMPLETED / REJECTED

结果状态：
PRELIMINARY → REVIEWED → FINAL / AMENDED

报告状态：
DRAFT → FINAL / AMENDED / CANCELLED
```

---

## 四、API 设计

### 4.1 样本管理

```http
POST /api/clinical/exam-requests/{requestId}/samples
Content-Type: application/json

{
  "sampleType": "BLOOD",
  "containerType": "EDTA_VACUUM",
  "quantity": 2
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "sampleIds": [1001, 1002],
    "barcodes": ["LAB20260918001", "LAB20260918002"],
    "printLabel": true
  }
}
```

```http
GET /api/clinical/exam-samples?status=PENDING&date=2026-09-18
```

```http
POST /api/clinical/exam-samples/{sampleId}/receive
Content-Type: application/json

{
  "receivedBy": 301,
  "receivedTime": "2026-09-18T10:30:00",
  "storageLocation": "冰箱A-2层",
  "notes": "样本质量良好"
}
```

```http
POST /api/clinical/exam-samples/{sampleId}/reject
Content-Type: application/json

{
  "rejectionReason": "样本溶血",
  "rejectedBy": 301
}
```

### 4.2 条码生成与打印

```http
POST /api/clinical/exam-samples/barcode/generate
Content-Type: application/json

{
  "requestIds": [501, 502, 503]
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "barcodes": [
      {"requestId": 501, "sampleId": 1001, "barcode": "LAB20260918001", "patientName": "张三", "examName": "血常规"},
      {"requestId": 502, "sampleId": 1002, "barcode": "LAB20260918002", "patientName": "李四", "examName": "肝功能"}
    ]
  }
}
```

```http
GET /api/clinical/exam-samples/{sampleId}/label
Accept: application/pdf

# 返回条码标签 PDF（患者姓名、条码、检验项目、采样时间）
```

### 4.3 检验结果录入

```http
POST /api/clinical/exam-results
Content-Type: application/json

{
  "requestId": 501,
  "results": [
    {"itemCode": "WBC", "resultValue": "8.5", "unit": "10^9/L", "referenceRange": "4.0-10.0", "abnormal": false},
    {"itemCode": "RBC", "resultValue": "4.2", "unit": "10^12/L", "referenceRange": "3.5-5.5", "abnormal": false},
    {"itemCode": "HGB", "resultValue": "125", "unit": "g/L", "referenceRange": "110-160", "abnormal": false}
  ],
  "instrumentCode": "SYSMEX-XN1000",
  "technicianId": 401
}
```

### 4.4 结果审核

```http
POST /api/clinical/exam-results/{resultId}/review
Content-Type: application/json

{
  "reviewerId": 402,
  "reviewer2Id": 403,  // 必要时双人审核
  "conclusion": "结果正常",
  "criticalValue": false
}
```

```http
POST /api/clinical/exam-results/{resultId}/confirm-critical
Content-Type: application/json

{
  "confirmedBy": 201,  // 临床医生
  "confirmedTime": "2026-09-18T11:00:00",
  "action": "已通知主管医生，调整用药方案"
}
```

### 4.5 报告管理

```http
POST /api/clinical/exam-reports/{requestId}/generate
Content-Type: application/json

{
  "reporterId": 401,
  "reviewerId": 402,
  "conclusion": "血常规各项指标正常"
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "reportId": 801,
    "reportNo": "RPT20260918001",
    "reportTime": "2026-09-18T11:30:00",
    "status": "FINAL"
  }
}
```

```http
GET /api/clinical/exam-reports/{reportId}
```

```http
GET /api/clinical/exam-reports/{reportId}/pdf
Accept: application/pdf

# 返回标准格式检验报告 PDF
```

### 4.6 检验科工作台

```http
GET /api/clinical/lab/dashboard
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "pendingReceive": 15,       // 待接收样本数
    "inProgress": 8,            // 检验中样本数
    "pendingReview": 12,        // 待审核结果数
    "completedToday": 45,       // 今日完成数
    "criticalValues": [         // 危急值列表
      {
        "requestId": 505,
        "patientName": "王五",
        "itemCode": "K",
        "resultValue": "6.5",
        "unit": "mmol/L",
        "reportedTime": "2026-09-18T10:45:00"
      }
    ],
    "tatStats": {               // 周转时间统计
      "averageMinutes": 85,
      "withinTarget": 92.5      // 达标率 %
    }
  }
}
```

---

## 五、前端设计

### 5.1 页面结构

```
LabDashboard (检验科工作台)
  ├── 统计卡片（待接收、检验中、待审核、今日完成）
  ├── 危急值提醒列表（红色高亮）
  ├── TAT 统计图表
  └── 快捷入口

SampleReceivePage (样本接收)
  ├── 扫码输入框（支持扫码枪）
  ├── 待接收样本列表
  ├── 接收确认弹窗
  └── 拒收原因录入

ExamProcessingPage (检验执行)
  ├── 待检验样本列表
  ├── 结果录入表单（按检验项目动态渲染）
  ├── 异常结果标记
  └── 批量录入支持

ResultReviewPage (结果审核)
  ├── 待审核结果列表
  ├── 结果详情（含历史结果对比）
  ├── 审核确认弹窗
  └── 危急值确认流程

ReportManagementPage (报告管理)
  ├── 报告列表（按日期、科室筛选）
  ├── 报告预览
  ├── 报告打印
  └── 报告补打记录
```

### 5.2 关键组件

```typescript
// BarcodeScanner.tsx - 条码扫描组件
interface BarcodeScannerProps {
  onScan: (barcode: string) => void;
  autoFocus?: boolean;
}

// ResultEntryForm.tsx - 结果录入表单
interface ResultEntryFormProps {
  examItems: ExamItem[];
  onSubmit: (results: ExamResult[]) => void;
}

// CriticalValueAlert.tsx - 危急值提醒
interface CriticalValueAlertProps {
  criticalValues: CriticalValue[];
  onConfirm: (requestId: number) => void;
}

// ReportPreview.tsx - 报告预览
interface ReportPreviewProps {
  report: ExamReport;
  onPrint: () => void;
}
```

### 5.3 交互流程

**样本接收**：
```
检验人员打开样本接收页面
  ↓
扫码枪扫描样本条码（或手工输入）
  ↓
系统自动查询样本信息
  ↓
显示患者姓名、检验项目、采样时间
  ↓
确认接收 → 更新样本状态为 RECEIVED
  ↓
打印接收标签（可选）
```

**结果录入**：
```
检验人员选择待检验样本
  ↓
系统显示检验项目列表
  ↓
逐项录入结果（或从仪器导入）
  ↓
异常结果自动标红
  ↓
保存 → 结果状态变为 PRELIMINARY
```

**危急值处理**：
```
检验结果超出危急值阈值
  ↓
系统弹窗提醒检验人员
  ↓
检验人员复核结果
  ↓
确认危急值 → 自动通知临床医生（WebSocket/短信）
  ↓
临床医生确认接收 → 记录确认时间和处理措施
```

---

## 六、条码规则

### 6.1 条码格式

```
LAB + 日期(8位) + 序号(4位)
示例：LAB20260918001
```

### 6.2 条码生成

- 申请开立时预生成条码（可选）
- 样本接收时正式分配条码
- 支持补打标签

### 6.3 标签内容

- 患者姓名、性别、年龄
- 住院号/门诊号
- 条码（Code 128 或 QR Code）
- 检验项目名称
- 采样时间
- 容器类型

---

## 七、权限与审计

### 7.1 权限点

```
lab:sample:receive       # 接收样本
lab:sample:reject        # 拒收样本
lab:result:entry         # 录入结果
lab:result:review        # 审核结果
lab:report:generate      # 生成报告
lab:report:print         # 打印报告
lab:critical:confirm     # 确认危急值
```

### 7.2 角色分配

- **检验技师**：样本接收、结果录入
- **检验医师**：结果审核、报告签发
- **临床医生**：查看报告、确认危急值
- **护士**：查看报告、采样

### 7.3 审计

- 所有结果修改记录操作人和时间
- 危急值确认记录临床医生和处理措施
- 报告打印记录打印人和次数

---

## 八、测试策略

### 8.1 后端测试

```java
@Test
void generatesUniqueBarcodeForSample() {
    // 执行：generateBarcode(requestId)
    // 断言：条码格式正确，唯一性约束
}

@Test
void preventsResultEntryWithoutSampleReceived() {
    // 准备：样本状态为 PENDING
    // 执行：录入结果
    // 断言：抛出 BusinessException
}

@Test
void requiresDoubleReviewForCriticalValue() {
    // 准备：结果超出危急值阈值
    // 执行：单人审核
    // 断言：需要第二审核人
}
```

### 8.2 前端测试

```typescript
test('scans barcode and displays sample info', async () => {
  // 输入条码
  // 断言显示患者信息和检验项目
});

test('highlights abnormal results in red', async () => {
  // 录入超出参考范围的结果
  // 断言结果标红显示
});

test('shows critical value alert modal', async () => {
  // mock 危急值结果
  // 断言弹窗显示，要求确认
});
```

### 8.3 验收场景

1. 开立检验申请 → 生成条码 → 打印标签
2. 扫码接收样本 → 更新状态
3. 录入检验结果 → 异常标红
4. 审核结果 → 生成报告
5. 危急值处理 → 通知临床 → 医生确认
6. 报告打印 → 记录打印次数

---

## 九、实施计划

### 阶段 1（2 周）：样本管理

- 创建数据库表和迁移脚本
- 实现样本管理 API
- 前端样本接收页面
- 条码生成和打印

### 阶段 2（2 周）：结果录入与审核

- 结果录入 API 增强
- 审核流程实现
- 前端结果录入和审核页面
- 危急值提醒

### 阶段 3（1 周）：报告管理

- 报告生成 API
- 报告 PDF 模板
- 前端报告管理页面

### 阶段 4（1 周）：测试与优化

- 单元测试、集成测试
- 浏览器验收
- 性能优化（批量录入）

---

## 十、风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 条码打印机兼容性问题 | 中 | 支持多种打印机，提供 PDF 下载备选 |
| 结果录入效率低 | 中 | 支持批量录入、模板、仪器对接（后续） |
| 危急值通知不及时 | 高 | 多渠道通知（WebSocket + 短信），超时升级 |
| 报告格式不符合规范 | 高 | 参考卫健委检验报告规范，邀请检验科评审 |

---

## 十一、验收标准

- [ ] 检验申请自动生成条码
- [ ] 扫码接收样本，更新状态
- [ ] 检验结果录入，异常标红
- [ ] 双人审核流程
- [ ] 危急值自动提醒和确认
- [ ] 检验报告生成和打印
- [ ] 检验科工作台统计看板
- [ ] 权限控制：不同角色看到授权内容
- [ ] 前端测试覆盖核心场景
- [ ] 浏览器验收：至少 3 个真实检验流程
