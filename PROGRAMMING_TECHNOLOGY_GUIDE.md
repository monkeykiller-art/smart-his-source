# Smart HIS Agent 协作开发技术指南

> 适用项目：`smart-his-source`  
> 适用对象：使用 AI Agent 参与需求说明、代码审查、测试、运行和交付的项目负责人、开发人员和学习者  
> 技术基线：Java 21、Spring Boot 3、Maven、PostgreSQL、React 18、TypeScript、Vite、Ant Design

## 1. 需要掌握到什么程度

使用 Agent 开发并不要求一个人精通全部技术，但用户必须能描述目标、判断结果、执行验证并控制版本。能力分为三级：

| 级别 | 目标 | 能完成的工作 |
| --- | --- | --- |
| 基础操作 | 会运行和检查 | 进入目录、启动项目、运行测试、查看 Git 状态、识别成功或失败 |
| 项目开发 | 会修改和排错 | 阅读前后端代码、理解接口和数据库、定位测试错误、审查 Agent 改动 |
| 架构交付 | 会设计和治理 | 划分服务、设计数据一致性、制定安全策略、建设 CI/CD 和生产监控 |

项目负责人至少应达到“基础操作”，实际开发人员应达到“项目开发”。负责上线的人还应具备“架构交付”能力。

## 2. 项目技术全景

一次典型请求会经过以下链路：

```text
浏览器中的 React 页面
        ↓ HTTP/JSON + JWT
Spring Cloud Gateway
        ↓ 路由、鉴权、限流
Spring Boot 业务服务
        ↓ MyBatis-Plus / 事务
PostgreSQL、Redis、Kafka
```

在本仓库中：

- `smart-his-frontend/`：React 前端。
- `his-gateway/`：统一 API 入口。
- `his-auth/`：登录、Token 和权限基础。
- `his-patient/`：患者、排班、挂号和住院信息。
- `his-clinical/`：病历、诊断、医嘱和检查申请。
- `his-resource/`、`his-operations/`、`his-pharma/`：资源、运营和药事业务。
- `his-common/`：共享模型、异常处理、安全和通用组件。
- `his-migration-tests/`：所有服务数据库迁移的 PostgreSQL 验证。

## 3. 计算机与命令行基础

### 3.1 Windows 和 PowerShell

需要理解目录、绝对路径、环境变量、进程、端口和退出码。PowerShell 是当前 Windows 开发环境的主要操作入口。

```powershell
# 进入项目
cd "C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source"

# 查看当前目录
Get-Location

# 查看占用端口的进程
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

# 临时设置环境变量
$env:JAVA_HOME="C:\path\to\jdk-21"

# 查看上一条程序的退出码；0 通常表示成功
$LASTEXITCODE
```

必须能区分命令文本和程序输出，不要把 `BUILD SUCCESS`、`PS C:\...>` 等输出重新粘贴成命令。

### 3.2 文件与字符编码

项目文件主要使用 UTF-8。Windows PowerShell 5 对无 BOM 的 UTF-8 脚本支持有限，因此含中文的 `.ps1` 文件需要注意编码。还应理解：

- Windows 路径使用反斜杠，Git 和 Java 配置中有时使用正斜杠。
- `CRLF` 与 `LF` 是不同的换行格式。
- 密码、Token、私钥和生产配置不能写入仓库。

## 4. Git 版本管理

Git 是用户管理 Agent 改动的核心工具，必须优先掌握。

### 4.1 必会概念

- 工作区：当前文件内容。
- 暂存区：准备放进下一次提交的改动。
- Commit：一组可追踪、可比较、可回滚的改动。
- Branch：相互隔离的开发线路。
- Remote：GitHub 等远程仓库。
- Merge conflict：不同改动修改了同一位置，需要人工决定保留内容。

### 4.2 必会命令

```powershell
git status
git diff
git diff --staged
git add path/to/file
git commit -m "feat(clinical): add encounter workflow"
git log --oneline -10
git pull --rebase
git push
```

提交前应确认：

1. `git diff` 中只有本任务内容。
2. 没有 `.env`、密码、临时日志、构建目录和个人文件。
3. 相关测试已经通过。
4. Commit 信息说明“改了什么”，不要只写“修改”或“更新”。

本项目要求每个完成的改动都有独立 Commit，方便定位问题和回滚。

## 5. Java 21 编程基础

后端使用 Java 21。需要掌握：

- 类、接口、继承、封装和多态。
- 基本类型、字符串、枚举和日期时间。
- `List`、`Set`、`Map` 等集合。
- 泛型、Lambda、Stream 和 `Optional`。
- 异常分类、捕获、抛出和全局处理。
- 注解及其用途。
- 不可变对象、线程安全和并发基础。
- JVM、JDK、编译产物和运行时的区别。

项目中的常见代码关系是：

```text
Controller → Service → Mapper → PostgreSQL
       ↓          ↓
      DTO       Entity
```

- Controller 接收 HTTP 请求。
- DTO 定义输入和输出的数据结构。
- Service 实现业务规则和事务。
- Mapper 访问数据库。
- Entity 映射数据库表。

应能阅读以下类型的代码：

```java
@PostMapping
public ApiResponse<MedicalRecordVo> create(
        @Valid @RequestBody MedicalRecordCreateRequest request) {
    return ApiResponse.ok(medicalRecordService.create(request));
}
```

这里包含 POST 路由、JSON 请求体、参数校验、服务调用和统一响应。

## 6. Maven 与多模块工程

Maven 负责依赖、编译、测试和打包。根目录的 `pom.xml` 聚合多个模块，每个 `his-*` 目录有自己的 `pom.xml`。

### 6.1 生命周期

```powershell
.\mvnw.cmd test          # 编译并运行单元测试
.\mvnw.cmd package       # 测试后生成 JAR
.\mvnw.cmd clean verify  # 清理旧产物并完成全部验证
```

- `clean` 删除旧的 `target/`，避免历史产物干扰。
- `test` 运行单元测试。
- `verify` 运行完整验证和集成测试。
- `-pl his-clinical` 只选择指定模块。
- `-am` 同时构建该模块依赖的其他模块。

### 6.2 依赖管理

需要理解 Maven 坐标、版本管理、依赖范围和传递依赖：

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

遇到依赖冲突时，会使用 `dependency:tree`、Maven Enforcer 和 exclusion 进行分析，不能随意删除规则来让构建“变绿”。

## 7. Spring Boot 后端开发

### 7.1 Spring 核心

需要理解依赖注入、Bean 生命周期、配置绑定和环境 Profile。常见注解包括：

- `@SpringBootApplication`：应用入口。
- `@RestController`：REST 控制器。
- `@Service`：业务服务。
- `@Configuration`：配置类。
- `@Transactional`：事务边界。
- `@Valid`、`@NotNull`：请求校验。

### 7.2 Spring MVC 与 WebFlux

普通业务服务主要使用 Spring MVC；网关使用响应式 WebFlux。需要知道两套模型不能随意混用：

- MVC 通常一请求对应一个工作线程。
- WebFlux 使用非阻塞数据流，常见类型是 `Mono` 和 `Flux`。

### 7.3 Spring Security 与 JWT

需要掌握：

- 身份认证与权限授权的区别。
- JWT Access Token、Refresh Token 和过期时间。
- 401 表示未认证，403 表示已认证但无权限。
- 密码必须使用安全哈希保存。
- 网关鉴权不能代替服务端的业务权限检查。

### 7.4 微服务组件

本项目还涉及：

- Nacos：服务注册发现和配置管理。
- OpenFeign：服务间 HTTP 调用。
- Sentinel：流量控制、熔断和降级。
- Redis：缓存、会话和临时状态。
- Kafka：跨服务异步事件。
- Micrometer/Prometheus：指标采集。
- Springdoc OpenAPI：接口文档。

初学阶段先掌握单服务请求，再学习服务发现、超时、重试、幂等和最终一致性。

## 8. HTTP、REST 与接口契约

前后端通过 HTTP 和 JSON 通信。需要掌握：

| 方法 | 常见用途 | 示例 |
| --- | --- | --- |
| GET | 查询 | `/api/clinical/records/patient/12` |
| POST | 创建 | `/api/clinical/records` |
| PUT | 完整修改或状态操作 | `/api/clinical/records/7/sign` |
| DELETE | 删除 | `/api/clinical/diagnoses/9` |

还要理解 URL 路径参数、查询参数、请求头、请求体、状态码和统一响应结构。

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

接口修改需要同步检查 Controller、请求 DTO、响应 VO、前端 TypeScript 类型、API 客户端和测试。推荐用 OpenAPI 保存可检查的接口契约。

## 9. PostgreSQL 与 SQL

### 9.1 基础能力

需要掌握：

- 数据库、Schema、表、行、列和约束。
- `SELECT`、`INSERT`、`UPDATE`、`DELETE`。
- 主键、外键、唯一约束和非空约束。
- 普通索引、联合索引及其使用场景。
- `JOIN`、分组、聚合和分页。
- 事务的 ACID 特性和隔离级别。
- 行锁、死锁、乐观锁和并发更新。
- 使用 `EXPLAIN` 查看查询计划。

医疗业务特别需要重视患者唯一性、号源并发、医嘱状态、库存扣减和账单一致性。

### 9.2 Flyway 数据库迁移

Flyway 用版本脚本管理表结构变化：

```text
V1__init.sql
V2__seed_data.sql
V3__add_patient_index.sql
```

已发布脚本不能直接修改，应增加新版本脚本。迁移必须在空数据库和升级数据库上验证，确保：

- SQL 可以在真实 PostgreSQL 执行。
- 执行顺序正确。
- 表、索引和约束创建成功。
- 重复运行不会产生不可控结果。

## 10. HTML、CSS 与浏览器基础

学习 React 前应理解：

- HTML 语义结构、表单、表格和可访问性。
- CSS 盒模型、选择器、Flex、Grid 和响应式布局。
- 浏览器 DOM、事件、网络请求和本地存储。
- Chrome/Edge 开发者工具中的 Elements、Network、Console 和 Application 面板。

排查页面问题时，先确认是页面渲染、JavaScript、请求参数、网络状态还是后端响应错误。

## 11. JavaScript 与 TypeScript

### 11.1 JavaScript

需要掌握变量、函数、对象、数组、模块、Promise、`async/await`、异常处理和事件循环。

### 11.2 TypeScript

TypeScript 为接口数据和组件属性提供静态类型。需要掌握：

- `interface`、`type`、联合类型和泛型。
- 可选属性与空值处理。
- 类型收窄和类型推断。
- `import type`。
- 避免滥用 `any` 和非空断言。

```ts
export interface Diagnosis {
  id: number
  diagnosisName: string
  icdCode?: string
  isPrimary?: number
}
```

后端字段变化时，前端类型也必须同步调整。

## 12. React 前端开发

本项目使用 React 18，需要掌握：

- 函数组件和 JSX。
- Props、State 和单向数据流。
- `useState`、`useMemo`、`useEffect` 的适用边界。
- 表单提交、列表渲染和条件渲染。
- 组件拆分、复用和错误状态。
- React Router 路由与受保护页面。
- 懒加载和代码分块。

### 12.1 TanStack Query

用于管理服务器数据：

- `useQuery` 查询与缓存。
- `useMutation` 创建或修改。
- Query Key 标识缓存。
- 成功后使用 `invalidateQueries` 刷新数据。
- 正确处理 loading、error、empty 和 success 四种状态。

### 12.2 Zustand

用于管理登录会话等跨页面客户端状态。服务器数据应优先交给 TanStack Query，避免把所有数据都塞入全局 Store。

### 12.3 Ant Design

需要会使用 `Form`、`Input`、`Select`、`Table`、`Modal`、`Tabs`、`Alert` 和 `Message`，并理解表单校验、表格分页和响应式布局。

### 12.4 Axios 与 Token 刷新

Axios 封装统一请求，通过拦截器添加 JWT，并在 Access Token 过期时尝试刷新。需要避免：

- 同时发起多个重复刷新请求。
- 刷新接口再次进入刷新逻辑。
- 刷新失败后继续保留无效会话。

## 13. 前端工程化

### 13.1 Node.js、npm 与 package.json

Node.js 提供前端工具运行环境，npm 管理依赖和脚本。

```powershell
cd smart-his-frontend
npm install
npm run dev
npm test
npm run lint
npm run build
npm audit
```

- `npm run dev` 启动开发服务器。
- `npm test` 运行 Vitest。
- `npm run lint` 检查代码问题。
- `npm run build` 运行 TypeScript 编译和生产构建。
- `npm audit` 检查已知依赖漏洞。

### 13.2 Vite

Vite 负责开发服务器、模块转换、代理和生产构建。需要理解 `/api` 开发代理、环境变量和构建产物 `dist/`。

## 14. 自动化测试

测试不是最后补的文档，而是判断 Agent 改动是否正确的证据。

### 14.1 后端测试

- JUnit 5：测试框架。
- Mockito：隔离依赖并验证交互。
- Spring Boot Test：验证 Spring 配置和集成行为。
- MockMvc：测试 MVC 接口。
- Testcontainers：用临时 PostgreSQL 等容器做真实集成测试。
- Maven Surefire/Failsafe：执行单元测试和集成测试。

关键业务应测试正常流程、边界条件、非法状态、权限失败和并发冲突。

### 14.2 前端测试

- Vitest：运行单元测试。
- Testing Library：从用户操作角度测试组件。
- Mock：隔离 HTTP 接口。
- Playwright：在真实浏览器中验证登录、挂号、接诊等流程。

测试不能只复制实现。高价值测试应验证接口地址、请求参数、状态变化、错误提示和关键用户路径。

## 15. Docker 与容器

Docker 在当前日常页面开发中可以暂缓，但完整数据库迁移测试、集成环境和交付需要它。

需要理解：

- Image 是只读运行模板。
- Container 是镜像的运行实例。
- Port 将容器服务暴露给主机。
- Volume 保存需要持久化的数据。
- Docker Compose 编排多个容器。

```powershell
docker version
docker ps
docker compose up -d
docker compose logs -f
docker compose down
```

数据库测试使用临时 PostgreSQL 容器，不会修改正式数据库。Docker 未安装时可以完成代码编译，但不能宣称真实 PostgreSQL 迁移已经验收。

## 16. CI/CD、Linux 与部署

### 16.1 GitHub Actions

CI 会在提交或拉取请求后自动运行构建和测试。需要理解 Workflow、Job、Step、Runner、缓存、Artifact 和 Secret。

项目应至少执行：

- Java `clean verify`。
- 前端 test、lint 和 build。
- 依赖漏洞检查。
- 数据库迁移测试。
- 镜像构建和安全扫描。

### 16.2 Linux 基础

生产服务通常运行在 Linux，需要掌握文件权限、进程、端口、日志、环境变量和常用命令，例如 `ls`、`cd`、`ps`、`ss`、`curl`、`tail`。

### 16.3 Kubernetes

进入生产部署阶段后，需要理解 Pod、Deployment、Service、Ingress、ConfigMap、Secret、健康检查、资源限制、滚动更新和回滚。初期开发不要求立即掌握全部 Kubernetes 内容。

## 17. 安全与医疗数据保护

HIS 处理敏感数据，开发人员必须掌握安全编码基础：

- 最小权限和基于角色的访问控制。
- 密码哈希、Token 生命周期和密钥轮换。
- SQL 注入、XSS、CSRF 和越权访问防护。
- 日志脱敏，不能记录身份证号、病历全文、密码和 Token。
- 审计日志记录谁在何时查看或修改了什么。
- 测试数据与真实患者数据隔离。
- 备份、恢复、数据保留和删除策略。

病历签署、医嘱审核、处方发药等关键动作必须有明确状态机、权限和审计记录，不能只依赖前端按钮是否可见。

## 18. 可观测性与故障排查

需要理解日志、指标和链路追踪的区别：

- 日志解释某次事件发生了什么。
- 指标展示一段时间内的数量、延迟和错误率。
- Trace 展示一次请求经过了哪些服务。

排查顺序建议：

1. 浏览器 Network 查看请求和响应。
2. 网关日志确认路由和鉴权。
3. 服务日志确认业务异常。
4. PostgreSQL 检查数据与锁。
5. Redis、Kafka 和下游服务检查依赖状态。
6. 使用 Trace ID 串联一次请求。

## 19. 如何与 Agent 协作

高质量任务应包含范围、期望行为、约束和验收方式：

```text
实现临床接诊页面，对接现有病历、诊断和医嘱接口。
保持现有 Ant Design 风格和移动端适配。
补充相关 API 与组件测试。
运行 test、lint、build 和安全审计。
验证通过后创建独立 Git commit。
```

用户需要承担以下职责：

1. 提供真实业务规则和验收标准。
2. 检查 Agent 是否修改了正确的文件。
3. 阅读测试结果，不能只看 Agent 的文字结论。
4. 对外发布、生产数据操作和密钥配置保持人工控制。
5. 发现失败时提供从第一条错误到最终失败汇总的完整输出。

## 20. 推荐学习顺序

### 第一阶段：能运行项目（1—2 周）

学习 PowerShell、Git、JDK、Maven、Node.js 和 npm。目标是独立完成：

```powershell
git status
.\mvnw.cmd test
cd smart-his-frontend
npm test
npm run build
```

### 第二阶段：能理解前后端（3—6 周）

学习 Java、Spring Boot、HTTP、JSON、SQL、JavaScript、TypeScript 和 React。目标是能跟踪一个字段从数据库到页面的完整路径。

### 第三阶段：能开发业务功能（7—10 周）

学习事务、校验、Spring Security、MyBatis-Plus、TanStack Query、Ant Design 和单元测试。目标是独立完成一个小型业务闭环并提交 Commit。

### 第四阶段：能完成联调交付（11—16 周）

学习 Docker、Testcontainers、Redis、Kafka、Nacos、CI/CD、日志、指标和安全。目标是让功能在可重复环境中通过集成测试并能回滚。

### 第五阶段：持续进阶

深入学习领域驱动设计、微服务一致性、性能调优、Kubernetes、OpenTelemetry、医疗互操作标准 FHIR、数据治理和灾难恢复。

## 21. 技能验收清单

### 基础操作

- [ ] 能进入正确项目目录并确认当前路径。
- [ ] 能执行 `git status`、查看 diff 和创建 Commit。
- [ ] 能识别 Maven、npm 和测试的成功或失败。
- [ ] 能启动前端并用浏览器开发者工具查看请求。
- [ ] 知道哪些文件包含秘密，不能提交。

### 项目开发

- [ ] 能解释 Controller、Service、Mapper、Entity 和 DTO 的职责。
- [ ] 能新增一个 REST 接口并同步前端类型。
- [ ] 能编写基本 SQL 和 Flyway 迁移。
- [ ] 能实现 React 表格、表单和请求状态处理。
- [ ] 能编写后端或前端自动化测试。
- [ ] 能根据堆栈定位第一条有效错误。

### 架构交付

- [ ] 能说明服务边界、同步调用和异步事件的选择依据。
- [ ] 能设计幂等、事务补偿和审计方案。
- [ ] 能配置 CI 门禁、容器健康检查和安全扫描。
- [ ] 能通过日志、指标和 Trace 排查跨服务故障。
- [ ] 能执行数据库备份恢复、应用回滚和故障演练。

## 22. 当前项目最优先掌握的技术

按当前开发进度，建议优先级如下：

1. **Git 与 PowerShell**：保证每次 Agent 改动可检查、可提交、可回滚。
2. **Java 21、Spring Boot 和 Maven**：理解后端接口与构建失败。
3. **HTTP、JSON、JWT 和 PostgreSQL**：理解前后端与数据链路。
4. **TypeScript、React、TanStack Query 和 Ant Design**：继续完成业务页面。
5. **JUnit 5、Vitest 和 Testing Library**：为每次改动提供验证证据。
6. **Docker 与 Testcontainers**：完成真实 PostgreSQL 迁移和集成测试。
7. **Redis、Kafka、Nacos 和微服务治理**：完成跨服务联调。
8. **CI/CD、安全和可观测性**：达到可发布、可运维水平。

学习时应围绕一个真实功能形成闭环，例如“挂号”或“临床接诊”：读懂需求，找到接口和表，修改代码，增加测试，运行验证，检查 diff，最后创建 Commit。这样比孤立记忆框架 API 更容易形成可用能力。
