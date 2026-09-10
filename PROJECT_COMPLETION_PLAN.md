# Smart HIS 项目完成计划

> 仓库：`smart-his-source`
> 更新基线：2026-09-10
> 范围：开发、测试、集成、安全、部署和交付

## 一、当前结论

项目已经具备 14 个 Maven reactor 工程、12 个可执行服务、六个核心业务域代码、基础 CI 与容器清单，并完成独立 React 前端的登录、工作台、患者建档和门诊挂号。当前可以继续模块开发和局部演示，尚未形成可以验收或上线的完整 HIS。

| 维度 | 当前估算 | 完成情况 |
| --- | ---: | --- |
| 后端代码骨架与接口 | 60% | 患者、临床、资源、运营、药事和认证代码较充足 |
| 前端业务功能 | 40% | 登录、工作台、患者建档和门诊挂号可用 |
| 自动化测试与联调 | 30% | 前端 13 项测试通过；后端仅 4 个测试类 |
| 生产交付能力 | 20% | CI 和部署清单已有，安全、监控和发布闭环缺失 |
| 综合可交付程度 | 35% | 适合继续开发，不满足完整验收和生产发布条件 |

完成度只用于排定开发顺序。最终完成以可重复构建、自动测试、完整业务流程和可回滚发布为准。

## 二、工程基线

### 2.1 已完成

- Git 仓库已连接 `https://github.com/monkeykiller-art/smart-his-source.git`，使用 `main` 分支。
- Java 21、本地 Maven 3.9.16、Maven Wrapper 和工具链验证脚本已经建立。
- Maven Enforcer 已检查 Java/Maven 版本、依赖收敛、重复类和禁止依赖。
- 达梦和金仓驱动已取消 `systemPath`，`his-patient` 重复 Flyway 版本已修复。
- GitHub Actions 已包含 Linux、Windows 构建验证与依赖审查。
- `his-auth` 已覆盖登录失败锁定、Token 过期、刷新和撤销等分支。
- 前端采用 React 18、TypeScript、Vite、Ant Design、Zustand 和 TanStack Query。
- 前端已完成登录、会话保存、Token 刷新、路由守卫、工作台、患者建档、敏感信息遮罩、排班查询、挂号和取消挂号。
- 前端 lint、13 项 Vitest 测试和生产构建通过，最近一次 npm audit 未发现漏洞。

### 2.2 当前阻塞和缺口

- Windows 全量 `clean verify` 在 `his-common` 编译时无法关闭 Maven 缓存中的 `reactive-streams-1.0.4.jar`，后端构建基线仍为红色。
- 625 个主 Java 类只有 4 个后端测试类；18 个 Flyway 脚本没有真实 PostgreSQL 空库和升级测试。
- 登录到发药的主业务流程没有端到端自动验证。
- 临床、资源、运营和药事前端仍是占位页面。
- 动态 RBAC 菜单、按钮权限、403 页面和 Playwright 测试尚未建立。
- 网关 CORS、Actuator、Swagger、JWT 声明和生产日志配置仍需收紧。
- `his-platform`、`his-cdss`、`his-emergency`、`his-collaboration` 和 `his-drg` 仍以占位实现为主。
- OpenAPI、Kafka 事件版本、Outbox/Inbox、OpenTelemetry、容量测试和发布演练尚未闭环。
- 前端存在一个超过 500 KB 的公共依赖分块提示。

## 三、统一完成定义

每项任务只有同时满足以下条件才算完成：

1. 代码、配置、迁移和文档已写入 Git，每项改动有独立 commit。
2. 改动包含相应单元、集成、契约或端到端测试。
3. 受影响模块测试、全量验证和静态检查全部通过。
4. 接口变更同步更新调用方、OpenAPI 和兼容性检查。
5. 数据库变更只增加新的 Flyway 迁移，不修改已发布迁移。
6. 核心写操作具有权限、校验、幂等、审计和故障处理。
7. 用户可见功能具备加载、空数据、失败和无权限状态。
8. 阶段门禁失败时先修复基线，再继续后续功能。

## 四、实施阶段

### 阶段 R0：修复构建与环境基线（第 1 周）

**目标：** 让后端在本地和 CI 中稳定、可重复地完成全量验证。

任务：

1. 定位 Windows `javac` 无法关闭 Maven JAR 的问题，检查文件锁、缓存损坏和 JDK 差异。
2. 使用全新的工作区 Maven 缓存执行 `clean verify`。
3. 在 Linux CI 和 Windows CI 运行全 reactor 构建并保存测试报告。
4. 验证 12 个服务的 Dockerfile 可以构建。
5. 将前端 lint、Vitest 和 Vite build 加入 GitHub Actions。
6. 开启依赖漏洞和 secret scanning，高危问题阻止合并。

验收标准：

- Windows 和 Linux 使用 Java 21 执行 `clean verify` 均一次通过。
- CI 连续 10 次无偶发失败，构建报告可以下载。
- 前端 lint、13 项以上测试和生产构建进入每次 PR。
- Git 工作区干净，每个修复都有对应 commit。

建议提交：

- `fix(build): stabilize Windows Java compilation`
- `ci: verify backend frontend and dependency security`
- `build: verify all service container images`

### 阶段 R1：数据库和核心后端测试（第 2—4 周）

**目标：** 保护医疗、库存和费用领域中错误成本最高的规则。

任务：

1. 用 Testcontainers PostgreSQL 为 12 个 schema 增加空库 Flyway 迁移测试。
2. 保存上一发布版本结构基线，增加升级到最新版本的测试。
3. `his-patient`：覆盖患者去重、号源并发、挂号取消、入出院转换和 FHIR 映射。
4. `his-clinical`：覆盖主次诊断、医嘱状态机和病历签名后禁止修改。
5. `his-resource`：覆盖库存并发扣减、重复出入库、床位占用和乐观锁。
6. `his-operations`：覆盖金额精度、押金、重复支付、退费上限和结算对账。
7. `his-pharma`：覆盖剂量、过敏、禁忌、相互作用和处方审核状态。
8. `his-auth`：增加角色越权、登出黑名单、JWT 声明和 Redis 集成测试。

验收标准：

- 18 个 Flyway 脚本均通过 PostgreSQL 空库迁移和升级测试。
- 六个核心业务服务均有领域单元测试和数据库集成测试。
- 核心领域行覆盖率达到 50%，关键规则同时检查分支覆盖率。
- 测试数据可以重复创建和清理，不依赖共享数据库。

建议提交：

- `test(database): verify all Flyway migrations on PostgreSQL`
- `test(patient): cover registration and admission rules`
- `test(clinical): cover order and record state transitions`
- `test(resource): cover concurrent inventory and bed allocation`
- `test(operations): cover payment settlement and refund rules`
- `test(pharma): cover prescription safety and review rules`

### 阶段 R2：完成门诊主流程和前端（第 5—8 周）

**目标：** 交付一条可以由用户操作并由自动测试验收的门诊主流程。

```text
用户登录 → 患者建档 → 选择排班并挂号 → 接诊与诊断
→ 开立处方 → 药师审核 → 收费结算 → 药房发药
```

任务：

1. 完善患者编辑、重复建档提示和档案合并入口。
2. 完善挂号支付、退号原因、号源并发冲突和操作记录。
3. 实现接诊队列、诊断、病历编辑与签名、医嘱和处方页面。
4. 实现处方审核、拒绝原因和用药安全告警展示。
5. 实现费用清单、支付、退费、结算和票据状态页面。
6. 实现药房待发药队列、核对和发药页面。
7. 使用 REST Assured 或 Newman 验证后端主流程。
8. 使用 Playwright 验证正常流程、重复提交、权限不足和下游失败。
9. 通过路由懒加载和依赖分块消除大于 500 KB 的公共 chunk。

验收标准：

- 新环境可以按文档启动基础设施、后端和前端并完成主流程。
- 正常流程和四类关键失败路径都有自动测试。
- 业务状态在患者、临床、运营和药事服务之间保持一致。
- 前端 lint、类型检查、单元测试、Playwright 和生产构建全部通过。
- 首屏 gzip 制品小于 1 MB，主要页面在目标环境 2 秒内可交互。

建议提交：

- `feat(frontend): add clinical encounter workflow`
- `feat(frontend): add prescription review workflow`
- `feat(frontend): add billing and refund workflow`
- `feat(frontend): add pharmacy dispensing workflow`
- `test(e2e): cover outpatient journey from login to dispensing`
- `perf(frontend): split route and vendor bundles`

### 阶段 R3：权限、安全、配置和契约（第 9—10 周）

**目标：** 建立角色边界、服务独立发布能力和默认安全的生产配置。

任务：

1. 实现后端权限矩阵、前端动态菜单、按钮权限和 403 页面。
2. 将 CORS 来源、方法和请求头改为显式环境配置。
3. 生产环境仅公开 liveness/readiness，限制 Swagger 和其他 Actuator 端点。
4. JWT 增加 issuer、audience 和 kid，实现双密钥轮换。
5. 区分用户令牌和服务身份，不信任未经签名的用户头。
6. 为患者读取、病历签名、处方审核、退费和权限修改记录审计事件。
7. 导出 OpenAPI JSON，统一分页、金额、时间和错误响应并检测破坏性变更。
8. 为 Kafka 事件定义版本化信封、schema 和兼容性检查。
9. 将关键配置改为带校验的 `@ConfigurationProperties`。

验收标准：

- 医生、护士、药师、收费员和管理员只能访问授权菜单与接口。
- 越权、Token 伪造、过期、错误 audience 和旧密钥场景均有测试。
- 日志不会明文输出 Token、身份证、手机号、病历正文和数据库密码。
- OpenAPI 或事件 schema 出现破坏性变化时 CI 自动失败。
- 生产安全配置缺失时服务拒绝启动。

建议提交：

- `feat(security): enforce role permissions across gateway and services`
- `feat(auth): validate JWT issuer audience and key id`
- `test(contract): detect breaking API and event changes`
- `fix(config): secure production endpoints and logging defaults`

### 阶段 R4：服务边界和扩展模块（第 11—15 周）

**目标：** 降低公共模块耦合，让五个占位模块形成生产级纵切片。

先完成公共治理：生成 Maven 依赖图、schema 所有权和 API 清单；用 ArchUnit 固化分层；逐步拆分 `common-core`、`common-web`、`common-security`、`common-observability` 和 `fhir-support`；禁止跨服务共享 Entity、Mapper 或数据库表；Kafka 写入使用 Outbox，消费使用 Inbox 或 `eventId` 幂等记录。

| 顺序 | 模块 | 第一条纵切片 | 核心验证 |
| ---: | --- | --- | --- |
| 1 | `his-platform` | 术语导入、发布和编码查询 | 版本不可变、FHIR 校验、审计 |
| 2 | `his-cdss` | 药物过敏决策 | 规则版本、优先级和拦截结果 |
| 3 | `his-emergency` | 分诊到抢救 | 非法转换、重复命令、并发冲突 |
| 4 | `his-collaboration` | 会诊申请到完成 | 权限、超时、撤销和领域事件 |
| 5 | `his-drg` | 病案分组和结果保存 | 规则版本、输入快照和可复算 |

验收标准：

- 五个扩展模块各有迁移、领域规则、API、权限、审计、指标和测试。
- 服务之间不存在 Entity、Mapper 和数据库表的实现级共享。
- 重复、乱序 Kafka 消息不会重复产生业务副作用。
- 跨服务流程失败后可以自动重试、补偿或人工恢复。

建议提交：

- `refactor(common): establish enforceable module boundaries`
- `feat(platform): deliver terminology lifecycle slice`
- `feat(cdss): deliver allergy decision slice`
- `feat(emergency): deliver triage state workflow`
- `feat(collaboration): deliver consultation workflow`
- `feat(drg): deliver versioned grouping workflow`

### 阶段 R5：可观测性、性能和发布（第 16—18 周）

**目标：** 形成可诊断、可度量、可追溯并可回滚的生产交付链路。

任务：

1. 接入 Micrometer Tracing 和 OpenTelemetry，传播 HTTP、Feign 和 Kafka trace。
2. 使用结构化 JSON 日志，统一 traceId、spanId、脱敏用户标识和业务流水号。
3. 建立 RED 指标及挂号、处方审核、库存扣减和结算差异指标。
4. 为关键告警编写 Runbook 并演练 PostgreSQL、Redis、Kafka 和下游故障。
5. 使用 k6 或 Gatling 建立固定数据集、容量模型和性能回归门禁。
6. 构建带 Git SHA 的镜像，生成 SBOM，执行漏洞扫描与签名。
7. 使用 Helm 或 Kustomize 管理环境差异，完成滚动或金丝雀发布。
8. 演练数据库备份恢复、应用回滚和迁移前向修复。

验收标准：

- 核心查询 P95 小于 500ms，核心写入 P95 小于 1s。
- 任一主流程请求可以从网关追踪到数据库和 Kafka 消费者。
- 发布制品可以追溯到 Git SHA、依赖清单、测试报告和扫描结果。
- 自动部署、烟雾测试、告警、回滚和恢复演练全部通过。
- 候选版本连续运行 72 小时无阻断级故障后进入生产审批。

建议提交：

- `feat(observability): trace HTTP Feign and Kafka workflows`
- `test(performance): add core API capacity baselines`
- `build(release): produce signed traceable service images`
- `ops: add canary rollback and recovery runbooks`

## 五、里程碑和工期

| 里程碑 | 目标时间 | 可交付结果 |
| --- | --- | --- |
| M1 构建全绿 | 第 1 周末 | Windows/Linux 全量构建、前后端 CI、安全扫描 |
| M2 核心可信 | 第 4 周末 | 全 schema 迁移测试、六个核心模块规则测试 |
| M3 门诊闭环 | 第 8 周末 | 登录到发药的前后端流程与端到端测试 |
| M4 安全可联调 | 第 10 周末 | RBAC、JWT、审计、OpenAPI 和事件契约 |
| M5 功能完整 | 第 15 周末 | 五个扩展模块各完成一条生产级纵切片 |
| M6 候选发布 | 第 18 周末 | 可观测、性能、制品、部署和恢复闭环 |

建议团队至少配置 2 名后端、1 名前端和 1 名测试开发。单人开发时保持任务顺序，将总工期调整为 28—36 周。

## 六、下一批任务

1. 修复 Windows 后端全量编译的 JAR 文件句柄问题。
2. 将前端 lint、Vitest 和生产构建接入 GitHub Actions。
3. 添加全部 schema 的 Testcontainers PostgreSQL 空库迁移测试。
4. 覆盖患者去重、号源并发和挂号状态测试。
5. 实现医生接诊、诊断和病历签名页面及接口联调。
6. 实现处方、审核、收费和发药，贯通首条门诊流程。

每完成一步按以下格式汇报：

```text
任务：
完成内容：
测试与验证：
Commit：
剩余风险：
下一步：
```

## 七、项目最终完成标准

- 核心与扩展模块均有真实数据库、权限、审计和自动测试验证。
- 登录、建档、挂号、接诊、诊断、处方、审核、收费和发药可以完整操作。
- 主流程和关键失败路径在 CI 中自动通过。
- 前后端构建可在干净 Windows 和 Linux 环境重复执行。
- API 和事件契约可版本化发布且破坏性变化会被阻止。
- 生产配置默认安全，缺少必需密钥时启动失败。
- 系统故障可以通过日志、指标和链路追踪定位。
- 发布制品可以追溯、签名、部署、回滚和恢复。
