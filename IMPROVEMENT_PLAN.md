# Smart HIS 开发改进方案

> 适用仓库：`smart-his-source`
>
> 视角：软件开发、测试、交付与运行质量
>
> 基线日期：2026-09-08

## 一、范围与目标

本方案只处理开发问题，不讨论产品定位、市场规划和运营推广。目标是在保留 Java 21、Spring Boot 3、Spring Cloud、PostgreSQL、Redis、Kafka、Nacos 和 Kubernetes 技术路线的基础上，使项目达到以下工程标准：

1. 任意开发者可以从干净环境复现构建、测试和本地启动。
2. 每个服务具备清晰的代码边界、数据所有权和版本化接口。
3. 数据库迁移、消息事件和跨服务调用可以自动验证。
4. 核心流程有单元、集成、契约和端到端测试保护。
5. 每次提交都经过统一的 CI、安全和质量门禁。
6. 生产问题可以通过日志、指标和链路追踪快速定位。

本轮不建议更换主框架，也不建议在工程基线建立前继续拆分微服务或引入新的基础设施。

## 二、代码库现状与开发风险

### 2.1 已有基础

- 13 个 Maven 模块，约 627 个 Java 文件。
- Java 21、Spring Boot 3.2.4、Spring Cloud 2023.0.1。
- PostgreSQL 16、Redis 7、Kafka 3.6、Nacos 2.3.2。
- MyBatis-Plus、Flyway、HAPI FHIR R4、SpringDoc OpenAPI。
- Docker Compose、本地启动脚本和 Kubernetes 清单。
- Actuator、Micrometer 和 Prometheus 依赖已经接入。

这些组件足以支撑当前开发，不需要先进行技术栈迁移。接下来应把“组件已经存在”转化为“构建可重复、行为有测试、故障可诊断”。

### 2.2 必须优先处理的问题

| 优先级 | 问题 | 代码库证据 | 开发影响 |
| --- | --- | --- | --- |
| P0 | 全量构建尚未形成稳定基线 | 当前环境下 Maven 全量测试未能一次通过 | 无法确认任意提交是否可交付 |
| P0 | Flyway 迁移版本冲突 | `his-patient` 同时存在两个 `V2` 脚本 | 空库初始化或升级可能失败 |
| P0 | 自动测试数量过少 | 约 627 个 Java 文件，仅发现 2 个真正的测试类 | 重构和跨模块修改缺少保护 |
| P0 | 缺少持续集成 | 仓库没有 `.github/workflows` | 构建、测试和扫描依赖人工执行 |
| P1 | 数据库驱动使用 `systemPath` | 根 `pom.xml` 直接引用 `lib` 下的达梦、金仓 JAR | 依赖不可传递，构建难以复现 |
| P1 | 父 POM 注入较多运行依赖 | Nacos、Actuator、Prometheus 等被所有子模块继承 | 模块依赖膨胀，职责不清晰 |
| P1 | `his-common` 承担多类技术能力 | Web、FHIR、MyBatis、Feign、Kafka、Redis、AOP 等集中在一个模块 | 任一公共变更可能影响全部服务 |
| P1 | 网关开发配置过宽 | CORS 通配、Actuator 整体白名单、DEBUG 日志 | 测试配置容易误用于生产 |
| P1 | 配置和 POM 高度重复 | 多个业务服务的依赖与 YAML 结构基本一致 | 升级容易遗漏，配置漂移难发现 |
| P2 | 服务契约和事件契约未集中管理 | API 主要依赖实现代码和 SpringDoc 动态生成 | 跨服务独立发布存在兼容风险 |
| P2 | 可观测性只有基础接入 | 指标依赖存在，但缺少统一追踪和业务指标规范 | 分布式问题定位成本高 |

### 2.3 空壳模块的开发处理原则

`his-cdss`、`his-drg`、`his-platform`、`his-emergency` 和 `his-collaboration` 当前实现较少。不要按“先增加 Controller、Service、Mapper”批量填充，而应逐个模块完成以下最小工程闭环：

1. 编写领域边界和数据所有权说明。
2. 定义 OpenAPI 或事件契约，并添加兼容性测试。
3. 创建 Flyway 迁移和 Testcontainers 迁移测试。
4. 实现一条最小业务纵切片，从接口到持久化完整贯通。
5. 增加单元测试、集成测试和异常路径测试。
6. 接入认证、审计、指标、日志和健康检查。
7. 通过 CI 后再扩展第二条用例。

## 三、目标工程结构

### 3.1 Maven 模块职责

建议逐步将当前公共能力拆成以下层次：

```text
smart-his
├─ his-bom                    统一第三方依赖版本
├─ his-common-core            ID、时间、错误码、基础值对象
├─ his-common-web             统一响应、异常处理、Web 过滤器
├─ his-common-security        当前用户、权限上下文、服务间认证
├─ his-common-observability   日志、指标、trace 约定
├─ his-contract-api           OpenAPI 生成代码或共享 DTO 契约
├─ his-contract-event         Kafka 事件 schema 及兼容规则
├─ his-fhir-support           FHIR 映射、校验和术语支持
└─ his-*                      各业务服务
```

拆分时遵守以下规则：

- `common-core` 不得依赖 Spring Web、数据库、Kafka 或 FHIR。
- 服务不得依赖另一个服务的实体、Mapper 或实现类。
- 共享 DTO 只用于稳定契约，服务内部命令和实体保持私有。
- 每个服务只拥有并迁移自己的数据库 schema。
- 依赖方向由业务服务指向公共基础层，不允许反向依赖。

第一阶段无需一次完成全部拆分。先用 Maven Enforcer 和 ArchUnit 固化禁止规则，再按实际改动逐步迁移。

### 3.2 服务内部分层

建议统一采用以下包结构：

```text
com.smarthis.<service>
├─ api              Controller、请求响应对象、OpenAPI 适配
├─ application      用例编排、事务边界、命令和查询
├─ domain           实体、值对象、领域服务、仓储接口
└─ infrastructure   Mapper、数据库、Kafka、Feign 和外部系统适配
```

约束：

- Controller 只做协议转换、参数校验和调用用例。
- 事务放在 application 层，不放在 Controller。
- domain 层不依赖 Spring MVC、MyBatis 和 Feign。
- 外部服务调用通过接口适配，单元测试可替换。
- 数据库 DO、领域对象和 API DTO 分离，避免字段变更扩散。

### 3.3 同步调用与异步事件

同步 HTTP 适用于需要即时结果的查询或校验；Kafka 适用于状态已经提交后的通知和最终一致性流程。

所有同步调用必须定义连接和读取超时、可重试错误、幂等语义、降级行为及 OpenAPI 兼容策略。

所有 Kafka 事件必须包含统一信封：

```json
{
  "eventId": "uuid",
  "eventType": "patient.registered.v1",
  "occurredAt": "2026-09-08T09:00:00Z",
  "producer": "his-patient",
  "correlationId": "trace-or-business-id",
  "aggregateId": "patient-id",
  "schemaVersion": 1,
  "payload": {}
}
```

生产端使用 Outbox 保证数据库提交与事件发布一致；消费端以 `eventId` 实现幂等，并明确重试次数、退避时间和死信队列。

## 四、分阶段开发计划

### 阶段 P0：恢复可复现构建（第 1—2 周）

#### 4.1 固定工具链

- 添加 Maven Wrapper 并提交 Wrapper 配置。
- 使用 Maven Toolchains 或 CI 镜像固定 Java 21 发行版。
- 在 README 中记录唯一受支持的构建命令：`./mvnw clean verify`。
- 禁止使用开发者本机 Maven 缓存中的未声明制品。
- 为 Windows 和 Linux 各执行一次干净构建。

#### 4.2 修复 Maven 构建

- 从空的 Maven 本地仓库运行全量构建，逐个处理编译错误。
- 统一 `maven-compiler-plugin`、`maven-surefire-plugin` 和 `maven-failsafe-plugin` 版本。
- 添加 Maven Enforcer，检查 Java 版本、依赖收敛、重复类和禁止依赖。
- 将父 POM 中的运行时依赖移到真正需要它们的模块。
- 处理达梦和金仓驱动的 `systemPath`，改用受控的内部 Maven 仓库或安装脚本。

#### 4.3 修复数据库迁移

- 重编号 `his-patient` 的两个 `V2` 迁移，确保版本唯一且执行顺序明确。
- 检查所有模块是否存在重复版本、不可重复执行的种子数据和跨 schema 修改。
- 禁止修改已经发布的版本化迁移；后续变更必须新增迁移文件。
- 为每个 schema 建立两类测试：空库迁移到最新版、上一发布版本升级到最新版。
- 在 PostgreSQL 通过后，再对达梦和金仓执行 SQL 兼容测试。

#### 4.4 建立最小 CI

新增 GitHub Actions 流水线：

```text
checkout
→ setup-java 21 + Maven 缓存
→ ./mvnw -B clean verify
→ 上传 Surefire/Failsafe 报告
→ 依赖漏洞扫描
→ 构建镜像但不发布
```

P0 完成标准：

- 干净环境执行 `./mvnw clean verify` 一次通过。
- CI 连续 20 次无偶发失败。
- 所有 Flyway 迁移测试通过。
- 构建不再依赖仓库内 `systemPath` JAR。

### 阶段 P1：建立自动化测试体系（第 2—6 周）

#### 4.5 测试分层

| 测试层 | 工具建议 | 主要验证内容 | 是否进入每次 PR |
| --- | --- | --- | --- |
| 单元测试 | JUnit 5、AssertJ、Mockito | 领域规则、状态转换、计算逻辑 | 是 |
| 架构测试 | ArchUnit | 包依赖、跨模块边界、命名规范 | 是 |
| 数据库集成 | Testcontainers PostgreSQL | Mapper、事务、Flyway、唯一约束 | 是 |
| 消息集成 | Testcontainers Kafka | 序列化、幂等、重试、死信 | 是 |
| API 测试 | MockMvc/WebTestClient | 参数、状态码、权限、错误响应 | 是 |
| 契约测试 | Spring Cloud Contract 或 Pact | Feign 调用和事件兼容性 | 是 |
| 端到端测试 | REST Assured/Newman | 跨服务核心流程 | 合并前或夜间 |
| 性能测试 | k6/Gatling | P95、吞吐量、资源占用 | 发布前 |

#### 4.6 首批测试范围

优先覆盖错误成本高的代码：

1. `his-auth`：登录失败锁定、Token 过期、刷新、角色越权。
2. `his-patient`：患者去重、挂号状态、入出院转换、FHIR 映射。
3. `his-clinical`：诊断、医嘱状态机、病历签名后修改限制。
4. `his-resource`：库存并发扣减、床位占用、重复出入库。
5. `his-operations`：金额精度、押金、退费、重复结算和对账。
6. `his-pharma`：剂量限制、过敏、禁忌、相互作用和告警闭环。

不要为了覆盖率测试简单的 getter 或框架生成代码。覆盖率门槛分两步实施：先保证核心领域行覆盖率达到 50%，稳定后提高到 70%，同时检查分支覆盖率。

#### 4.7 端到端测试数据

- 使用代码或迁移脚本创建最小测试机构、科室、人员、患者和药品数据。
- 每个测试使用唯一业务编号，可重复运行并可清理。
- 禁止依赖共享开发数据库中的历史数据。
- 首条端到端流程覆盖：用户登录→患者建档→挂号→诊断→开方→处方审核→收费→发药。

P1 完成标准：六个核心业务服务均有单元和数据库集成测试，至少一条跨服务流程在 CI 中自动通过，PR 无法在测试失败时合并。

### 阶段 P1：接口、安全和配置治理（第 3—7 周）

#### 4.8 API 契约

- 为所有 Controller 补齐 OpenAPI 摘要、参数、错误码和权限说明。
- 在 CI 生成并保存 OpenAPI JSON，检测破坏性变更。
- URL 采用显式版本，例如 `/api/v1/patients`。
- 分页、排序、日期时间、金额和错误响应使用统一格式。
- 错误响应包含稳定错误码和 `traceId`，不返回内部堆栈。
- Feign 客户端从契约生成或集中封装，不复制请求响应类。

#### 4.9 安全基线

- CORS 来源由环境变量配置，禁止通配来源与凭据组合。
- 生产环境只公开 readiness 和 liveness，保护其他 Actuator 端点。
- Swagger 仅在开发、测试环境开启，或由管理员权限保护。
- JWT 增加 `issuer`、`audience`、`kid` 和密钥轮换策略。
- 服务间调用使用独立身份，不转发无法验证的用户头。
- 日志禁止输出 Token、身份证号、手机号、病历全文和数据库口令。
- 认证、患者读取、病历修改、处方、退费和权限变更写入审计事件。
- CI 加入 secret scanning、SAST、依赖漏洞和容器镜像扫描。

#### 4.10 配置治理

- 将公共配置拆为可复用 Nacos 配置集，服务只保留差异项。
- 所有配置项提供类型安全的 `@ConfigurationProperties` 和校验。
- 明确 `local`、`test`、`staging`、`prod` 配置层级。
- 默认配置必须安全；生产环境缺少密钥时应启动失败。
- 为配置绑定增加测试，避免属性名修改后静默使用默认值。
- 关闭生产环境 DEBUG 日志，统一日志滚动和保留期。

完成标准：OpenAPI 兼容检查、安全扫描和配置测试进入 PR 门禁，网关和服务内部均验证权限。

### 阶段 P2：服务边界和数据一致性（第 6—12 周）

#### 4.11 渐进拆分 `his-common`

1. 生成依赖图，找出每个服务实际使用的公共类。
2. 先抽取无框架依赖的 `common-core`。
3. 再拆分 Web、安全、可观测、FHIR 和契约模块。
4. 每次只迁移一类能力，并运行全量测试。
5. 使用 ArchUnit 或 Maven Enforcer 阻止依赖重新混入。

不要在一个提交中完成所有模块拆分，避免形成难以审查和回滚的大改动。

#### 4.12 数据所有权

- 为每个 schema 生成表清单并标注所属服务。
- 禁止跨 schema 直接查询和外键；通过 API 或事件取得其他领域数据。
- 跨服务报表使用只读投影、CDC 或专用分析存储。
- 数据写操作使用业务幂等键和唯一约束共同防重。
- 金额使用 `BigDecimal` 并统一精度与舍入方式。
- 时间统一保存 UTC 时间点，接口使用 ISO-8601 并明确时区。

#### 4.13 一致性与故障恢复

- 本地强一致操作使用单库事务。
- 跨服务流程采用 Saga 或事件编排，并为每一步定义补偿行为。
- Kafka 生产使用 Outbox，消费使用 Inbox 或去重表。
- 对超时重试设置上限，避免重试风暴。
- 通过故障注入测试验证重复消息、乱序、消费者重启和下游超时。

P2 完成标准：服务之间不存在实体、Mapper 和数据库表的实现级共享；关键事件重复投递不会重复执行副作用；跨服务流程失败后可以恢复或补偿。

### 阶段 P2：空壳模块按纵切片落地（第 8—16 周）

从开发风险和依赖顺序看，建议依次处理：`his-platform`→`his-cdss`→`his-emergency`→`his-collaboration`→`his-drg`。

#### 4.14 `his-platform`

- 第一条纵切片：术语表导入、版本发布、编码查询。
- 定义术语、值集、映射关系的领域模型和迁移脚本。
- 对接 FHIR 术语校验接口，并增加标准示例测试。
- 后续再实现 EMPI；患者合并和拆分必须使用状态机、版本号和审计记录。

#### 4.15 `his-cdss`

- 先定义稳定的 `DecisionRequest` 和 `DecisionResult` 契约。
- 从药事模块迁移一条规则，例如药物过敏检查。
- 规则具有 ID、版本、优先级、生效范围、来源和启停状态。
- 结果支持提示、警告和拦截三级，并记录调用方处理结果。
- 规则引擎选型通过基准测试和可维护性验证决定。

#### 4.16 `his-emergency` 与 `his-collaboration`

- 使用显式状态机实现分诊、抢救、留观、会诊和转诊状态变化。
- 每个命令校验当前状态、操作者权限和乐观锁版本。
- 将关键时间点和状态变化发布为领域事件。
- API 测试覆盖正常流程、非法转换、重复提交和并发修改。

#### 4.17 `his-drg`

- 先定义与分组器无关的输入、输出和版本接口。
- 将第三方分组器封装在适配器中，领域层不依赖供应商 SDK。
- 每次分组保存规则版本、输入快照和结果，确保可复算。
- 建立脱敏黄金数据集，升级分组规则时执行回归对比。

完成标准：每个模块至少有一条生产级纵切片，包含迁移、代码、权限、审计、指标和四层测试，而不是只增加接口数量。

### 阶段 P3：可观测性、性能和发布（第 10—18 周）

#### 4.18 可观测性

- 接入 Micrometer Tracing 和 OpenTelemetry，HTTP、Feign、Kafka 统一传播 trace 上下文。
- 使用结构化 JSON 日志，统一 `traceId`、`spanId`、`userIdHash` 和业务流水号字段。
- 禁止在指标标签中使用患者 ID、订单 ID 等高基数字段。
- 建立 RED 指标：请求速率、错误率、耗时。
- 增加挂号成功率、医嘱积压、处方审核延迟、库存扣减失败和结算差异等业务指标。
- 为每项告警编写 Runbook，说明影响、查询方法和恢复步骤。

#### 4.19 性能与容量

- 为核心 API 建立固定数据量和并发模型。
- 使用 k6 或 Gatling 在 CI 定期执行基准测试。
- 首批目标：核心查询 P95 小于 500ms，写入 P95 小于 1s；最终阈值根据真实容量修订。
- 对患者搜索、库存扣减、费用明细和运营聚合重点检查索引及慢 SQL。
- 为 Kafka 消费者监控积压和处理延迟。
- 性能优化必须附带前后基准，禁止只凭代码直觉调整。

#### 4.20 制品与发布

- 每个服务构建独立、不可变、带 Git SHA 标签的镜像。
- 生成 SBOM 并为镜像签名。
- Kubernetes 清单通过 Helm 或 Kustomize 管理环境差异。
- 部署前执行迁移兼容检查，部署后执行烟雾测试。
- 使用滚动或金丝雀发布，健康检查失败自动停止。
- 建立数据库备份恢复和应用回滚演练；数据库迁移必须提供前向修复策略。

P3 完成标准：任一请求可以从网关追踪到服务、数据库和 Kafka 消费端；发布制品可追溯到源码提交、依赖清单和测试报告。

## 五、CI 流水线设计

### Pull Request 快速流水线

```text
文档和格式检查
→ Maven Enforcer
→ 编译与单元测试
→ ArchUnit
→ PostgreSQL/Kafka 集成测试
→ OpenAPI 与事件契约兼容检查
→ SAST、依赖和 secret 扫描
```

目标时长控制在 15 分钟以内。

### main 分支流水线

```text
PR 全部检查
→ 全量端到端测试
→ 构建全部镜像
→ SBOM 与镜像扫描
→ 部署测试环境
→ 烟雾测试
```

### 发布流水线

```text
选择已验证的 Git SHA
→ 签名制品
→ 数据库兼容检查
→ 金丝雀部署
→ 自动健康与业务探针
→ 扩大流量或回滚
```

分支保护规则应要求 CI 通过、至少一次评审、提交历史可追踪，并禁止直接向 `main` 推送。

## 六、代码质量规则

- Spotless 统一 Java、XML、YAML 和 Markdown 格式。
- Checkstyle 或 PMD 检查可维护性问题。
- SpotBugs 检查常见 Java 缺陷。
- ArchUnit 检查分层和模块边界。
- Maven Enforcer 检查 Java 版本、依赖收敛和禁止依赖。
- JaCoCo 统计覆盖率，核心领域单独设门槛。
- OWASP Dependency-Check 或 OSV Scanner 检查依赖漏洞。
- Trivy 检查容器和 Kubernetes 配置。

规则应先以报告模式运行，清理现有问题后再逐项改为阻断，避免一次引入大量无关格式改动。

## 七、提交拆分建议

每项改动保持独立、可验证、可回滚。建议前十个提交按以下顺序实施：

1. `build: add Maven Wrapper and Java toolchain checks`
2. `fix: restore reproducible reactor build`
3. `fix(patient): resolve duplicate Flyway migration versions`
4. `ci: add Maven verify workflow and test reports`
5. `test(auth): cover login, lockout and token refresh`
6. `test(patient): add PostgreSQL and FHIR integration tests`
7. `security(gateway): restrict CORS and management endpoints`
8. `build: replace system-scoped database drivers`
9. `test: add first outpatient end-to-end workflow`
10. `arch: enforce service and package dependency rules`

每个提交必须包含对应实现、相关测试或验证脚本、执行过的验证结果，以及数据库或接口兼容性说明。

## 八、里程碑与验收

| 里程碑 | 时间 | 可交付结果 | 自动验收 |
| --- | --- | --- | --- |
| M1 构建基线 | 第 2 周 | 工具链、迁移和 CI 稳定 | 干净环境 `./mvnw clean verify` 通过 |
| M2 核心测试 | 第 6 周 | 六个核心服务测试体系 | 单元、集成、契约及首条 E2E 通过 |
| M3 边界治理 | 第 10 周 | 公共模块收敛、契约和事件规范 | ArchUnit 及兼容检查通过 |
| M4 模块纵切片 | 第 16 周 | 五个薄弱模块各有最小闭环 | 每个模块迁移、API、审计和测试通过 |
| M5 可交付 | 第 18 周 | 追踪、镜像、部署和回滚体系 | 测试环境自动发布与回滚演练通过 |

## 九、开发指标

| 指标 | 当前基线 | M1 | M2 | M5 |
| --- | ---: | ---: | ---: | ---: |
| 干净环境全量构建 | 未稳定通过 | 100% | 100% | 100% |
| 真正的测试类 | 2 | 修复现有测试 | 核心服务均覆盖 | 风险驱动持续增长 |
| CI 门禁 | 无 | 构建、测试、扫描 | 增加契约和 E2E | 增加制品及部署验证 |
| 核心领域行覆盖率 | 未建立 | 建立基线 | ≥50% | ≥70% |
| Flyway 空库/升级测试 | 无 | 全部 schema 通过 | 全部 schema 通过 | 全部支持数据库通过 |
| API 破坏性变更检测 | 无 | 设计完成 | 自动阻断 | 自动阻断 |
| 分布式追踪覆盖 | 未建立 | 基础设计 | 核心流程 | 全部服务 |

指标用于识别风险，不应用来鼓励无意义测试、拆分提交或堆积接口数量。

## 十、立即执行清单

- [ ] 用空 Maven 仓库记录 `mvn clean verify` 的全部失败项。
- [ ] 添加 Maven Wrapper 和 Java 21 版本检查。
- [ ] 修复 `his-patient` 重复 Flyway 版本。
- [ ] 建立最小 GitHub Actions 工作流。
- [ ] 修复并稳定现有两个测试类。
- [ ] 为认证和患者模块添加第一批 PostgreSQL 集成测试。
- [ ] 收紧网关 CORS、Actuator 和 Swagger 配置。
- [ ] 生成模块依赖图、数据库 schema 清单和 API 清单。
- [ ] 建立第一条跨服务端到端测试。
- [ ] 以上基线通过后，再开始填充空壳模块。

这份方案的首要判断标准不是新增了多少功能，而是每次代码变化是否能够被自动构建、自动验证、独立发布、快速定位并安全回滚。
