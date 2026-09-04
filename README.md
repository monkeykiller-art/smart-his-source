# Smart HIS

新一代智慧医院信息系统（HIS）后端工程，基于 Java 21、Spring Boot 3 与 Spring Cloud 构建。

## 模块

包含认证、网关、患者、临床、资源、运营、协同、药事、临床决策支持、DRG、急诊及平台等服务模块。

## 构建

```bash
mvn clean package
```

使用 Docker Compose 启动本地基础设施：

```bash
cp .env.example .env
# Edit .env and provide secure values first.
docker compose up -d
```
