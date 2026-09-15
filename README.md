# Smart HIS

基于 Java 21、Spring Boot 3、Spring Cloud、React 18 和 PostgreSQL 16 的基础门诊 HIS。当前版本只交付登录、患者、挂号、临床接诊和门诊收费流程。

## 模块

包含公共组件、认证、网关、患者、临床、运营和数据库迁移测试模块。药事、库存、住院、急诊、会诊、CDSS、DRG 和平台管理不属于当前版本。

详细范围、验收标准和执行顺序见 [`PROJECT_COMPLETION_PLAN.md`](PROJECT_COMPLETION_PLAN.md)。

## 构建

需要安装 Java 21。项目通过 Maven Wrapper 固定使用 Maven 3.9.16，无需单独安装 Maven。

Linux / macOS：

```bash
./mvnw clean verify
```

Windows：

```powershell
.\mvnw.cmd clean verify
```

如果 Java 和 Maven 安装在工作区工具链目录，Windows 可使用项目脚本自动配置当前测试进程并执行全量验证：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify-local.ps1
```

脚本也支持通过 `-JavaHome`、`-MavenHome` 和 `-MavenRepository` 指定自定义安装位置。

验证项目没有使用未声明的本地制品时，请指定一个新的缓存目录并要求它必须为空：

```powershell
$cleanRepository = Join-Path $env:TEMP "smart-his-maven-$([guid]::NewGuid())"
powershell -ExecutionPolicy Bypass -File scripts/verify-local.ps1 `
  -MavenRepository $cleanRepository `
  -RequireEmptyMavenRepository
```

使用 Docker Compose 启动本地基础设施：

```bash
cp .env.example .env
# Edit .env and provide secure values first.
docker compose up -d
```

Windows 可在填写 `.env` 后一键启动基础设施、5 个应用服务和工作台：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-local.ps1
```

启动完成后访问 `http://localhost:8080`。工作台由网关直接托管，不需要单独安装 Node.js。
