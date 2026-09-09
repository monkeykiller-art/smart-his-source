# Smart HIS

新一代智慧医院信息系统（HIS），基于 Java 21、Spring Boot 3 与 Spring Cloud 构建。网关内置轻量业务工作台，覆盖方案中的患者、临床、资源、运营和协同五大域。

## 模块

包含认证、网关、患者、临床、资源、运营、协同、药事、临床决策支持、DRG、急诊及平台等服务模块。

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

使用 Docker Compose 启动本地基础设施：

```bash
cp .env.example .env
# Edit .env and provide secure values first.
docker compose up -d
```

Windows 可在填写 `.env` 后一键启动基础设施、全部服务和工作台：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-local.ps1
```

启动完成后访问 `http://localhost:8080`。工作台由网关直接托管，不需要单独安装 Node.js。
