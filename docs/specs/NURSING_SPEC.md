# 护理文书与护理计划技术规格

> 版本：1.0-draft  
> 状态：待评审  
> 编制日期：2026-09-18  
> 依赖：M8 住院模块（入院、转科、出院流程已完成）

---

## 一、概述

### 1.1 目标

完善住院护理闭环，支持护理评估、护理计划、护理记录和医嘱执行确认。

### 1.2 范围

- 入院护理评估（跌倒、压疮、营养筛查）
- 护理计划（诊断、目标、措施）
- 护理记录单（体温单、出入量、病情记录）
- 护理评估量表（Braden、Morse、NRS）
- 医嘱执行确认（护士签名、时间记录）

### 1.3 不在范围

- 移动护理 PDA 端（第 3 批）
- 护理排班管理
- 护理质量管理指标

---

## 二、架构设计

### 2.1 服务划分

新增 `his-nursing` 服务，独立部署。

```
his-nursing (端口 8087)
  ├── controller/
  │   ├── AssessmentController       # 护理评估
  │   ├── CarePlanController         # 护理计划
  │   ├── NursingRecordController    # 护理记录
  │   ├── ScaleController            # 评估量表
  │   └── OrderExecutionController   # 医嘱执行确认
  ├── service/
  │   ├── AssessmentService
  │   ├── CarePlanService
  │   ├── NursingRecordService
  │   ├── ScaleService
  │   └── OrderExecutionService
  ├── repository/
  │   ├── AssessmentRepository
  │   ├── CarePlanRepository
  │   ├── NursingRecordRepository
  │   ├── ScaleRepository
  │   └── OrderExecutionRepository
  └── dto/
      ├── AssessmentRequest/Response
      ├── CarePlanRequest/Response
      ├── NursingRecordRequest/Response
      ├── ScaleScoreRequest/Response
      └── OrderExecutionRequest/Response
```

### 2.2 调用链路

```
前端 NursingPage (新增)
  ↓ REST API
his-gateway → his-nursing
  ↓ 查询患者、医嘱（跨服务）
his-patient (患者信息)
his-clinical (医嘱信息)
his-operations (住院信息)
```

### 2.3 前端路由

```
/nursing                    # 护理工作台（患者列表）
/nursing/:admissionId/assessment   # 护理评估
/nursing/:admissionId/plan         # 护理计划
/nursing/:admissionId/records      # 护理记录
/nursing/:admissionId/orders       # 医嘱执行
```

---

## 三、数据库设计

### 3.1 新增表（schema: nursing）

```sql
-- 护理评估表
CREATE TABLE nursing.nur_assessment (
    id BIGSERIAL PRIMARY KEY,
    admission_id BIGINT NOT NULL REFERENCES operations.ops_admission(id),
    patient_id BIGINT NOT NULL,
    assessment_type VARCHAR(30) NOT NULL,  -- ADMISSION, SHIFT, SPECIAL
    fall_risk BOOLEAN,                     -- 跌倒风险
    fall_risk_score INT,                   -- Morse 评分
    pressure_ulcer_risk BOOLEAN,           -- 压疮风险
    pressure_ulcer_score INT,              -- Braden 评分
    nutrition_risk BOOLEAN,                -- 营养风险
    nutrition_score INT,                   -- NRS2002 评分
    pain_score INT,                        -- 疼痛评分 (0-10)
    allergy_info TEXT,                     -- 过敏信息
    special_notes TEXT,                    -- 特殊注意事项
    assessed_by BIGINT NOT NULL,           -- 评估护士 ID
    assessed_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 护理诊断表
CREATE TABLE nursing.nur_diagnosis (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES nursing.nur_assessment(id),
    diagnosis_code VARCHAR(20),            -- NANDA 编码（可选）
    diagnosis_name VARCHAR(200) NOT NULL,
    related_factors TEXT,                  -- 相关因素
    defining_characteristics TEXT,         -- 定义特征
    priority INT,                          -- 优先级 1-3
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT NOW()
);

-- 护理计划表
CREATE TABLE nursing.nur_care_plan (
    id BIGSERIAL PRIMARY KEY,
    diagnosis_id BIGINT NOT NULL REFERENCES nursing.nur_diagnosis(id),
    goal TEXT NOT NULL,                    -- 预期目标
    interventions TEXT NOT NULL,           -- 护理措施
    evaluation TEXT,                       -- 效果评价
    planned_date DATE,
    actual_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',   -- ACTIVE, COMPLETED, CANCELLED
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 护理记录表
CREATE TABLE nursing.nur_record (
    id BIGSERIAL PRIMARY KEY,
    admission_id BIGINT NOT NULL REFERENCES operations.ops_admission(id),
    record_type VARCHAR(30) NOT NULL,      -- VITAL_SIGN, INTAKE_OUTPUT, NURSING_NOTE, HANDOVER
    record_time TIMESTAMP NOT NULL,
    
    -- 生命体征（record_type = VITAL_SIGN）
    temperature DECIMAL(4,1),              -- 体温 ℃
    pulse INT,                             -- 脉搏 次/分
    respiration INT,                       -- 呼吸 次/分
    blood_pressure VARCHAR(20),            -- 血压 mmHg (如 "120/80")
    oxygen_saturation DECIMAL(4,1),        -- 血氧饱和度 %
    blood_sugar DECIMAL(4,1),              -- 血糖 mmol/L
    
    -- 出入量（record_type = INTAKE_OUTPUT）
    intake_ml INT,                         -- 入量 ml
    output_ml INT,                         -- 出量 ml
    intake_details TEXT,                   -- 入量明细
    output_details TEXT,                   -- 出量明细
    
    -- 护理记录/交接班（record_type = NURSING_NOTE / HANDOVER）
    content TEXT,                          -- 记录内容
    
    recorded_by BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 医嘱执行记录表
CREATE TABLE nursing.nur_order_execution (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,              -- 医嘱 ID（来自 his-clinical）
    admission_id BIGINT NOT NULL,
    execution_status VARCHAR(20) NOT NULL, -- PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    planned_time TIMESTAMP,                -- 计划执行时间
    actual_time TIMESTAMP,                 -- 实际执行时间
    executed_by BIGINT,                    -- 执行护士 ID
    verified_by BIGINT,                    -- 核对护士 ID（双人核对）
    execution_note TEXT,                   -- 执行备注
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW()
);

-- 评估量表定义表
CREATE TABLE nursing.nur_scale (
    id BIGSERIAL PRIMARY KEY,
    scale_code VARCHAR(30) UNIQUE NOT NULL,  -- MORSE, BRADEN, NRS2002
    scale_name VARCHAR(100) NOT NULL,
    description TEXT,
    min_score INT,
    max_score INT,
    risk_threshold INT,                    -- 风险阈值
    items JSONB,                           -- 量表项目（JSON）
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_assessment_admission ON nursing.nur_assessment(admission_id);
CREATE INDEX idx_record_admission_time ON nursing.nur_record(admission_id, record_time);
CREATE INDEX idx_order_exec_admission ON nursing.nur_order_execution(admission_id, execution_status);
CREATE INDEX idx_diagnosis_assessment ON nursing.nur_diagnosis(assessment_id);
```

### 3.2 初始数据

```sql
-- Morse 跌倒评估量表
INSERT INTO nursing.nur_scale (scale_code, scale_name, items, min_score, max_score, risk_threshold)
VALUES (
    'MORSE',
    'Morse 跌倒风险评估量表',
    '[
        {"item": "跌倒史", "options": [{"value": 0, "text": "无"}, {"value": 25, "text": "有"}]},
        {"item": "医学诊断", "options": [{"value": 0, "text": "无"}, {"value": 15, "text": "有"}]},
        {"item": "行走辅助", "options": [{"value": 0, "text": "无/卧床/轮椅"}, {"value": 15, "text": "拐杖"}, {"value": 30, "text": "助行器"}]},
        {"item": "静脉治疗", "options": [{"value": 0, "text": "无"}, {"value": 20, "text": "有"}]},
        {"item": "步态", "options": [{"value": 0, "text": "正常/卧床"}, {"value": 10, "text": "虚弱"}, {"value": 20, "text": "不平衡"}]},
        {"item": "认知状态", "options": [{"value": 0, "text": "自知量力"}, {"value": 15, "text": "高估/忘记限制"}]}
    ]'::jsonb,
    0, 125, 45
);

-- Braden 压疮评估量表
INSERT INTO nursing.nur_scale (scale_code, scale_name, items, min_score, max_score, risk_threshold)
VALUES (
    'BRADEN',
    'Braden 压疮风险评估量表',
    '[
        {"item": "感觉感知", "options": [{"value": 1, "text": "完全受限"}, {"value": 2, "text": "非常受限"}, {"value": 3, "text": "轻度受限"}, {"value": 4, "text": "未受损"}]},
        {"item": "潮湿", "options": [{"value": 1, "text": "持续潮湿"}, {"value": 2, "text": "非常潮湿"}, {"value": 3, "text": "偶尔潮湿"}, {"value": 4, "text": "很少潮湿"}]},
        {"item": "活动能力", "options": [{"value": 1, "text": "卧床"}, {"value": 2, "text": "局限"}, {"value": 3, "text": "偶尔行走"}, {"value": 4, "text": "频繁行走"}]},
        {"item": "移动能力", "options": [{"value": 1, "text": "完全不能"}, {"value": 2, "text": "非常受限"}, {"value": 3, "text": "轻度受限"}, {"value": 4, "text": "不受限"}]},
        {"item": "营养", "options": [{"value": 1, "text": "非常差"}, {"value": 2, "text": "可能不足"}, {"value": 3, "text": "充足"}, {"value": 4, "text": "优秀"}]},
        {"item": "摩擦力和剪切力", "options": [{"value": 1, "text": "有问题"}, {"value": 2, "text": "潜在问题"}, {"value": 3, "text": "无明显问题"}]}
    ]'::jsonb,
    6, 23, 18
);
```

---

## 四、API 设计

### 4.1 护理评估

```http
POST /api/nursing/assessments
Content-Type: application/json

{
  "admissionId": 1001,
  "assessmentType": "ADMISSION",
  "fallRiskScore": 45,
  "pressureUlcerScore": 16,
  "nutritionScore": 3,
  "painScore": 2,
  "allergyInfo": "青霉素过敏",
  "specialNotes": "老年患者，需关注跌倒风险"
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "id": 501,
    "fallRisk": true,
    "pressureUlcerRisk": true,
    "nutritionRisk": false
  }
}
```

```http
GET /api/nursing/assessments?admissionId=1001
```

### 4.2 护理计划

```http
POST /api/nursing/care-plans
Content-Type: application/json

{
  "diagnosisId": 301,
  "goal": "患者住院期间不发生跌倒",
  "interventions": "1. 床旁护栏\n2. 呼叫器放置易取处\n3. 家属陪护宣教",
  "plannedDate": "2026-09-20"
}
```

### 4.3 护理记录

```http
POST /api/nursing/records
Content-Type: application/json

{
  "admissionId": 1001,
  "recordType": "VITAL_SIGN",
  "recordTime": "2026-09-18T08:00:00",
  "temperature": 36.5,
  "pulse": 72,
  "respiration": 18,
  "bloodPressure": "120/80",
  "oxygenSaturation": 98.0
}
```

```http
GET /api/nursing/records?admissionId=1001&recordType=VITAL_SIGN&startDate=2026-09-18&endDate=2026-09-18
```

### 4.4 医嘱执行确认

```http
POST /api/nursing/orders/{orderId}/execute
Content-Type: application/json

{
  "executionStatus": "COMPLETED",
  "actualTime": "2026-09-18T10:30:00",
  "executionNote": "已按时给药，患者无不适"
}
```

```http
GET /api/nursing/orders?admissionId=1001&executionStatus=PENDING
```

### 4.5 评估量表

```http
POST /api/nursing/scales/{scaleCode}/score
Content-Type: application/json

{
  "admissionId": 1001,
  "answers": [
    {"itemIndex": 0, "selectedValue": 25},
    {"itemIndex": 1, "selectedValue": 15},
    {"itemIndex": 2, "selectedValue": 0},
    {"itemIndex": 3, "selectedValue": 20},
    {"itemIndex": 4, "selectedValue": 10},
    {"itemIndex": 5, "selectedValue": 15}
  ]
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "totalScore": 85,
    "riskLevel": "HIGH",
    "riskThreshold": 45,
    "recommendation": "高风险，需采取跌倒预防措施"
  }
}
```

---

## 五、前端设计

### 5.1 页面结构

```
NursingPage (护理工作台)
  ├── 患者列表（按病区/床位）
  ├── 待执行医嘱数量提示
  └── 点击进入患者护理页面

PatientNursingPage (患者护理详情)
  ├── Tabs
  │   ├── 护理评估 (AssessmentTab)
  │   │   ├── 评估表单（动态渲染量表）
  │   │   └── 历史评估列表
  │   ├── 护理计划 (CarePlanTab)
  │   │   ├── 护理诊断列表
  │   │   └── 计划列表（按诊断分组）
  │   ├── 护理记录 (RecordTab)
  │   │   ├── 体温单图表（ECharts）
  │   │   ├── 出入量统计
  │   │   └── 护理记录/交接班
  │   └── 医嘱执行 (OrderExecutionTab)
  │       ├── 待执行医嘱列表
  │       ├── 执行确认弹窗
  │       └── 执行历史记录
  └── 侧边栏：患者基本信息、过敏史、风险评估结果
```

### 5.2 关键组件

```typescript
// ScaleForm.tsx - 动态量表表单
interface ScaleFormProps {
  scale: NursingScale;
  onSubmit: (answers: ScaleAnswer[]) => void;
}

// TemperatureChart.tsx - 体温单
interface TemperatureChartProps {
  records: VitalSignRecord[];
  dateRange: [Dayjs, Dayjs];
}

// OrderExecutionList.tsx - 医嘱执行列表
interface OrderExecutionListProps {
  orders: OrderExecution[];
  onExecute: (orderId: number, note: string) => void;
}
```

### 5.3 交互流程

**入院评估**：
```
护士点击"新入院评估"
  ↓
选择量表（Morse、Braden、NRS2002）
  ↓
逐项填写，自动计算总分
  ↓
超过阈值 → 自动标记风险，生成护理诊断建议
  ↓
保存评估，触发护理计划创建提醒
```

**医嘱执行**：
```
护士查看待执行医嘱列表
  ↓
点击"执行" → 弹窗确认
  ↓
填写执行备注（可选）
  ↓
确认 → 记录执行时间和护士
  ↓
特殊医嘱需双人核对（如化疗药）→ 第二人扫码确认
```

---

## 六、权限与审计

### 6.1 权限点

```
nursing:assessment:create    # 创建护理评估
nursing:assessment:view      # 查看护理评估
nursing:careplan:create      # 创建护理计划
nursing:careplan:view        # 查看护理计划
nursing:record:create        # 创建护理记录
nursing:record:view          # 查看护理记录
nursing:order:execute        # 执行医嘱
nursing:order:verify         # 核对医嘱（双人核对）
```

### 6.2 角色分配

- **责任护士**：所有护理操作权限
- **护士长**：查看本病区所有患者，审核护理计划
- **医生**：仅查看护理评估和记录

### 6.3 审计

- 所有护理记录修改记录操作人和时间
- 医嘱执行记录执行护士和核对护士
- 评估量表修改保留历史版本

---

## 七、集成点

### 7.1 与 his-clinical 集成

- 查询患者医嘱列表
- 医嘱状态回写（执行完成后更新 `cli_order.status`）
- 危急值提醒（检验结果异常时推送护理工作台）

### 7.2 与 his-operations 集成

- 查询住院患者列表、床位信息
- 入院时自动创建护理评估任务
- 出院时锁定护理记录

### 7.3 与 his-patient 集成

- 查询患者基本信息、过敏史

---

## 八、测试策略

### 8.1 后端测试

```java
@Test
void calculatesMorseScoreCorrectly() {
    // 准备：6 项答案
    // 执行：score("MORSE", answers)
    // 断言：总分 = 85，风险等级 = HIGH
}

@Test
void preventsDuplicateAssessmentForSameAdmission() {
    // 准备：已存在入院评估
    // 执行：再次创建入院评估
    // 断言：抛出 BusinessException
}

@Test
void recordsOrderExecutionWithTimestamp() {
    // 准备：待执行医嘱
    // 执行：execute(orderId, "COMPLETED", note)
    // 断言：execution_status = COMPLETED，actual_time 不为空
}
```

### 8.2 前端测试

```typescript
test('calculates scale score automatically', async () => {
  // 渲染 Morse 量表
  // 填写各项
  // 断言总分自动计算，超过阈值显示风险提示
});

test('displays temperature chart with vital signs', async () => {
  // mock 生命体征数据
  // 渲染体温单
  // 断言图表显示体温、脉搏曲线
});

test('requires nurse signature for order execution', async () => {
  // 点击执行医嘱
  // 断言弹窗要求确认
  // 提交后显示执行记录
});
```

### 8.3 验收场景

1. 新入院患者完成 Morse 评估，评分 85 → 标记跌倒高风险
2. 创建护理计划，3 天后评价效果
3. 记录 24 小时生命体征，体温单显示曲线
4. 执行医嘱，记录时间和护士签名
5. 交接班时查看护理记录，继续记录

---

## 九、实施计划

### 阶段 1（3 周）：基础框架

- 激活 `his-nursing` 服务
- 创建数据库表和迁移脚本
- 实现护理评估、护理记录 API
- 前端护理工作台和评估页面

### 阶段 2（2 周）：护理计划与量表

- 护理诊断和计划 API
- 评估量表动态渲染
- 前端护理计划页面

### 阶段 3（2 周）：医嘱执行

- 医嘱执行确认 API
- 与 his-clinical 集成
- 前端医嘱执行页面

### 阶段 4（1 周）：测试与优化

- 单元测试、集成测试
- 浏览器验收
- 性能优化（体温单大数据量渲染）

---

## 十、风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 护理文书格式不符合规范 | 高 | 参考卫健委《护理文书书写规范》，邀请护理专家评审 |
| 量表评分逻辑复杂 | 中 | 使用 JSONB 存储量表定义，前端动态渲染 |
| 医嘱执行与临床服务耦合紧密 | 中 | 通过事件或消息队列解耦，避免直接数据库调用 |
| 体温单数据量大导致性能问题 | 中 | 分页查询，图表虚拟化渲染 |

---

## 十一、验收标准

- [ ] 入院评估完整流程（Morse、Braden、NRS2002）
- [ ] 护理计划创建、执行、评价
- [ ] 体温单图表显示 7 天生命体征趋势
- [ ] 医嘱执行确认，记录护士签名和时间
- [ ] 护理记录查询和导出
- [ ] 权限控制：不同角色看到授权内容
- [ ] 前端测试覆盖核心场景
- [ ] 浏览器验收：至少 3 个真实护理流程
