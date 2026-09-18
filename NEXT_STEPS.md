# Smart HIS 后续功能计划

> 更新于 2026-09-18。第 1 周技术债务已处理，第 2 周患者统一查询与测试补全已实现；本轮前端 24 个文件、137 项测试通过，后端 his-common / his-patient 共 71 项测试通过（无跳过），Lint 与生产构建通过。本轮验证不等同于全项目 API 或迁移验收，第 3–5 周仍待实施。

---

## 一、FEATURE_ROADMAP 中尚未实现的子功能

### 1.1 M4：门诊临床效率（部分未完成）

| 功能 | 当前状态 | 需要做的事 | 优先级 |
|------|----------|-----------|--------|
| 病历模板管理 | 后端已完成（RecordTemplateController CRUD + 科室过滤） | 前端 ClinicalPage 已接入模板选择，基本可用 | — 已完成 |
| **常用短语** | 未实现 | 新增 `cli_common_phrase` 表（科室/个人两级），后端 CRUD 接口，前端病历编辑器中插入主诉/现病史/查体/诊疗计划短语 | 中 |
| **病历自动保存与版本** | 未实现 | 新增 `cli_record_version` 表（record_id, version_no, content_json, created_by, created_time），后端定时保存草稿接口（`POST /api/clinical/records/draft`），前端 debounce 自动调用，恢复草稿接口，签署后锁定版本 | 中 |
| **患者统一查询** | 已实现 | `GET /api/patient/patients/search?keyword=xxx` 支持系统 ID、EMPI、姓名/拼音、证件号、手机号；患者、挂号、收费页面统一接入；收费拒绝无匹配/多匹配并保留长 ID | — 已完成 |

### 1.2 M5：检验检查与处方闭环（部分未完成）

| 功能 | 当前状态 | 需要做的事 | 优先级 |
|------|----------|-----------|--------|
| 检验申请与结果回写 | 后端已完成（ExamRequestController 全生命周期 + 危急值确认） | 前端 ClinicalPage 已接入检查申请，基本可用 | — 已完成 |
| **PACS/外部检查对接** | 仅预留概念，无字段 | `cli_exam_request` 增加 `external_exam_id`、`pacs_url`、`report_attachment_url` 字段（V4 迁移），后端更新接口支持写入，前端报告查看页展示链接 | 低 |
| 电子处方与用药校验 | 后端已完成（药品安全知识库 + RxReview 审核流程） | 前端 PharmaPage 已接入，相关测试纳入前端全量回归 | — 已完成 |

### 1.3 M7：收费医保（部分未完成）

| 功能 | 当前状态 | 需要做的事 | 优先级 |
|------|----------|-----------|--------|
| 收费项目/预交金/对账/发票 | 全部已完成 | — | — 已完成 |
| **医保结算适配层** | 仅有本地确定性结算预览（InsuranceController.preview） | 设计医保接口适配层：定义 `InsuranceSettlementPort` 接口，本地预览作为默认实现，预留异地医保/省平台对接实现；增加医保目录映射表 `ops_insurance_catalog`（fee_item_id ↔ 医保编码），前端收费页增加"医保预结算"按钮 | 低（依赖外部系统） |

---

## 二、技术债务与安全加固

### 2.1 网关安全（第 1 周已处理）

| 问题 | 当前状态 |
|------|----------|
| **DEBUG 日志泄露 JWT** | 应用及 Gateway 日志级别已改为 `INFO` |
| **CORS 允许所有来源** | 已使用 `HIS_CORS_ALLOWED_ORIGINS` 配置允许来源，默认仅 localhost；部署时须设置实际可信来源 |
| **Emergency 缺健康路由** | 已增加 `/api/emergency/health` 路由（`SetPath=/health`，`order: -1`），工作台已纳入急诊监控 |

### 2.2 dormant 模块清理（第 1 周已处理）

已清理 `his-cdss`、`his-collaboration`、`his-drg`、`his-platform`、`his-resource` 的遗留 `target/` 编译产物，未删除业务源码。

### 2.3 前端测试覆盖缺口

第 2 周新增以下五个测试文件，覆盖现有功能，不将尚未实现的页面算作已测：

| 页面/服务 | 状态 | 已覆盖范围 |
|-----------|------|------------|
| `authApi.ts` | 已补充 7 项 | 登录/登出 HTTP 契约、OTP 传递、异常响应；刷新逻辑保留既有 `http.test.ts` 覆盖 |
| `LoginPage.tsx` | 已补充 13 项 | 必填、OTP 前导零、失败重试、防重复提交、登录后返回原页面 |
| `PatientPage.tsx` | 已补充 9 项 | 五类标识搜索、脱敏、建档/编辑校验、失败保留表单、长 ID |
| `RegistrationPage.tsx` | 已补充 10 项 | 科室/医生筛选、号源状态、患者搜索、长 ID、冲突与失败重试 |
| `InpatientEmergencyPage.tsx` | 已补充 16 项 | 已有待入院记录办理入院、转科转床、出院归档、权限、分诊与状态流转、长 ID 与非法输入 |
| `AnalyticsSecurityPage.tsx` | 待补充 | 日期筛选、权限降级提示、报表查询/导出，随第 4 周推进 |

同时扩充 `patientApi.test.ts` 和 `OperationsPage.test.tsx`。MFA 二维码/设置弹窗、前端新建住院登记不属于当前已实现与已测范围。

---

## 三、功能增强建议

### 3.1 消息通知系统（中优先级）

当前系统没有任何通知机制。建议增加：

- **危急值推送**：检验危急值确认后，通过 WebSocket 推送主治医生浏览器通知
- **库存预警**：近效期批次、库存低于安全阈值时推送药房工作台
- **排队叫号**：急诊分诊后通知目标科室医生
- 技术方案：Spring WebSocket + STOMP，前端 `useWebSocket` hook

### 3.2 报表与统计增强（中优先级）

当前 AnalyticsController 仅提供基础汇总。可扩展：

- **收入趋势图**：按日/周/月折线图（前端 ECharts / AntV）
- **药品使用排名**：TOP 20 药品用量/金额排行
- **抗生素使用率**：按科室统计抗菌药处方占比
- **医保拒付分析**：预留医保对接后的拒付原因统计
- **患者满意度**：预留评价入口

### 3.3 打印增强（低优先级）

- **处方签打印**：当前 PharmaPage 有"打印审核单"按钮但仅 `window.print()`，需设计标准处方签格式（医院名称、患者信息、药品列表、医师签名栏）
- **住院费用清单**：出院时打印每日费用明细
- **腕带/标签打印**：住院患者腕带、药品标签

### 3.4 数据导入/导出（低优先级）

- **患者批量导入**：Excel 模板导入患者基本信息
- **药品目录初始化**：从国家药品编码 Excel 批量导入
- **历史数据迁移工具**：从旧 HIS 系统导入病历、账单等

---

## 四、基础设施与运维

### 4.1 CI/CD 流水线

当前项目无自动化 CI/CD。建议：

```yaml
# GitHub Actions 示例
name: CI
on: [push, pull_request]
jobs:
  backend:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
    steps:
      - uses: actions/setup-java@v4
        with: { java-version: '21' }
      - run: ./mvnw clean verify

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - working-directory: smart-his-frontend
        run: npm ci && npm test && npm run lint && npm run build
```

### 4.2 容器化部署

当前仅 Docker Compose 启动基础设施。可增加：

- 各服务 Dockerfile（multi-stage build：JDK 21 + Maven build → JRE 21 runtime）
- `docker-compose.yml` 增加应用服务定义（profiles 区分 dev/prod）
- K8s manifests 已在 `k8s/` 目录，需补充 HPA、PodDisruptionBudget、健康检查

### 4.3 监控与告警

- 接入 Spring Boot Actuator + Prometheus metrics
- Grafana 仪表盘：JVM 内存/GC、HTTP 延迟/错误率、数据库连接池
- 日志聚合：ELK 或 Loki，替代本地 `.run/*.log`

---

## 五、建议执行顺序

```
第 1 周：技术债务清理（已完成）
  ├── 修复网关 DEBUG 日志泄露 JWT
  ├── 修复 CORS 配置
  ├── 增加 emergency 健康路由 + 工作台监控
  └── 清理 dormant 模块 target/

第 2 周：患者统一查询 + 前端测试补全（已实现）
  ├── 统一搜索接口 GET /api/patient/patients/search
  ├── 患者、挂号、收费页面接入并保留长 ID
  ├── authApi / LoginPage / PatientPage 测试
  ├── RegistrationPage / InpatientEmergencyPage 测试
  └── 修复登录返回目标页竞争和急诊分诊长 ID 舍入

第 3 周：病历常用短语 + 自动保存
  ├── cli_common_phrase 表 + CRUD 接口
  ├── cli_record_version 表 + 草稿保存/恢复接口
  └── 前端 ClinicalPage 接入短语插入和自动保存

第 4 周：报表增强
  ├── 收入趋势图（前端 ECharts）
  ├── 药品使用排名
  └── 抗生素使用率统计

第 5 周：CI/CD + 容器化
  ├── GitHub Actions 流水线
  ├── 各服务 Dockerfile
  └── docker-compose 应用服务定义

后续按需：
  ├── 消息通知系统（WebSocket）
  ├── 医保结算适配层
  ├── 打印模板设计
  └── 数据导入工具
```

### 第 2 周验收记录（2026-09-18）

- 自动化：前端 `npm test -- --maxWorkers=2`，24 个文件、137 项全部通过；`npm run lint` 无警告或错误；`npm run build` 通过，仅保留已有的大于 500 kB chunk 提示。
- 后端：`his-common` / `his-patient` 的 Maven `test` 共 71 项通过，Failures / Errors / Skipped 均为 0；包含搜索 SQL 条件、长 ID 边界及新旧路由参数绑定测试。本轮无数据库迁移，未执行全模块或 Flyway 验收。
- 真实只读浏览器验证：使用隔离网关和只读患者服务，登录后返回原患者页；五类标识均能查询同一现有患者；长 ID / EMPI 页面搜索、空结果提示、患者详情及编辑入口正常；收费手机号解析出的患者 ID 保持字符串；挂号患者选择使用统一搜索。
- 边界验证：浏览器模拟多匹配及患者服务失败，均显示明确提示且不追加错误账单查询，重置恢复正常；分诊表单拒绝科学计数法输入，19 位 ID 保持原值。
- 验收边界：浏览器未提交建档、挂号、入院、出院或急诊分诊等临床写操作，写入成功/失败与状态流转通过传输层模拟的自动化测试覆盖；不将其描述为真实数据库写入验收。原有运行服务未替换，本轮也未提交或推送 GitHub。

---

## 六、当前系统能力总结

| 维度 | 数据 |
|------|------|
| 后端服务 | 7 个（auth, patient, clinical, operations, pharma, emergency, gateway） |
| API 端点 | ~150 个 |
| 数据库表 | ~50 张（跨 6 个 schema） |
| 前端页面 | 8 个路由页面 |
| 前端测试 | 24 个文件、137 项，全量通过（`npm test -- --maxWorkers=2`） |
| 后端测试 | 本轮 `his-common` 36 项 + `his-patient` 35 项通过，无跳过；未重跑全模块或 Flyway 验证 |
| RBAC 角色 | 9 个 |
| 权限点 | 112 个 |
| 演示用户 | 8 个（覆盖管理员、医生、护士、药师、挂号员） |
