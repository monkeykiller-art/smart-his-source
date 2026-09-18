# Smart HIS 后续功能计划

> 截至 2026-09-18，M4–M9 主体功能已实现并通过 API 验收（70 个前端测试全通过，150+ 后端 API 均返回 200）。本文档梳理尚未完成的功能缺口、技术债务和可增强项，按优先级排列。

---

## 一、FEATURE_ROADMAP 中尚未实现的子功能

### 1.1 M4：门诊临床效率（部分未完成）

| 功能 | 当前状态 | 需要做的事 | 优先级 |
|------|----------|-----------|--------|
| 病历模板管理 | 后端已完成（RecordTemplateController CRUD + 科室过滤） | 前端 ClinicalPage 已接入模板选择，基本可用 | — 已完成 |
| **常用短语** | 未实现 | 新增 `cli_common_phrase` 表（科室/个人两级），后端 CRUD 接口，前端病历编辑器中插入主诉/现病史/查体/诊疗计划短语 | 中 |
| **病历自动保存与版本** | 未实现 | 新增 `cli_record_version` 表（record_id, version_no, content_json, created_by, created_time），后端定时保存草稿接口（`POST /api/clinical/records/draft`），前端 debounce 自动调用，恢复草稿接口，签署后锁定版本 | 中 |
| **患者统一查询** | 部分实现（按证件号 `/idno` 和列表分页） | 新增统一搜索接口 `GET /api/patients/search?keyword=xxx`，同时匹配系统 ID、EMPI、姓名、证件号、手机号；运营收费页从 EMPI 解析到系统患者 ID | 高 |

### 1.2 M5：检验检查与处方闭环（部分未完成）

| 功能 | 当前状态 | 需要做的事 | 优先级 |
|------|----------|-----------|--------|
| 检验申请与结果回写 | 后端已完成（ExamRequestController 全生命周期 + 危急值确认） | 前端 ClinicalPage 已接入检查申请，基本可用 | — 已完成 |
| **PACS/外部检查对接** | 仅预留概念，无字段 | `cli_exam_request` 增加 `external_exam_id`、`pacs_url`、`report_attachment_url` 字段（V4 迁移），后端更新接口支持写入，前端报告查看页展示链接 | 低 |
| 电子处方与用药校验 | 后端已完成（药品安全知识库 + RxReview 审核流程） | 前端 PharmaPage 已完成，70 个测试通过 | — 已完成 |

### 1.3 M7：收费医保（部分未完成）

| 功能 | 当前状态 | 需要做的事 | 优先级 |
|------|----------|-----------|--------|
| 收费项目/预交金/对账/发票 | 全部已完成 | — | — 已完成 |
| **医保结算适配层** | 仅有本地确定性结算预览（InsuranceController.preview） | 设计医保接口适配层：定义 `InsuranceSettlementPort` 接口，本地预览作为默认实现，预留异地医保/省平台对接实现；增加医保目录映射表 `ops_insurance_catalog`（fee_item_id ↔ 医保编码），前端收费页增加"医保预结算"按钮 | 低（依赖外部系统） |

---

## 二、技术债务与安全加固

### 2.1 网关安全（高优先级）

| 问题 | 影响 | 修复方案 |
|------|------|---------|
| **DEBUG 日志泄露 JWT** | `his-gateway/application.yml` 的 `com.smarthis: DEBUG` + `org.springframework.cloud.gateway: DEBUG` 将完整 `Authorization: Bearer <JWT>` 写入 `.run/his-gateway.log` | 将日志级别改为 `INFO`，仅对 `org.springframework.security` 保留 `DEBUG`；或在网关过滤器中对 `Authorization` header 做脱敏处理 |
| **CORS 允许所有来源** | `allowedOriginPatterns: "*"` + `allowCredentials: true` 在生产环境会被浏览器拒绝，且存在 CSRF 风险 | 改为配置式白名单 `allowedOriginPatterns: http://localhost:*,https://his.example.com`，通过环境变量注入 |
| **Emergency 缺健康路由** | 其余 6 个服务都有 `his-*-health` 网关路由，emergency 只有业务路由，`/api/emergency/health` 返回 404 | 在网关 `application.yml` 增加 `his-emergency-health` 路由（`SetPath=/health`，`order: -1`）；Dashboard 工作台纳入 emergency 服务监控 |

### 2.2  dormant 模块清理（低优先级）

`his-cdss`、`his-collaboration`、`his-drg`、`his-platform`、`his-resource` 的 `target/` 目录仍残留编译产物，但源码已删除。清理这些空目录避免混淆。

### 2.3 前端测试覆盖缺口

以下页面/服务缺少单元测试：

| 缺失测试 | 影响 | 建议 |
|----------|------|------|
| `authApi.ts` 无测试 | 登录/刷新/MFA 流程无自动化保障 | 补充 login、refresh、mfaSetup、mfaVerify 的 adapter mock 测试 |
| `LoginPage.tsx` 无测试 | 登录表单交互（错误提示、MFA 弹窗）无保障 | 补充登录失败提示、MFA 二维码展示测试 |
| `PatientPage.tsx` 无测试 | 患者创建/编辑/搜索交互无保障 | 补充表单验证、搜索结果展示测试 |
| `RegistrationPage.tsx` 无测试 | 挂号流程无保障 | 补充科室/医生选择、时段冲突提示测试 |
| `InpatientEmergencyPage.tsx` 无测试 | 住院/急诊 UI 无保障 | 补充入院/转科/出院/分诊交互测试 |
| `AnalyticsSecurityPage.tsx` 无测试 | 报表查询/导出无保障 | 补充日期筛选、权限降级提示测试 |

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
第 1 周：技术债务清理
  ├── 修复网关 DEBUG 日志泄露 JWT
  ├── 修复 CORS 配置
  ├── 增加 emergency 健康路由 + 工作台监控
  └── 清理 dormant 模块 target/

第 2 周：患者统一查询 + 前端测试补全
  ├── 统一搜索接口 GET /api/patients/search
  ├── 前端 authApi / LoginPage / PatientPage 测试
  └── 前端 RegistrationPage / InpatientEmergencyPage 测试

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

---

## 六、当前系统能力总结

| 维度 | 数据 |
|------|------|
| 后端服务 | 7 个（auth, patient, clinical, operations, pharma, emergency, gateway） |
| API 端点 | ~150 个 |
| 数据库表 | ~50 张（跨 6 个 schema） |
| 前端页面 | 8 个路由页面 |
| 前端测试 | 70 个（全通过） |
| 后端测试 | 覆盖安全切面、拦截器、分析服务、迁移验证 |
| RBAC 角色 | 9 个 |
| 权限点 | 112 个 |
| 演示用户 | 8 个（覆盖管理员、医生、护士、药师、挂号员） |
