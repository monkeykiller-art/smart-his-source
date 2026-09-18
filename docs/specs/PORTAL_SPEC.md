# 患者自助门户技术规格

> 版本：1.0-draft  
> 状态：待评审  
> 编制日期：2026-09-18  
> 依赖：现有患者服务（his-patient）、临床服务（his-clinical）、运营服务（his-operations）

---

## 一、概述

### 1.1 目标

为患者提供自助服务门户，支持查看检验检查报告、费用明细、预约管理和健康档案，减少窗口排队和电话咨询。

### 1.2 范围

- 检验报告查看（PDF 下载、历史对比）
- 检查报告查看（含影像链接）
- 费用查询（门诊账单、住院日清单、出院结算）
- 预约管理（在线预约、取消、改期）
- 健康档案（诊断记录、过敏史、用药记录）
- 满意度评价（就诊后评价）
- 消息通知（报告完成、缴费提醒）

### 1.3 不在范围

- 在线问诊（第 3 批）
- 医保报销申请（依赖外部系统）
- 电子处方流转（院外购药）
- 家庭成员管理（第 2 期）

---

## 二、架构设计

### 2.1 服务划分

新增 `his-portal` 服务，独立部署，作为患者端 BFF（Backend for Frontend），聚合后端服务数据。

```
his-portal (端口 8088)
  ├── controller/
  │   ├── ReportController         # 报告查看
  │   ├── BillingController        # 费用查询
  │   ├── AppointmentController    # 预约管理
  │   ├── HealthRecordController   # 健康档案
  │   ├── EvaluationController     # 满意度评价
  │   └── NotificationController   # 消息通知
  ├── service/
  │   ├── ReportAggregationService
  │   ├── BillingQueryService
  │   ├── AppointmentService
  │   ├── HealthRecordService
  │   ├── EvaluationService
  │   └── NotificationService
  ├── client/                      # Feign 客户端
  │   ├── PatientClient
  │   ├── ClinicalClient
  │   ├── OperationsClient
  │   └── PharmaClient
  └── dto/
      ├── ReportResponse
      ├── BillingResponse
      ├── AppointmentRequest/Response
      └── HealthRecordResponse
```

### 2.2 前端架构

独立前端项目 `smart-his-portal`（React 18 + Ant Design Mobile），与现有管理端分离。

```
smart-his-portal (端口 3001)
  ├── pages/
  │   ├── LoginPage.tsx            # 登录（手机号 + 验证码 / 身份证 + 密码）
  │   ├── HomePage.tsx             # 首页（快捷入口、待办提醒）
  │   ├── ReportsPage.tsx          # 报告列表
  │   ├── ReportDetailPage.tsx     # 报告详情
  │   ├── BillingPage.tsx          # 费用查询
  │   ├── AppointmentPage.tsx      # 预约管理
  │   ├── HealthRecordPage.tsx     # 健康档案
  │   ├── EvaluationPage.tsx       # 满意度评价
  │   └── NotificationPage.tsx     # 消息中心
  ├── components/
  │   ├── ReportCard.tsx
  │   ├── BillingItem.tsx
  │   ├── AppointmentForm.tsx
  │   └── HealthTimeline.tsx
  └── hooks/
      ├── useAuth.ts
      └── useNotification.ts
```

### 2.3 调用链路

```
患者浏览器 / 移动端
  ↓ HTTPS
his-gateway (路由 /api/portal/** → his-portal)
  ↓ Feign 调用
his-patient (患者信息、实名认证)
his-clinical (检验检查报告、诊断记录)
his-operations (预约、住院费用)
his-pharma (用药记录)
  ↓
返回聚合数据给前端
```

### 2.4 认证与授权

- **患者身份验证**：手机号 + 短信验证码（首次登录自动注册）
- **会话管理**：JWT Token（有效期 7 天，支持刷新）
- **敏感操作**：二次验证（如查看完整报告需输入身份证后 4 位）
- **数据隔离**：患者只能查看自己的数据，通过 `patient_id` 严格过滤

---

## 三、数据库设计

### 3.1 新增表（schema: portal）

```sql
-- 患者门户账户表
CREATE TABLE portal.ptl_account (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE REFERENCES patient.pat_patient(id),
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255),              -- 可选，支持身份证 + 密码登录
    real_name VARCHAR(50) NOT NULL,
    id_card_no VARCHAR(18) NOT NULL,         -- 加密存储
    email VARCHAR(100),
    avatar_url VARCHAR(500),
    last_login_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',     -- ACTIVE, LOCKED, DISABLED
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 预约记录表
CREATE TABLE portal.ptl_appointment (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,             -- 排班 ID（来自 his-operations）
    doctor_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot VARCHAR(20) NOT NULL,          -- 如 "09:00-09:30"
    status VARCHAR(20) NOT NULL,             -- PENDING, CONFIRMED, CANCELLED, COMPLETED, NOSHOW
    cancel_reason TEXT,
    cancel_time TIMESTAMP,
    reminder_sent BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_appointment_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NOSHOW'))
);

-- 满意度评价表
CREATE TABLE portal.ptl_evaluation (
    id BIGSERIAL PRIMARY KEY,
    encounter_id BIGINT NOT NULL,            -- 就诊记录 ID
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT,
    dept_id BIGINT,
    overall_score INT NOT NULL,              -- 总体评分 1-5
    doctor_score INT,                        -- 医生评分 1-5
    service_score INT,                       -- 服务评分 1-5
    environment_score INT,                   -- 环境评分 1-5
    comment TEXT,                            -- 文字评价
    tags VARCHAR(200),                       -- 标签（如 "态度好,专业,耐心"）
    anonymous BOOLEAN DEFAULT FALSE,         -- 是否匿名
    reply_content TEXT,                      -- 医院回复
    reply_by BIGINT,
    reply_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SUBMITTED',  -- SUBMITTED, REPLIED
    created_time TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_score_range CHECK (overall_score BETWEEN 1 AND 5)
);

-- 消息通知表
CREATE TABLE portal.ptl_notification (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    notification_type VARCHAR(30) NOT NULL,  -- REPORT_READY, BILL_REMINDER, APPOINTMENT_REMINDER, SYSTEM
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    related_id BIGINT,                       -- 关联业务 ID（报告 ID、账单 ID 等）
    related_type VARCHAR(30),                -- REPORT, BILL, APPOINTMENT
    is_read BOOLEAN DEFAULT FALSE,
    read_time TIMESTAMP,
    channel VARCHAR(20) DEFAULT 'PORTAL',    -- PORTAL, SMS, WECHAT
    sent_time TIMESTAMP,
    created_time TIMESTAMP DEFAULT NOW()
);

-- 患者授权记录表（查看他人报告，如家属）
CREATE TABLE portal.ptl_authorization (
    id BIGSERIAL PRIMARY KEY,
    owner_patient_id BIGINT NOT NULL,        -- 数据所有者
    authorized_patient_id BIGINT NOT NULL,   -- 被授权人
    auth_type VARCHAR(30) NOT NULL,          -- FAMILY, GUARDIAN, LEGAL
    auth_scope VARCHAR(100) NOT NULL,        -- 授权范围（REPORT, BILL, ALL）
    auth_start DATE NOT NULL,
    auth_end DATE,                           -- NULL 表示长期
    status VARCHAR(20) DEFAULT 'ACTIVE',     -- ACTIVE, EXPIRED, REVOKED
    created_time TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_different_patients CHECK (owner_patient_id <> authorized_patient_id)
);

CREATE INDEX idx_account_patient ON portal.ptl_account(patient_id);
CREATE INDEX idx_account_phone ON portal.ptl_account(phone);
CREATE INDEX idx_appointment_patient ON portal.ptl_appointment(patient_id, appointment_date);
CREATE INDEX idx_appointment_status ON portal.ptl_appointment(status);
CREATE INDEX idx_evaluation_patient ON portal.ptl_evaluation(patient_id, created_time);
CREATE INDEX idx_notification_patient ON portal.ptl_notification(patient_id, is_read, created_time);
CREATE INDEX idx_authorization_owner ON portal.ptl_authorization(owner_patient_id);
CREATE INDEX idx_authorization_authorized ON portal.ptl_authorization(authorized_patient_id);
```

### 3.2 数据安全

- **身份证号**：使用 AES-256 加密存储，密钥通过环境变量注入
- **手机号**：脱敏显示（如 138****1234），完整号码仅在验证时显示
- **敏感操作日志**：记录所有查看报告、下载 PDF 的操作人和时间
- **会话超时**：30 分钟无操作自动登出

---

## 四、API 设计

### 4.1 认证

```http
POST /api/portal/auth/send-code
Content-Type: application/json

{
  "phone": "13800138000"
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "expireSeconds": 60
  }
}
```

```http
POST /api/portal/auth/login
Content-Type: application/json

{
  "phone": "13800138000",
  "verifyCode": "123456"
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "patientId": 2099000000000000001,
    "patientName": "张三",
    "isNewUser": false
  }
}
```

### 4.2 报告查看

```http
GET /api/portal/reports?reportType=LAB&startDate=2026-09-01&endDate=2026-09-18
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "reports": [
      {
        "reportId": 801,
        "reportType": "LAB",
        "reportNo": "RPT20260918001",
        "examName": "血常规",
        "examDate": "2026-09-18",
        "reportTime": "2026-09-18T11:30:00",
        "status": "FINAL",
        "criticalValue": false,
        "doctorName": "李医生",
        "deptName": "检验科"
      }
    ]
  }
}
```

```http
GET /api/portal/reports/{reportId}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "reportId": 801,
    "reportNo": "RPT20260918001",
    "patientName": "张三",
    "gender": "男",
    "age": 45,
    "examName": "血常规",
    "examDate": "2026-09-18",
    "reportTime": "2026-09-18T11:30:00",
    "results": [
      {
        "itemName": "白细胞计数",
        "resultValue": "8.5",
        "unit": "10^9/L",
        "referenceRange": "4.0-10.0",
        "abnormal": false
      },
      {
        "itemName": "红细胞计数",
        "resultValue": "4.2",
        "unit": "10^12/L",
        "referenceRange": "3.5-5.5",
        "abnormal": false
      }
    ],
    "conclusion": "血常规各项指标正常",
    "reporterName": "王技师",
    "reviewerName": "赵医师",
    "pdfUrl": "/api/portal/reports/801/pdf"
  }
}
```

```http
GET /api/portal/reports/{reportId}/pdf
Accept: application/pdf

# 返回报告 PDF
```

### 4.3 费用查询

```http
GET /api/portal/billing/outpatient?encounterId=5001
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "encounterId": 5001,
    "patientName": "张三",
    "visitDate": "2026-09-18",
    "deptName": "内科",
    "doctorName": "李医生",
    "items": [
      {
        "feeItemId": 101,
        "itemName": "挂号费",
        "quantity": 1,
        "unitPrice": 50.00,
        "totalAmount": 50.00,
        "paidTime": "2026-09-18T08:30:00"
      },
      {
        "feeItemId": 205,
        "itemName": "血常规",
        "quantity": 1,
        "unitPrice": 30.00,
        "totalAmount": 30.00,
        "paidTime": "2026-09-18T09:00:00"
      }
    ],
    "totalAmount": 80.00,
    "paidAmount": 80.00,
    "refundAmount": 0.00,
    "balance": 0.00
  }
}
```

```http
GET /api/portal/billing/inpatient/daily?admissionId=1001&date=2026-09-18
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "admissionId": 1001,
    "patientName": "张三",
    "wardName": "内科一病区",
    "bedNo": "101-A",
    "date": "2026-09-18",
    "items": [
      {
        "category": "床位费",
        "itemName": "普通床位",
        "quantity": 1,
        "unitPrice": 80.00,
        "totalAmount": 80.00
      },
      {
        "category": "药品费",
        "itemName": "阿莫西林胶囊",
        "quantity": 6,
        "unitPrice": 12.50,
        "totalAmount": 75.00
      }
    ],
    "totalAmount": 155.00,
    "prepaidBalance": 2000.00
  }
}
```

### 4.4 预约管理

```http
GET /api/portal/appointments/schedules?deptId=101&startDate=2026-09-20&endDate=2026-09-26
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "schedules": [
      {
        "scheduleId": 301,
        "doctorId": 201,
        "doctorName": "李医生",
        "doctorTitle": "主任医师",
        "deptId": 101,
        "deptName": "内科",
        "date": "2026-09-20",
        "timeSlots": [
          {"slot": "08:00-08:30", "status": "AVAILABLE", "remaining": 5},
          {"slot": "08:30-09:00", "status": "AVAILABLE", "remaining": 3},
          {"slot": "09:00-09:30", "status": "FULL"}
        ],
        "fee": 50.00
      }
    ]
  }
}
```

```http
POST /api/portal/appointments
Content-Type: application/json

{
  "scheduleId": 301,
  "date": "2026-09-20",
  "timeSlot": "08:30-09:00"
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "appointmentId": 601,
    "status": "PENDING",
    "qrCode": "data:image/png;base64,..."
  }
}
```

```http
POST /api/portal/appointments/{appointmentId}/cancel
Content-Type: application/json

{
  "reason": "临时有事"
}
```

### 4.5 健康档案

```http
GET /api/portal/health-records/summary
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "patientId": 2099000000000000001,
    "patientName": "张三",
    "gender": "男",
    "dateOfBirth": "1981-05-15",
    "allergies": [
      {"allergen": "青霉素", "severity": "SEVERE", "confirmedTime": "2025-03-10"}
    ],
    "chronicDiseases": [
      {"diseaseName": "高血压", "diagnosedDate": "2020-06-15", "status": "ACTIVE"}
    ],
    "recentEncounters": [
      {
        "encounterId": 5001,
        "visitDate": "2026-09-18",
        "deptName": "内科",
        "doctorName": "李医生",
        "diagnosis": "上呼吸道感染",
        "status": "COMPLETED"
      }
    ],
    "currentMedications": [
      {
        "drugName": "阿莫西林胶囊",
        "dosage": "0.5g",
        "frequency": "每日 3 次",
        "startDate": "2026-09-18",
        "endDate": "2026-09-25"
      }
    ]
  }
}
```

### 4.6 满意度评价

```http
POST /api/portal/evaluations
Content-Type: application/json

{
  "encounterId": 5001,
  "overallScore": 5,
  "doctorScore": 5,
  "serviceScore": 4,
  "environmentScore": 5,
  "comment": "医生很专业，护士态度好",
  "tags": ["专业", "态度好", "耐心"],
  "anonymous": false
}
```

### 4.7 消息通知

```http
GET /api/portal/notifications?unreadOnly=true&page=1&size=20
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "notifications": [
      {
        "id": 1001,
        "type": "REPORT_READY",
        "title": "您的检验报告已出",
        "content": "您 2026-09-18 的血常规报告已生成，点击查看",
        "relatedId": 801,
        "relatedType": "REPORT",
        "isRead": false,
        "sentTime": "2026-09-18T11:35:00"
      }
    ],
    "unreadCount": 3
  }
}
```

```http
POST /api/portal/notifications/{id}/read
```

---

## 五、前端设计

### 5.1 页面结构

```
LoginPage (登录)
  ├── 手机号 + 验证码登录
  ├── 身份证 + 密码登录（备选）
  └── 首次登录自动注册

HomePage (首页)
  ├── 快捷入口（报告查询、费用查询、预约、健康档案）
  ├── 待办提醒（未读消息、待缴费、待就诊预约）
  └── 健康摘要（最近就诊、过敏史）

ReportsPage (报告列表)
  ├── 报告类型筛选（检验、检查）
  ├── 日期范围筛选
  ├── 报告卡片列表
  └── 点击进入详情

ReportDetailPage (报告详情)
  ├── 患者基本信息
  ├── 检验/检查结果列表（异常项标红）
  ├── 结论
  ├── 报告人、审核人
  └── 下载 PDF 按钮

BillingPage (费用查询)
  ├── Tabs：门诊 / 住院
  ├── 门诊：按就诊记录查看费用明细
  ├── 住院：按日期查看每日费用清单
  └── 费用分类统计（药品、检查、治疗、床位）

AppointmentPage (预约管理)
  ├── 我的预约列表（待就诊、已完成、已取消）
  ├── 新建预约：选择科室 → 选择医生 → 选择日期 → 选择时段
  ├── 预约确认弹窗
  └── 取消预约

HealthRecordPage (健康档案)
  ├── 基本信息（姓名、性别、年龄、血型）
  ├── 过敏史
  ├── 慢性病记录
  ├── 就诊记录时间轴
  └── 当前用药

EvaluationPage (满意度评价)
  ├── 待评价就诊列表
  ├── 评分表单（星级评分）
  ├── 文字评价
  ├── 标签选择
  └── 匿名选项

NotificationPage (消息中心)
  ├── 消息列表（按时间倒序）
  ├── 未读消息标记
  └── 点击跳转到相关页面
```

### 5.2 关键组件

```typescript
// ReportCard.tsx - 报告卡片
interface ReportCardProps {
  report: ReportSummary;
  onClick: () => void;
}

// BillingItem.tsx - 费用明细项
interface BillingItemProps {
  item: BillingItem;
  showDate?: boolean;
}

// AppointmentForm.tsx - 预约表单
interface AppointmentFormProps {
  schedules: Schedule[];
  onSubmit: (scheduleId: number, date: string, timeSlot: string) => void;
}

// HealthTimeline.tsx - 健康时间轴
interface HealthTimelineProps {
  encounters: Encounter[];
}

// StarRating.tsx - 星级评分
interface StarRatingProps {
  value: number;
  onChange: (value: number) => void;
  max?: number;
}
```

### 5.3 交互流程

**报告查看**：
```
患者登录门户
  ↓
点击"报告查询"
  ↓
选择报告类型和日期范围
  ↓
显示报告列表（最新在前）
  ↓
点击报告卡片
  ↓
显示报告详情（异常项标红）
  ↓
点击下载 PDF（可选）
```

**在线预约**：
```
患者点击"预约挂号"
  ↓
选择科室
  ↓
选择医生（显示医生简介、出诊时间）
  ↓
选择日期（显示未来 7 天）
  ↓
选择时段（显示剩余号源）
  ↓
确认预约信息
  ↓
提交 → 生成预约二维码
  ↓
发送短信提醒（就诊前一天）
```

**满意度评价**：
```
就诊完成后，首页显示"待评价"提醒
  ↓
点击评价
  ↓
星级评分（总体、医生、服务、环境）
  ↓
文字评价（可选）
  ↓
选择标签（如"专业"、"耐心"）
  ↓
选择是否匿名
  ↓
提交 → 感谢评价
```

---

## 六、安全与隐私

### 6.1 身份验证

- **首次登录**：手机号 + 短信验证码，自动匹配患者档案（通过证件号或手机号）
- **匹配失败**：提示联系窗口办理实名认证
- **会话管理**：JWT Token，有效期 7 天，支持刷新
- **超时登出**：30 分钟无操作自动登出

### 6.2 数据隔离

- **严格患者隔离**：所有查询接口强制过滤 `patient_id = current_user.patient_id`
- **家属授权**：通过 `ptl_authorization` 表授权查看（需窗口办理）
- **敏感信息脱敏**：身份证号、手机号部分隐藏

### 6.3 审计日志

- **查看报告**：记录查看时间、IP 地址
- **下载 PDF**：记录下载人、时间、文件名
- **修改预约**：记录取消/改期原因
- **登录日志**：记录登录时间、IP、设备信息

### 6.4 合规要求

- **隐私政策**：首次登录需同意隐私政策
- **数据加密**：敏感字段（身份证、手机号）加密存储
- **GDPR/个人信息保护法**：支持患者申请删除数据（需窗口办理）

---

## 七、集成点

### 7.1 与 his-patient 集成

- 查询患者基本信息、证件号、联系方式
- 患者实名认证（匹配现有档案）

### 7.2 与 his-clinical 集成

- 查询检验报告（`cli_exam_report`）
- 查询检查报告（`cli_exam_report`，report_type = 'IMAGING'）
- 查询诊断记录（`cli_diagnosis`）
- 查询过敏史（`cli_allergy`）

### 7.3 与 his-operations 集成

- 查询排班（`ops_schedule`）
- 创建预约（新增 `ops_appointment` 或复用 `ptl_appointment`）
- 查询住院费用（`ops_daily_charge`）
- 查询预交金余额（`ops_prepayment`）

### 7.4 与 his-pharma 集成

- 查询用药记录（`pha_prescription`）

### 7.5 消息推送

- **短信**：阿里云短信 / 腾讯云短信（验证码、预约提醒）
- **微信**：微信公众号模板消息（报告完成、缴费提醒）
- **门户内**：`ptl_notification` 表，前端轮询或 WebSocket

---

## 八、测试策略

### 8.1 后端测试

```java
@Test
void patientCanOnlyViewOwnReports() {
    // 准备：患者 A 和患者 B 的报告
    // 执行：患者 A 调用 GET /api/portal/reports
    // 断言：只返回患者 A 的报告
}

@Test
void preventsAppointmentForPastDate() {
    // 准备：过去的日期
    // 执行：POST /api/portal/appointments
    // 断言：返回 400 Bad Request
}

@Test
void cancelsAppointmentBeforeVisitTime() {
    // 准备：明天的预约
    // 执行：POST /api/portal/appointments/{id}/cancel
    // 断言：状态变为 CANCELLED，释放号源
}

@Test
void requiresAuthenticationForSensitiveData() {
    // 准备：无 Token
    // 执行：GET /api/portal/reports
    // 断言：返回 401 Unauthorized
}
```

### 8.2 前端测试

```typescript
test('displays report list with correct filtering', async () => {
  // mock 报告数据
  // 选择检验报告、日期范围
  // 断言列表只显示符合条件的报告
});

test('shows appointment confirmation modal', async () => {
  // 选择科室、医生、日期、时段
  // 点击提交
  // 断言弹窗显示预约信息，要求确认
});

test('highlights abnormal results in red', async () => {
  // mock 报告详情
  // 断言异常项标红显示
});

test('requires login for protected pages', async () => {
  // 访问报告页面
  // 未登录
  // 断言跳转到登录页
});
```

### 8.3 验收场景

1. 患者登录 → 查看检验报告 → 下载 PDF
2. 在线预约 → 选择医生和时段 → 收到确认短信
3. 取消预约 → 号源释放 → 收到取消确认
4. 查看住院日清单 → 费用分类正确
5. 就诊后评价 → 提交成功 → 医院可见
6. 收到报告完成通知 → 点击查看

---

## 九、实施计划

### 阶段 1（3 周）：基础框架与认证

- 创建 `his-portal` 服务和 `smart-his-portal` 前端项目
- 实现患者认证（手机号 + 验证码）
- 数据库表创建和迁移
- 前端登录页、首页

### 阶段 2（3 周）：报告与费用查询

- 报告查询 API（聚合 his-clinical 数据）
- 费用查询 API（聚合 his-operations 数据）
- 前端报告列表、详情页、费用页
- PDF 下载功能

### 阶段 3（2 周）：预约与评价

- 预约管理 API
- 满意度评价 API
- 前端预约页、评价页
- 短信通知集成

### 阶段 4（2 周）：健康档案与优化

- 健康档案 API
- 消息通知系统
- 前端健康档案页、消息中心
- 性能优化（缓存、分页）

### 阶段 5（1 周）：测试与上线

- 单元测试、集成测试
- 浏览器验收
- 安全审计
- 上线部署

---

## 十、风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 患者身份匹配失败（手机号未登记） | 高 | 提供窗口实名认证流程，支持人工绑定 |
| 报告数据敏感，泄露风险 | 高 | 严格数据隔离、加密存储、审计日志、二次验证 |
| 短信验证码被刷 | 中 | 限制发送频率（60 秒一次）、每日上限（10 次）、IP 限流 |
| 预约号源超卖 | 中 | 数据库乐观锁、事务控制、实时校验剩余号源 |
| 移动端兼容性差 | 中 | 使用 Ant Design Mobile，覆盖主流机型测试 |
| 患者不会使用 | 中 | 提供操作指南视频、窗口指导、简化流程 |

---

## 十一、验收标准

- [ ] 患者通过手机号 + 验证码登录
- [ ] 查看检验检查报告，下载 PDF
- [ ] 查询门诊账单和住院日清单
- [ ] 在线预约、取消、改期
- [ ] 查看健康档案（诊断、过敏、用药）
- [ ] 提交满意度评价
- [ ] 接收消息通知（报告完成、预约提醒）
- [ ] 数据隔离：患者只能查看自己的数据
- [ ] 敏感操作有审计日志
- [ ] 前端测试覆盖核心场景
- [ ] 浏览器验收：至少 3 个真实患者流程
- [ ] 移动端适配（iOS / Android 主流浏览器）
