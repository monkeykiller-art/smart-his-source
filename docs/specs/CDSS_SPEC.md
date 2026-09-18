# CDSS 临床决策支持系统技术规格

> 版本：1.0-draft  
> 状态：待评审  
> 编制日期：2026-09-18  
> 依赖：现有药品安全知识库（`pha_drug_safety` 表）

---

## 一、概述

### 1.1 目标

在医生开立医嘱、处方时提供实时决策支持，减少用药错误和不良反应。

### 1.2 范围

- 药物相互作用检查（Drug-Drug Interaction, DDI）
- 诊断-用药合理性审查
- 药物-过敏交叉校验
- 危急值自动提醒（已有后端基础，需增强前端展示）
- 临床路径模板（后续阶段）

### 1.3 不在范围

- 复杂临床路径引擎（第 3 批）
- 基于 AI 的诊断建议
- 与外部 CDSS 系统对接

---

## 二、架构设计

### 2.1 服务划分

新增 `his-cdss` 服务（激活 dormant 模块），独立部署，避免污染现有临床服务。

```
his-cdss (端口 8086)
  ├── controller/
  │   ├── InteractionController      # DDI 查询
  │   ├── AllergyController          # 过敏交叉校验
  │   └── DiagnosisDrugController    # 诊断-用药合理性
  ├── service/
  │   ├── InteractionService
  │   ├── AllergyCrossCheckService
  │   └── DiagnosisDrugReviewService
  ├── repository/
  │   ├── DrugInteractionRepository
  │   ├── AllergyCrossRepository
  │   └── DiagnosisDrugRuleRepository
  └── dto/
      ├── InteractionCheckRequest
      ├── InteractionCheckResponse
      └── AlertLevel (枚举: INFO, WARNING, CRITICAL)
```

### 2.2 调用链路

```
前端 ClinicalPage / PharmaPage
  ↓ POST /api/cdss/interactions/check
his-gateway → his-cdss
  ↓ 查询规则库
返回告警列表
  ↓
前端弹窗展示，医生确认后继续
```

### 2.3 数据流

```
医生选择药品 → 前端收集当前患者所有药品 + 新药品
  → POST /api/cdss/interactions/check
  → 返回 [{drugA, drugB, level, description, suggestion}]
  → 前端根据 level 展示不同样式
  → CRITICAL 级别强制弹窗，医生需填写理由才能继续
```

---

## 三、数据库设计

### 3.1 新增表（schema: cdss）

```sql
-- 药物相互作用规则表
CREATE TABLE cdss.cdss_drug_interaction (
    id BIGSERIAL PRIMARY KEY,
    drug_a_id BIGINT NOT NULL REFERENCES pharma.pha_drug(id),
    drug_b_id BIGINT NOT NULL REFERENCES pharma.pha_drug(id),
    interaction_level VARCHAR(20) NOT NULL,  -- CONTRAINDICATED, MAJOR, MODERATE, MINOR
    description TEXT NOT NULL,
    mechanism TEXT,                          -- 作用机制
    clinical_effect TEXT,                    -- 临床效应
    management TEXT,                         -- 处理建议
    evidence_level VARCHAR(10),              -- 证据等级: A, B, C, D
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_different_drugs CHECK (drug_a_id <> drug_b_id),
    CONSTRAINT chk_unique_pair UNIQUE (drug_a_id, drug_b_id)
);

-- 诊断-用药规则表
CREATE TABLE cdss.cdss_diagnosis_drug_rule (
    id BIGSERIAL PRIMARY KEY,
    icd10_code VARCHAR(10) NOT NULL,         -- ICD-10 编码
    drug_id BIGINT REFERENCES pharma.pha_drug(id),
    drug_class_id BIGINT,                    -- 药品分类（可按类限制）
    rule_type VARCHAR(20) NOT NULL,          -- RECOMMENDED, CONTRAINDICATED, CAUTION
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT NOW()
);

-- 过敏交叉反应表
CREATE TABLE cdss.cdss_allergy_cross (
    id BIGSERIAL PRIMARY KEY,
    allergen_type VARCHAR(20) NOT NULL,      -- DRUG, FOOD, ENVIRONMENT
    allergen_id BIGINT NOT NULL,             -- 过敏原 ID（药品 ID 或其他）
    cross_allergen_id BIGINT NOT NULL,       -- 交叉过敏原 ID
    cross_probability VARCHAR(20),           -- HIGH, MEDIUM, LOW
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time TIMESTAMP DEFAULT NOW()
);

-- CDSS 告警记录表（审计）
CREATE TABLE cdss.cdss_alert_log (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT,
    encounter_id BIGINT,
    alert_type VARCHAR(30) NOT NULL,         -- DDI, ALLERGY, DIAGNOSIS_DRUG
    alert_level VARCHAR(20) NOT NULL,
    drug_ids BIGINT[],                       -- 涉及的药品 ID 数组
    description TEXT,
    doctor_action VARCHAR(20),               -- ACCEPTED, OVERRIDDEN, CANCELLED
    override_reason TEXT,                    -- 覆盖原因
    doctor_id BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_interaction_drug_a ON cdss.cdss_drug_interaction(drug_a_id);
CREATE INDEX idx_interaction_drug_b ON cdss.cdss_drug_interaction(drug_b_id);
CREATE INDEX idx_diagnosis_icd10 ON cdss.cdss_diagnosis_drug_rule(icd10_code);
CREATE INDEX idx_alert_patient ON cdss.cdss_alert_log(patient_id, created_time);
```

### 3.4 初始数据

从以下来源导入初始规则：
- 药品说明书中的配伍禁忌
- 《临床用药须知》中的药物相互作用
- 国内 DDInter 数据库（如有）
- 专家手工录入高频规则（初期 50-100 条）

---

## 四、API 设计

### 4.1 药物相互作用检查

```http
POST /api/cdss/interactions/check
Content-Type: application/json

{
  "patientId": 2099000000000000001,
  "drugIds": [101, 102, 103],  // 当前所有药品（含新开）
  "encounterId": 5001           // 可选，用于记录日志
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "alerts": [
      {
        "type": "DDI",
        "level": "CRITICAL",
        "drugA": {"id": 101, "name": "华法林"},
        "drugB": {"id": 102, "name": "阿司匹林"},
        "description": "两药合用显著增加出血风险",
        "mechanism": "华法林抗凝 + 阿司匹林抗血小板协同作用",
        "management": "避免合用，或密切监测 INR 和出血症状",
        "evidenceLevel": "A"
      }
    ],
    "maxLevel": "CRITICAL"
  }
}
```

### 4.2 过敏交叉校验

```http
POST /api/cdss/allergy/check
Content-Type: application/json

{
  "patientId": 2099000000000000001,
  "drugId": 205
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "patientAllergies": [
      {"type": "DRUG", "id": 201, "name": "青霉素", "severity": "SEVERE"}
    ],
    "crossAlerts": [
      {
        "allergen": {"id": 201, "name": "青霉素"},
        "crossDrug": {"id": 205, "name": "阿莫西林"},
        "probability": "HIGH",
        "description": "青霉素类交叉过敏率高，阿莫西林禁用"
      }
    ]
  }
}
```

### 4.3 诊断-用药合理性

```http
POST /api/cdss/diagnosis-drug/check
Content-Type: application/json

{
  "diagnosisCodes": ["I10"],  // 高血压
  "drugId": 301
}
```

**响应 200**：
```json
{
  "code": 200,
  "data": {
    "alerts": [
      {
        "ruleType": "RECOMMENDED",
        "description": "该药品为高血压一线用药",
        "evidenceLevel": "A"
      }
    ]
  }
}
```

---

## 五、前端设计

### 5.1 组件结构

```typescript
// CdssAlertModal.tsx
interface CdssAlertModalProps {
  alerts: CdssAlert[];
  onAccept: () => void;           // 接受建议（修改医嘱）
  onOverride: (reason: string) => void;  // 覆盖（继续）
  onCancel: () => void;           // 取消医嘱
}

// 根据 level 展示不同样式
// CRITICAL: 红色背景，强制弹窗，必须选择
// WARNING: 橙色背景，可关闭但记录
// INFO: 蓝色提示，可忽略
```

### 5.2 集成点

- **ClinicalPage**：开立医嘱时，提交药品列表检查
- **PharmaPage**：药师审核时，二次检查
- **NursingPage**（后续）：护士执行前检查

### 5.3 交互流程

```
医生点击"提交医嘱"
  ↓
前端调用 POST /api/cdss/interactions/check
  ↓
无告警 → 直接提交
  ↓
有告警 → 弹出 CdssAlertModal
  ↓
CRITICAL → 医生必须选择：
  - 修改医嘱（返回编辑）
  - 覆盖（填写理由）→ 记录日志后继续
  - 取消
WARNING/INFO → 医生可关闭弹窗继续
```

---

## 六、安全与审计

### 6.1 权限

- `cdss:alert:view`：查看告警
- `cdss:alert:override`：覆盖 CRITICAL 告警（仅主治及以上）

### 6.2 审计

所有 CRITICAL 告警的覆盖操作记录到 `cdss_alert_log`，包括：
- 医生 ID
- 覆盖原因
- 时间戳

### 6.3 性能

- 规则库缓存（Redis）：药品相互作用规则变化少，缓存 24 小时
- 查询优化：`drug_a_id` 和 `drug_b_id` 建立索引，支持双向查询

---

## 七、测试策略

### 7.1 后端测试

```java
@Test
void detectsContraindicatedDrugPair() {
    // 准备：华法林 + 阿司匹林规则
    // 执行：check([warfarinId, aspirinId])
    // 断言：返回 CRITICAL 告警
}

@Test
void noAlertForSafeCombination() {
    // 准备：无规则
    // 执行：check([drugA, drugB])
    // 断言：返回空列表
}
```

### 7.2 前端测试

```typescript
test('shows modal for CRITICAL interaction', async () => {
  // mock API 返回 CRITICAL 告警
  // 渲染处方页面
  // 点击提交
  // 断言弹窗显示，包含药品名称和处理建议
});

test('requires reason for CRITICAL override', async () => {
  // 点击覆盖按钮
  // 不填写理由直接提交
  // 断言表单校验失败
});
```

### 7.3 验收场景

1. 开立华法林 + 阿司匹林 → 弹窗，无法直接提交
2. 填写覆盖理由后继续 → 记录日志
3. 药师审核时再次看到告警
4. 已覆盖的告警在病历中可见

---

## 八、实施计划

### 阶段 1（2 周）：基础框架

- 激活 `his-cdss` 服务
- 创建数据库表
- 实现 DDI 查询 API
- 前端弹窗组件

### 阶段 2（2 周）：规则填充与集成

- 导入初始规则（50-100 条）
- ClinicalPage 集成
- 过敏交叉校验
- 审计日志

### 阶段 3（1 周）：测试与优化

- 单元测试、集成测试
- 浏览器验收
- 性能优化（缓存）

### 阶段 4（后续）：扩展

- 诊断-用药合理性
- 临床路径模板
- 规则管理界面

---

## 九、风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 规则库不完整导致漏报 | 高 | 初期保守策略，宁可多报；建立规则反馈机制 |
| 告警过多导致医生疲劳 | 中 | 分级展示，CRITICAL 才强制；定期评估告警频率 |
| 性能问题影响开嘱速度 | 中 | 缓存规则库；异步检查（非阻塞） |
| 医生覆盖所有告警 | 高 | 审计覆盖原因；定期回顾；培训 |

---

## 十、验收标准

- [ ] 药物相互作用检查响应时间 < 200ms
- [ ] CRITICAL 告警无法直接跳过，必须覆盖或取消
- [ ] 所有覆盖操作记录审计日志
- [ ] 前端测试覆盖核心场景
- [ ] 浏览器验收：至少 3 个真实药物相互作用场景
