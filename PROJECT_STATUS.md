# Smart HIS 当前项目状况

更新时间：2026-09-20

## 总体结论

项目已完成 M4 至 M9 的主要功能开发，代码已合并到本地 `main` 分支，并与 `origin/main` 同步。当前服务处于关闭状态，真实 PostgreSQL/Flyway 迁移和浏览器业务流程仍需要在本机环境执行最终验收。

## 已实现阶段

### M4：门诊临床效率

- 可配置病历模板和按科室分类。
- 患者查询支持数字 ID、EMPI、姓名、证件号和手机号等标识。
- 门诊接诊、病历、诊断和医嘱流程已接入。
- 病历草稿支持浏览器本地自动保存和刷新恢复。

### M5：检验、检查与处方闭环

- 检验检查申请、接收、执行、审核和结果回写。
- 危急值标记和确认记录。
- 电子处方、剂量和数量校验。
- 过敏、重复用药规则、处方审核、退回和打印。

### M6：药房与库存

- 药品目录、价格、库存、批号和有效期管理。
- 入库、出库、盘点、退药和报损。
- 门诊发药、处方审核、审核单打印。
- 库存不足、近效期提醒和原子扣减。
- 药房前端页面和后端服务已接入。

### M7：收费、医保与对账

- 收费项目、价格配置、预交金和欠费管理。
- 收款、退费、作废、日结和收银员交班。
- 本地医保目录结算预览适配层。
- 费用清单、收退款凭据和正式发票打印。
- 正式发票号支持幂等生成和账单追溯。

### M8：住院与急诊

- 入院、转科、床位占用和释放、出院费用。
- 急诊分级分诊、抢救和留观记录。
- 手术申请、排班、麻醉字段和出院小结。

### M9：权限、安全与运营分析

- 角色、菜单、科室和数据范围权限。
- JWT 权限、MFA 二次认证和敏感操作审计。
- 门诊量、收入、退费、库存、医生和科室报表。
- Excel/PDF 导出和报表查询条件保存。

## 最新代码状态

最新提交：

```text
adec38d fix: N+1 query, audit timing, permission wildcards, bed release logging
```

此前阶段提交包括：

```text
9edc272 feat(m7): complete billing reconciliation and invoice printing
b928584 feat(pharma):add pharmacy service source
b7cf233 feat(m6): complete pharmacy inventory and clinical workflows
119d9a6 feat(m5): complete clinical exam workflow and prescription validation
```

本地 `main` 与 `origin/main` 当前没有领先或落后提交。

## 当前未提交文件

以下回归测试仍需单独提交：

```text
his-patient/src/test/java/com/smarthis/patient/service/impl/AdmissionServiceImplTest.java
```

以下文件属于临时文件或运行日志，不应提交：

```text
.workbuddy/
his-gateway/gateway.log
his-pharma/pharma.log
```

## 已完成验证

- 前端测试：18 个测试文件、55 个测试通过。
- M7 打印相关测试：9/9 通过。
- `npm run lint` 通过。
- `npm run build` 通过。
- `git diff --check` 通过。
- 已补充住院列表批量加载患者姓名的回归测试。

## 尚未完成的验收

Maven 全量测试和真实迁移验证仍受本机 Maven 仓库目录权限影响，曾出现 `java.nio.file.AccessDeniedException`。真实数据库和浏览器验收需要在 Docker 与本机服务权限正常的终端执行，当前自动化会话无法代替用户访问 Docker API。

建议验收顺序：

1. 启动 Docker 基础设施和后端服务。
2. 启动前端并登录系统。
3. 依次验证临床、药房、收费、住院急诊和运营分析页面。
4. 执行 PostgreSQL/Flyway 迁移测试。
5. 确认无密钥、令牌、日志和临时文件被提交。

## 常用命令

项目目录：

```powershell
Set-Location "C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source"
```

启动基础设施：

```powershell
docker compose up -d --wait
```

启动前端：

```powershell
Set-Location .\smart-his-frontend
npm.cmd run dev
```

前端地址：`http://127.0.0.1:5173/`

关闭前端后端：

```powershell
docker compose down
```
