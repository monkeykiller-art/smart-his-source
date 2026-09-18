# PACS/RIS 医学影像系统对接技术规格

> 版本：1.0-draft  
> 状态：待评审  
> 编制日期：2026-09-18  
> 依赖：M5 检查模块（ExamRequestController 已完成申请和结果回写）

---

## 一、概述

### 1.1 目标

对接医学影像系统，支持影像查看、检查报告结构化、历史影像对比和报告打印。

### 1.2 范围

- DICOM 影像查看（集成 OHIF Viewer）
- 检查报告结构化（影像所见、诊断意见、危急值）
- 检查申请-报告闭环
- 历史影像对比
- 报告打印和电子签名

### 1.3 不在范围

- 自建 PACS 服务器（对接已有 PACS）
- 影像后处理（三维重建、测量工具）
- 影像 AI 辅助诊断

---

## 二、架构设计

### 2.1 服务划分

扩展现有 `his-clinical` 服务，新增影像模块。

```
his-clinical (端口 8083)
  ├── controller/
  │   ├── ExamRequestController      # 已有：检查申请
  │   ├── ExamReportController       # 已有：报告管理（增强）
  │   ├── ImagingController          # 新增：影像查看
  │   └── PacsIntegrationController  # 新增：PACS 对接
  ├── service/
  │   ├── ExamRequestService
  │   ├── ExamReportService
  │   ├── ImagingService             # 新增
  │   └── PacsIntegrationService     # 新增
  └── repository/
      ├── ImagingRepository          # 新增
      └── PacsStudyRepository        # 新增
```

### 2.2 外部系统集成

```
his-clinical
  ↓ DICOMweb (WADO-RS, QIDO-RS, STOW-RS)
PACS 服务器 (Orthanc/dcm4chee/商用 PACS)
  ↓
OHIF Viewer (前端影像浏览器)
```

### 2.3 前端新增页面

```
/imaging                      # 影像工作台
/imaging/:studyId/viewer      # 影像查看器（OHIF 嵌入）
/imaging/report               # 报告管理
/imaging/history              # 历史影像对比
```

### 2.4 调用链路

```
临床医生 → POST /api/clinical/exam-requests (开立检查申请)
  ↓
影像科工作台 → GET /api/clinical/imaging/studies?status=PENDING
  ↓
登记 → POST /api/clinical/imaging/studies/{id}/register
  ↓
检查 → 技师执行检查，影像上传至 PACS
  ↓
报告 → 医生撰写报告，审核签发
  ↓
临床医生 → GET /api/clinical/imaging/studies/{id}/viewer (查看影像)
```

---

## 三、数据库设计

### 3.1 新增表（schema: clinical）

```sql
-- 影像检查表
CREATE TABLE clinical.cli_imaging_study (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES clinical.cli_exam_request(id),
    study_instance_uid VARCHAR(64) UNIQUE,  -- DICOM StudyInstanceUID
    study_date DATE,                         -- 检查日期
    study_time TIME,                         -- 检查时间
    modality VARCHAR(10) NOT NULL,           -- CT, MR, DR, US, etc.
    body_part VARCHAR(50),                   -- 检查部位
    study_description TEXT,                  -- 检查描述
    
    -- 患者信息（冗余，便于查询）
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(100),
    patient_birth_date DATE,
    patient_gender VARCHAR(10),
    
    -- 检查信息
    referring_physician VARCHAR(100),        -- 申请医生
    performing_physician VARCHAR(100),       -- 检查医生
    institution_name VARCHAR(200),           -- 医疗机构
    
    -- 状态
    study_status VARCHAR(20) NOT NULL,       -- SCHEDULED, IN_PROGRESS, COMPLETED, REPORTED, FINAL
    priority VARCHAR(10) DEFAULT 'ROUTINE',  -- ROUTINE, URGENT, STAT
    
    -- PACS 信息
    pacs_server_url VARCHAR(200),            -- PACS 服务器地址
    pacs_ae_title VARCHAR(50),               -- PACS AE Title
    
    -- 报告信息
    report_id BIGINT REFERENCES clinical.cli_exam_report(id),
    report_status VARCHAR(20),               -- DRAFT, FINAL, AMENDED
    
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 影像序列/图像表
CREATE TABLE clinical.cli_imaging_series (
    id BIGSERIAL PRIMARY KEY,
    study_id BIGINT NOT NULL REFERENCES clinical.cli_imaging_study(id),
    series_instance_uid VARCHAR(64) UNIQUE,  -- DICOM SeriesInstanceUID
    series_number INT,                       -- 序列号
    series_description TEXT,                 -- 序列描述
    modality VARCHAR(10),                    -- 序列模态
    body_part VARCHAR(50),                   -- 序列部位
    
    -- 图像信息
    instance_count INT,                      -- 图像数量
    series_date DATE,
    series_time TIME,
    
    -- PACS 信息
    sop_class_uid VARCHAR(64),               -- DICOM SOPClassUID
    
    created_time TIMESTAMP DEFAULT NOW()
);

-- 影像报告增强表（已有 cli_exam_report，新增字段）
ALTER TABLE clinical.cli_exam_report ADD COLUMN IF NOT EXISTS imaging_findings TEXT;  -- 影像所见
ALTER TABLE clinical.cli_exam_report ADD COLUMN IF NOT EXISTS imaging_diagnosis TEXT; -- 影像诊断
ALTER TABLE clinical.cli_exam_report ADD COLUMN IF NOT EXISTS recommendations TEXT;   -- 建议
ALTER TABLE clinical.cli_exam_report ADD COLUMN IF NOT EXISTS critical_finding BOOLEAN DEFAULT FALSE;  -- 危急发现
ALTER TABLE clinical.cli_exam_report ADD COLUMN IF NOT EXISTS critical_finding_confirmed BOOLEAN;
ALTER TABLE clinical.cli_exam_report ADD COLUMN IF NOT EXISTS critical_finding_confirmed_by BIGINT;
ALTER TABLE clinical.cli_exam_report ADD COLUMN IF NOT EXISTS critical_finding_confirmed_time TIMESTAMP;

-- 历史影像对比表
CREATE TABLE clinical.cli_imaging_comparison (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    current_study_id BIGINT NOT NULL REFERENCES clinical.cli_imaging_study(id),
    previous_study_id BIGINT NOT NULL REFERENCES clinical.cli_imaging_study(id),
    comparison_notes TEXT,                     -- 对比说明
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_study_patient ON clinical.cli_imaging_study(patient_id);
CREATE INDEX idx_study_date ON clinical.cli_imaging_study(study_date);
CREATE INDEX idx_study_modality ON clinical.cli_imaging_study(modality);
CREATE INDEX idx_study_status ON clinical.cli_imaging_study(study_status);
CREATE INDEX idx_series_study ON clinical.cli_imaging_series(study_id);
CREATE INDEX idx_comparison_patient ON clinical.cli_imaging_comparison(patient_id);
```

### 3.2 状态机

```
检查申请状态：
APPLIED → SCHEDULED → REGISTERED → IN_PROGRESS → COMPLETED → REPORTED → FINAL

影像状态：
SCHEDULED → IN_PROGRESS → COMPLETED

报告状态：
DRAFT → FINAL / AMENDED / CANCELLED
```

---

## 四、API 设计

### 4.1 影像检查管理

```http
POST /api/clinical/imaging/studies
Content-Type: application/json

{
  "requestId": 501,
  "modality": "CT",
  "bodyPart": "胸部",
  "studyDescription": "胸部 CT 平扫",
  "priority": "ROUTINE",
  "scheduledDate": "2026-09-20"
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "id": 601,
    "studyInstanceUID": "1.2.840.113619.2.55.3.1234567890.123",
    "status": "SCHEDULED"
  }
}
```

```http
GET /api/clinical/imaging/studies?patientId=2099000000000000001&modality=CT&startDate=2026-01-01&endDate=2026-09-18
```

```http
POST /api/clinical/imaging/studies/{studyId}/register
Content-Type: application/json

{
  "performingPhysician": "李医生",
  "institutionName": "XX 医院影像科"
}
```

### 4.2 影像查看

```http
GET /api/clinical/imaging/studies/{studyId}/viewer
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "studyId": 601,
    "studyInstanceUID": "1.2.840.113619.2.55.3.1234567890.123",
    "viewerUrl": "https://viewer.example.com/study/1.2.840.113619.2.55.3.1234567890.123",
    "pacsServerUrl": "https://pacs.example.com/dicomweb",
    "series": [
      {
        "seriesInstanceUID": "1.2.840.113619.2.55.3.1234567890.123.1",
        "seriesDescription": "胸部 CT 平扫",
        "instanceCount": 120
      }
    ]
  }
}
```

### 4.3 影像报告

```http
POST /api/clinical/imaging/reports
Content-Type: application/json

{
  "studyId": 601,
  "imagingFindings": "双肺纹理增多，未见明显实质性病变。纵隔内未见肿大淋巴结。",
  "imagingDiagnosis": "胸部 CT 平扫未见明显异常。",
  "recommendations": "建议定期复查。",
  "criticalFinding": false
}
```

```http
POST /api/clinical/imaging/reports/{reportId}/review
Content-Type: application/json

{
  "reviewerId": 402,
  "reviewer2Id": 403,
  "conclusion": "报告审核通过"
}
```

### 4.4 历史影像对比

```http
POST /api/clinical/imaging/comparisons
Content-Type: application/json

{
  "patientId": 2099000000000000001,
  "currentStudyId": 601,
  "previousStudyId": 580,
  "comparisonNotes": "与 2026-06-15 胸部 CT 对比，病灶无明显变化。"
}
```

```http
GET /api/clinical/imaging/comparisons?patientId=2099000000000000001
```

### 4.5 PACS 集成

```http
GET /api/clinical/pacs/studies?patientId=2099000000000000001
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "studies": [
      {
        "studyInstanceUID": "1.2.840.113619.2.55.3.1234567890.123",
        "studyDate": "2026-09-18",
        "modality": "CT",
        "description": "胸部 CT 平扫",
        "seriesCount": 1,
        "instanceCount": 120
      }
    ]
  }
}
```

---

## 五、前端设计

### 5.1 页面结构

```
ImagingWorkbench (影像工作台)
  ├── 待检查列表
  ├── 待报告列表
  ├── 危急值提醒
  └── 统计卡片

ImageViewerPage (影像查看器)
  ├── OHIF Viewer 嵌入
  ├── 患者信息侧边栏
  ├── 序列列表
  ── 报告撰写面板

ReportManagementPage (报告管理)
  ├── 报告列表（按日期、模态筛选）
  ├── 报告编辑（结构化模板）
  ├── 审核流程
  └── 打印/导出

HistoryComparisonPage (历史对比)
  ├── 患者历史检查列表
  ├── 双联/四联对比视图
  └── 对比说明录入
```

### 5.2 关键组件

```typescript
// OhifViewer.tsx - OHIF 影像查看器
interface OhifViewerProps {
  studyInstanceUID: string;
  pacsServerUrl: string;
  onReport?: () => void;
}

// ReportEditor.tsx - 报告编辑器
interface ReportEditorProps {
  studyId: number;
  template?: ReportTemplate;
  onSubmit: (report: ImagingReport) => void;
}

// HistoryComparison.tsx - 历史对比
interface HistoryComparisonProps {
  patientId: number;
  studies: ImagingStudy[];
  onCompare: (currentId: number, previousId: number) => void;
}
```

### 5.3 OHIF Viewer 集成

```typescript
// 使用 iframe 嵌入 OHIF Viewer
<iframe
  src={`${OHIF_VIEWER_URL}?studyUID=${studyInstanceUID}&dicomWebUrl=${pacsServerUrl}`}
  width="100%"
  height="800px"
  frameBorder="0"
/>

// 或使用 OHIF 作为 npm 包集成
import { OHIFViewer } from '@ohif/viewer';
```

### 5.4 交互流程

**影像查看**：
```
医生点击"查看影像"
  ↓
系统查询 PACS 获取 StudyInstanceUID
  ↓
打开影像查看器页面
  ↓
OHIF Viewer 加载 DICOM 影像
  ↓
医生浏览影像（窗宽窗位、缩放、测量）
  ↓
点击"撰写报告" → 跳转报告编辑
```

**报告撰写**：
```
医生选择检查
  ↓
系统加载报告模板（按模态/部位）
  ↓
医生填写影像所见、诊断、建议
  ↓
保存草稿 / 提交审核
  ↓
审核医生审核 → 签发报告
  ↓
报告推送至临床医生
```

---

## 六、PACS 对接方案

### 6.1 DICOMweb 标准

使用 DICOMweb RESTful API：

- **WADO-RS** (Web Access to DICOM Objects): 获取影像
- **QIDO-RS** (Query based on ID for DICOM Objects): 查询影像
- **STOW-RS** (Store of DICOM Objects): 上传影像

### 6.2 配置

```yaml
# application.yml
pacs:
  server:
    url: https://pacs.example.com/dicomweb
    ae-title: PACS_SERVER
    username: ${PACS_USERNAME}
    password: ${PACS_PASSWORD}
  viewer:
    url: https://viewer.example.com
  modalities:
    - CT
    - MR
    - DR
    - US
    - CR
```

### 6.3 认证

- PACS 服务器使用 Basic Auth 或 OAuth2
- 敏感信息存储在环境变量或密钥管理系统

---

## 七、权限与审计

### 7.1 权限点

```
imaging:study:view       # 查看影像
imaging:study:register   # 登记检查
imaging:report:create    # 创建报告
imaging:report:review    # 审核报告
imaging:report:print     # 打印报告
imaging:comparison:view  # 查看历史对比
```

### 7.2 角色分配

- **影像技师**：登记检查、执行检查
- **影像医生**：撰写报告、审核报告
- **临床医生**：查看影像、查看报告
- **护士**：查看报告

### 7.3 审计

- 所有影像查看记录操作人和时间
- 报告修改记录修改人和时间
- 报告打印记录打印人和次数

---

## 八、测试策略

### 8.1 后端测试

```java
@Test
void createsImagingStudyWithStudyInstanceUID() {
    // 执行：createStudy(request)
    // 断言：生成唯一 StudyInstanceUID
}

@Test
void retrievesStudiesByPatientId() {
    // 准备：患者有多个检查
    // 执行：getStudies(patientId)
    // 断言：返回所有检查，按日期排序
}

@Test
void preventsReportWithoutCompletedStudy() {
    // 准备：检查状态为 IN_PROGRESS
    // 执行：创建报告
    // 断言：抛出 BusinessException
}
```

### 8.2 前端测试

```typescript
test('opens OHIF viewer with correct study UID', async () => {
  // 点击查看影像
  // 断言 iframe src 包含正确的 studyInstanceUID
});

test('loads report template by modality', async () => {
  // 选择 CT 检查
  // 断言报告编辑器加载 CT 模板
});

test('displays critical finding alert', async () => {
  // 勾选危急发现
  // 断言显示危急值确认流程
});
```

### 8.3 验收场景

1. 开立 CT 检查申请 → 影像科登记 → 执行检查
2. 影像上传至 PACS → 临床医生查看影像
3. 影像医生撰写报告 → 审核签发
4. 报告推送至临床医生
5. 历史影像对比（同一患者两次 CT）
6. 危急值处理（发现肺结节）

---

## 九、实施计划

### 阶段 1（2 周）：基础框架

- 创建数据库表和迁移脚本
- 实现影像检查管理 API
- 前端影像工作台

### 阶段 2（2 周）：PACS 对接

- PACS 服务器配置
- DICOMweb 接口对接
- OHIF Viewer 集成

### 阶段 3（2 周）：报告管理

- 报告结构化 API
- 报告模板管理
- 前端报告编辑器

### 阶段 4（1 周）：测试与优化

- 单元测试、集成测试
- 浏览器验收
- 性能优化（影像加载速度）

---

## 十、风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| PACS 服务器不稳定 | 高 | 多 PACS 服务器负载均衡，本地缓存常用影像 |
| 影像加载慢 | 中 | 预加载、缩略图、渐进式加载 |
| DICOM 标准兼容性 | 中 | 使用标准 DICOMweb API，避免厂商特定功能 |
| 报告格式不符合规范 | 高 | 参考卫健委影像报告规范，邀请影像科评审 |

---

## 十一、验收标准

- [ ] 检查申请自动生成 StudyInstanceUID
- [ ] OHIF Viewer 正确加载 DICOM 影像
- [ ] 影像报告结构化录入
- [ ] 双人审核流程
- [ ] 危急值自动提醒和确认
- [ ] 历史影像对比功能
- [ ] 报告打印和导出
- [ ] 权限控制：不同角色看到授权内容
- [ ] 前端测试覆盖核心场景
- [ ] 浏览器验收：至少 3 个真实影像流程
