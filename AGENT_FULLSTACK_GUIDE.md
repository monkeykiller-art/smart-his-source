# Agent 全栈项目实施流程 — 以 smart-his-source 为例

> 本文讲清楚一件事：**用 AI Agent 协同做全栈项目，到底要按什么顺序走**。  
> 例子就用 `smart-his-source`（Java 21 + Spring Boot 3 + Spring Cloud Alibaba 微服务后端，正在补 React18 + Vite + AntD5 前端）+ 计划中的 Nacos / PostgreSQL 信创底座 + Docker 化交付。

---


## 0. 全局视角：8 个阶段 + Agent 协作模型

```
┌──────────────────────────────────────────────────────────────────────┐
│  Phase 0   Phase 1   Phase 2   Phase 3-4   Phase 5   Phase 6   Phase 7  │
│  需求建模 → 架构选型 → 脚手架 → 前后端开发 → 集成联调 → DevOps → 交付文档 │
└──────────────────────────────────────────────────────────────────────┘
                                    ↓
                            每个阶段都有 4 类 Agent 协作
                                    ↓
        ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
        │ Planner  │ → │ Coder(s) │ → │ Reviewer │ → │ Verifier │
        │  设计/拆解│   │  实现    │   │  审查    │   │  验证    │
        └──────────┘   └──────────┘   └──────────┘   └──────────┘
```

**关键原则**

| 原则                | 说明                                                                   |
| ----------------- | -------------------------------------------------------------------- |
| **阶段交付物必须可验证**    | 每个 phase 结束都有明确的「完成」定义（build 通过 / 测试覆盖 X% / 文档章节齐了）                  |
| **接口契约先于实现**      | API 契约（OpenAPI/TypeScript 类型）跨前后端 Agent 同时生成，**先定契约再写实现**，避免返工       |
| **小步快跑 + 频繁反馈**   | 每个 PR/Commit 都跑 CI（lint + 单元测试 + 契约测试），Agent 自己无法验证的东西就停             |
| **AGENTS.md 是宪法** | 项目根的 `AGENTS.md` 定义所有 Agent 的红线（不能 push、不能动 CI、不能改 secret），人类保留最终决策权 |

---

## Phase 0：需求与建模（1-2 周）

**目标**：把"想做什么"变成"能写测试的规范"。

| Agent 角色          | 输入          | 输出                         |
| ----------------- | ----------- | -------------------------- |
| **Product Agent** | 用户访谈 / 业务痛点 | 用户故事、验收标准（Given-When-Then） |
| **Domain Agent**  | 用户故事        | 领域模型图（实体 + 聚合根 + 领域事件）     |
| **Data Agent**    | 领域模型        | ER 图 + 表结构草案               |

**关键产物**

- `docs/01-requirements/user-stories.md` — 至少 30 条用户故事，按 MoSCoW 排优先级
- `docs/02-domain/model.md` — UML 类图 + 状态机图（特别是临床医嘱状态机、药事处方状态机）
- `docs/02-domain/erd.md` — 实体关系图

**smart-his-source 的现实状态**

- ✅ 13 个微服务模块已经按领域拆分（patient/clinical/resource/pharma/operations…）
- ⚠️ 但 5 个模块是空壳（cdss/drg/platform/emergency/collaboration），需要在 Phase 0 把"业务边界"讲清楚，否则后面 Agent 不知道这 5 个空壳到底要填什么
- ⚠️ 没有用户故事文档 → Phase 0 必须补，否则 AI Agent 只能"按代码猜业务"

**踩坑提示**

- ❌ 让 Agent 直接看代码生成需求 — 会得到"代码在做什么"而不是"用户需要什么"
- ✅ 强迫 Agent 先访谈（让用户口述），再对照代码补差
- ✅ 用户故事必须能翻译成 1 个测试用例（Given-When-Then）

---


## Phase 1：架构与技术选型（1 周）

**目标**：定下不可逆的决策（语言、框架、数据库、部署形态），每个决策写 ADR（Architecture Decision Record）。

| Agent 角色                 | 输入                | 输出                               |
| ------------------------ | ----------------- | -------------------------------- |
| **Architect Agent**      | 需求 + 约束（非功能需求）    | 系统架构图（C4 模型的 L1/L2）              |
| **Tech-Selection Agent** | 架构图 + 团队技能 + 生态调研 | ADR 列表（每条带"选了什么 vs 弃用什么"）        |
| **Contract Agent**       | 用户故事              | OpenAPI 3.1 草案 + TypeScript 类型草案 |

**关键产物**

- `docs/03-architecture/c4-container.md` — 容器图（前端 / 后端 / 数据库 / 消息队列 / 网关）
- `docs/03-architecture/adr/` — 5-8 个 ADR：
  - ADR-001: 前后端技术栈（Java 21 / Spring Boot 3 / React 18 / Vite）
  - ADR-002: 数据库（PostgreSQL 主 + 信创备选达梦/金仓）
  - ADR-003: 中间件（Nacos / Kafka / Redis / Sentinel）
  - ADR-004: 部署形态（K8s + Docker）
  - ADR-005: 鉴权方案（JWT + Spring Security + 网关统一鉴权）
- `openapi/openapi.yaml` — API 契约 v0.1（端点签名 + 请求响应 schema）
- `frontend/src/api/types.ts` — TypeScript 类型（从 OpenAPI 自动生成）

**smart-his-source 的现实状态**

- ✅ 技术栈大方向已定（pom.xml 里 Java 21 + Spring Boot 3 + Spring Cloud Alibaba）
- ⚠️ **没有 ADR** — 选型靠"约定俗成"，新人接手要靠口口相传。**Phase 1 第一件事就是把已有决策补成 ADR**
- ⚠️ **没有 OpenAPI 文档** — 207 个 全靠代码注释，存在就是隐性知识。Phase 1 必须用 Agent 扫 Controller 批量生成 OpenAPI v0.1

**踩坑提示**

- ❌ 在 ADR 里写"我们选 X 因为它很流行" — 必须有 2 个以上备选 + 量化对比（性能、生态、招聘难度）
- ✅ ADR 一旦写完任何人改都要 PR + 至少 1 个 reviewer，避免技术栈漂移

---


## Phase 2：项目脚手架（3-5 天）

**目标**：让后续 Agent 有"模板可填"，避免每个新服务/页面都从零开始。

| Agent 角色                    | 输入                               | 输出                                                      |
| --------------------------- | -------------------------------- | ------------------------------------------------------- |
| **Backend-Scaffold Agent**  | ADR-001 + his-common 现有结构        | his-* 新服务模板（带 BOM、parent、ArchUnit、Dockerfile、CI 步骤）     |
| **Frontend-Scaffold Agent** | ADR-001 + FRONTEND_BUILD_PLAN.md | Vite + React18 + TS + AntD5 + Zustand + React Query 脚手架 |
| **Contract-Gen Agent**      | OpenAPI v0.1                     | 后端 OpenAPI Generator 配置 + 前端自动生成代码 + Mock Server        |

**关键产物**

- **后端**：Maven Wrapper（已在）、JDK 21 校验脚本、ArchUnit 测试模板、每个新服务的 `pom.xml` 模板、Dockerfile 模板
- **前端**：`smart-his-frontend/`（Vite + TS + AntD 5 + 路径别名 + ESLint + Prettier + Husky + Vitest + Playwright）
- **共享**：`openapi/` 仓库（前后端共享契约）
- **CI**：`.github/workflows/` 下 4 条流水线：backend-ci、frontend-ci、contract-ci、image-build

**smart-his-source 的现实状态**

- ⚠️ **his-common 模块过重**（承担了 web/security/observability/contract-api/event/fhir-support 6 个职责），所有服务都依赖它 — 拆分前先建一份**依赖图**，否则 Agent 改一处坏 13 处
- ⚠️ 现有 13 个模块的 pom 各自定义 Spring Boot 版本/依赖版本（已有 commit `fca3871 build: scope runtime dependencies to services` 在修这个） — Phase 2 必须做完
- ✅ 前端零代码，可以从零起步

**踩坑提示**

- ❌ 用 `npm create vite@latest` 一路回车生成脚手架 — 默认配置和 AntD/React Router/Zustand 集成有不少坑
- ✅ 用 Phase 2 写好的自定义模板（见 FRONTEND_BUILD_PLAN.md），每个新组件/页面/服务都从模板 fork

---


## Phase 3：后端实现（4-8 周，可与前端并行）

**目标**：实现业务逻辑，每服务都有可运行的代码 + 单元测试。

| Agent 角色                      | 输入             | 输出                                                                             |
| ----------------------------- | -------------- | ------------------------------------------------------------------------------ |
| **Service-Designer Agent**    | 用户故事 + OpenAPI | 1 个新服务（entity + repository + service + controller + Flyway 迁移 + 单元测试 + 集成测试）   |
| **Empty-Module-Filler Agent** | 5 个空壳模块 + 业务边界 | 把 his-cdss / his-drg / his-platform / his-emergency / his-collaboration 填到 MVP |
| **Test Agent**                | Service 代码     | 补充单元测试 + ArchUnit 测试 + Testcontainers 集成测试                                     |
| **Reviewer Agent**            | PR diff        | 代码审查 + 安全审查 + 性能审查                                                             |

**smart-his-source 的空壳模块填法**（按优先级）

| 模块                  | 当前文件数 | 落地路径                              | 关键依赖                   |
| ------------------- | ----: | --------------------------------- | ---------------------- |
| `his-platform`      |     2 | EMPI 算法 + 主数据管理 + ICD-10/ATC 术语字典 | 需引入 HAPI-FHIR          |
| `his-cdss`          |     2 | Drools 规则引擎（处方审查、剂量、过敏、相互作用）      | 需引入 Drools + 规则库       |
| `his-drg`           |     2 | CHS-DRG 分组器（1.1 版 ADRG → DRG）     | 需引入分组规则包               |
| `his-emergency`     |    10 | 预检分诊 + 抢救记录 + 绿色通道 + 留观           | 复用 patient/clinical 接口 |
| `his-collaboration` |    10 | 会诊申请 + MDT + 转诊 + 照护计划            | 复用 patient 接口          |

**关键产物（每模块）**

- `his-{module}/src/main/java/.../{module}/` — domain/application/infrastructure 四层包结构
- `his-{module}/src/main/resources/db/migration/V{N}__{module}_init.sql` — Flyway 迁移
- `his-{module}/src/test/java/.../{module}/` — 单元测试（覆盖率 ≥ 60%）
- `his-{module}/src/test/java/.../architecture/{module}/` — ArchUnit 规则

**踩坑提示**

- ❌ 一次性把所有 Service 写完再测 — 中间任何一处崩了都不知道从哪开始查
- ✅ **垂直切片（Vertical Slice）**：按"用例"为单位开发，每个用例 = 1 个 user story → 1 个端到端测试 → 1 组代码 + 迁移 + 测试
- ❌ 空壳模块一次性填满 — 复杂度不可控
- ✅ 第一个 PR 只做"happy path"，后续 PR 补边界条件

---


## Phase 4：前端实现（4-8 周，与 Phase 3 并行）

**目标**：用 Vite + React18 + AntD5 把后端 API 用起来，每个页面都能"点开看到数据"。

| Agent 角色                    | 输入          | 输出                                                     |
| --------------------------- | ----------- | ------------------------------------------------------ |
| **Page-Scaffold Agent**     | 路由清单 + 权限矩阵 | 1 个新页面（路由 + 组件骨架 + AntD 表单/表格 + API 对接 + RBAC 守卫）      |
| **Component-Library Agent** | 设计规范        | 共享组件库（Header / Sidebar / DataTable / Form / AuthGuard） |
| **State-Manager Agent**     | 复杂状态场景      | Zustand store（用户信息 / 字典 / 全局设置）+ React Query 配置        |
| **E2E-Test Agent**          | 用户故事        | Playwright 测试（每个关键流程 1 个 spec）                         |

**关键产物**

- `src/pages/` — 按业务域分目录（patient/clinical/resource/pharma/operations/…）
- `src/components/` — 共享组件 + Storybook 文档
- `src/api/` — 从 OpenAPI 自动生成的客户端 + 自定义 hooks
- `src/store/` — Zustand stores
- `tests/e2e/` — Playwright specs
- `vite.config.ts` — dev proxy（`/api → http://localhost:8080`）

**smart-his-source 的页面优先级**（按业务价值）

| 优先级 | 页面                 | 后端依赖           |
| --- | ------------------ | -------------- |
| P0  | 登录 + 工作台 Dashboard | his-auth       |
| P0  | 患者列表 / 患者详情 / 建档   | his-patient    |
| P0  | 挂号 / 预约            | his-patient    |
| P1  | 医嘱开立 / 电子病历        | his-clinical   |
| P1  | 处方开立 / 配药 / 发药     | his-pharma     |
| P2  | 床位图 / 库存           | his-resource   |
| P2  | 账单 / 结算            | his-operations |
| P3  | CDSS 告警面板          | his-cdss（空壳）   |
| P3  | DRG 入组分析           | his-drg（空壳）    |

**踩坑提示**

- ❌ 先写完所有页面再对接 API — 联调阶段会爆
- ✅ 每个页面 = 1 个 PR，从 OpenAPI 生成 hooks → 渲染 mock 数据 → 切真实 API 三步走
- ❌ AntD 5 直接按需引入 + Vite — 配置容易踩坑
- ✅ 用 `vite-plugin-style-import` 或 AntD 官方推荐的 CSS-in-JS 模式

---

## Phase 5：集成与联调（2 周）

**目标**：前后端在真实环境跑通，性能、安全、可观测性达标。

| Agent 角色                | 输入     | 输出                                              |
| ----------------------- | ------ | ----------------------------------------------- |
| **Integration Agent**   | 前后端 PR | 端到端测试 + Pact 契约测试                               |
| **Performance Agent**   | 集成环境   | JMeter / k6 压测报告 + 优化建议                         |
| **Security Agent**      | 全代码    | SAST 扫描（Snyk / Trivy）+ DAST 扫描（OWASP ZAP）+ 密钥扫描 |
| **Observability Agent** | 服务清单   | Grafana 看板 + Prometheus 告警 + Loki 日志聚合          |

**关键产物**

- `tests/integration/` — Pact 契约测试（前端 mock 服务端验证）
- `tests/e2e/` — Playwright（关键用户路径 ≥ 10 个 spec）
- `docs/04-deployment/runbook.md` — 运维手册（含告警阈值、故障处理）
- `dashboards/grafana/` — 4 个看板（业务概览 / 服务健康 / 数据库 / 中间件）

**smart-his-source 集成要点**

- 🔴 **前端 ↔ his-gateway ↔ 后端服务** 链路长 — 网关需要稳定，Sentinel 流控规则必须在 Phase 5 前定稿
- 🔴 **信创数据库兼容** — PG 写的 SQL 必须能在达梦/金仓跑（his-common 里已有 `SqlPortabilityLintTest`，Phase 5 把它升级为 CI 门禁）
- 🟡 **JWT 鉴权跨服务** — his-gateway 统一签发，但 his-* 服务需要共享密钥轮换机制

**踩坑提示**

- ❌ 只在开发环境测 — 信创数据库可能在生产环境才暴露问题
- ✅ Phase 5 开始就部署一份预生产环境（独立 PG 实例 + 独立 K8s namespace），用真实数据脱敏后跑

---


## Phase 6：DevOps 与交付（2 周）

**目标**：把"能跑"变成"可发布、可回滚、可观测"。

| Agent 角色                   | 输入                   | 输出                                                         |
| -------------------------- | -------------------- | ---------------------------------------------------------- |
| **Containerization Agent** | 服务清单 + Dockerfile 模板 | 13 个后端镜像 + 1 个前端镜像 + docker-compose.dev.yml + helm charts/ |
| **CI/CD Agent**            | 测试报告 + Dockerfile    | 4 条流水线（PR → main → tag → release）+ 自动发版                    |
| **Deploy Agent**           | Helm chart           | staging 自动部署 + production 手动审批                             |
| **Release Agent**          | tag + changelog      | 自动生成 CHANGELOG + GitHub Release                            |

**关键产物**

- `Dockerfile` × 14（13 后端 + 1 前端）+ `docker-compose.yml`（dev 一键起）
- `helm-charts/smart-his/` — K8s Helm 包（Deployment + Service + Ingress + HPA + ConfigMap + Secret）
- `.github/workflows/` — CI/CD 流水线
- `docs/04-deployment/architecture.md` — 生产架构图 + 容量规划

**smart-his-source 的容器化要点**

- 后端镜像推荐 **JLink 自定义 JRE**（Java 21 模块化裁剪后基础镜像 < 200MB）
- 前端镜像用 **nginx:alpine** + 多阶段构建（builder 用 node:22，runtime 用 nginx）
- 信创场景需要 **docker manifest list**（amd64 + arm64）

**踩坑提示**

- ❌ 一个超大 Dockerfile 包所有服务 — 镜像大、构建慢、出问题难定位
- ✅ **每个服务独立 Dockerfile + CI 并行构建**
- ❌ K8s yaml 全手写 — 漂移难管
- ✅ **Helm 模板 + values.yaml 分离环境差异**

---

## Phase 7：交付与文档（1 周）

**目标**：让一个新人在 1 周内能接手维护。

| Agent 角色           | 输入        | 输出                        |
| ------------------ | --------- | ------------------------- |
| **Docs Agent**     | 全代码 + 全决策 | 5 类文档（架构/开发/部署/API/用户）    |
| **Demo Agent**     | 完整功能      | Docker Compose 一键起 + 录屏演示 |
| **Handover Agent** | 全部产物      | 维护清单 + 故障手册 + 升级路径        |

**关键产物**

- `README.md` — 项目入口（5 分钟跑通）
- `docs/01-requirements/` — 需求文档
- `docs/02-domain/` — 领域模型
- `docs/03-architecture/` — 架构 + ADR
- `docs/04-deployment/` — 部署手册 + Runbook
- `docs/05-api/` — 自动生成的 OpenAPI 文档（Swagger UI）
- `docs/06-development/` — 开发指南（环境搭建 / 测试 / 调试）
- `CHANGELOG.md` — 自动生成

**smart-his-source 当前文档状态**

- ⚠️ 已有 `README.md`、`IMPROVEMENT_PLAN.md`、`FRONTEND_BUILD_PLAN.md`、`PROJECT_COMPLETION_PLAN.md` — 但分散
- ❌ 缺架构图、API 文档、部署手册 — Phase 7 必须补

**踩坑提示**

- ❌ 让 Agent 自己写文档 — 会得到"翻译代码"而不是"讲清楚为什么"
- ✅ 文档 = **决策 + 流程**，不是代码的复述

---

## 总结：smart-his-source 全栈化路线（合计 12-18 周）

| Phase    | 时间      | 关键里程碑                           | Agent 角色                                                 |
| -------- | ------- | ------------------------------- | -------------------------------------------------------- |
| 0 需求建模   | W1-W2   | 用户故事 30+ 条 / 领域模型定稿             | Product + Domain + Data                                  |
| 1 架构选型   | W3      | C4 图 + 8 个 ADR + OpenAPI v0.1   | Architect + Tech-Selection + Contract                    |
| 2 脚手架    | W4      | 后端模板 + 前端脚手架 + CI 4 条           | Backend-Scaffold + Frontend-Scaffold + Contract-Gen      |
| 3 后端实现   | W5-W10  | 13 模块完成 + 5 空壳填 MVP             | Service-Designer + Empty-Module-Filler + Test + Reviewer |
| 4 前端实现   | W5-W10  | 工作台 + 8 个核心页面 + RBAC            | Page-Scaffold + Component-Library + State + E2E          |
| 5 集成联调   | W11-W12 | E2E 10+ spec + 压测通过 + SAST 0 高危 | Integration + Performance + Security + Observability     |
| 6 DevOps | W13-W14 | 14 个镜像 + Helm chart + CI/CD     | Containerization + CI/CD + Deploy + Release              |
| 7 交付文档   | W15-W16 | 5 类文档齐 + Demo                   | Docs + Demo + Handover                                   |

---

## 一句话总结

> **Agent 时代做全栈 = "AI 干 80% 的活，人盯 20% 的决策"**。  
> 决策点（架构选型、ADR、空壳边界、合规、安全、UI 定稿）必须人来拍；  
> 实现点（脚手架、CRUD、测试、文档、Demo）Agent 干得又快又好。  
> 关键是要有 `AGENTS.md` 这份"宪法"+ **阶段交付物可验证** + **接口契约先于实现** 三件套。
