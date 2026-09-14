# Smart HIS 用户终端检查与提交手册

> 用途：每次 Agent 修改代码、配置、数据库脚本或文档后，用户可以按改动类型复制本手册中的命令完成检查、验证和提交。  
> 推荐终端：Windows PowerShell。  
> 项目目录：`C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source`

## 0. 使用规则

只复制标有“执行命令”的代码块。终端显示的以下文字属于程序输出，不能重新输入：

```text
BUILD SUCCESS
BUILD FAILURE
Tests run: 16
files changed
create mode 100644
PS C:\Users\...
```

如果终端进入长文本分页界面，按键盘 `q` 退出。当前行输入错误但还没有执行时，按 `Ctrl+C` 清除。

不要直接执行 `git add .`，应该明确列出本次任务需要提交的文件。

## 1. 确认当前使用PowerShell

PowerShell提示符通常以 `PS` 开头：

```text
PS C:\Users\21529>
```

如果当前提示符只有 `C:\...>`，说明正在使用CMD。可以执行以下命令进入PowerShell：

```cmd
powershell
```

CMD中不能直接执行 `Get-Location`、`Get-NetTCPConnection` 和 `$env:JAVA_HOME=...` 等PowerShell命令。

## 2. 进入项目并确认目录

执行命令：

```powershell
cd "C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source"
Get-Location
```

正常结果中的路径应为：

```text
C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source
```

如果路径是 `C:\Users\21529`，在该位置执行Git命令可能得到 `not a git repository`。

## 3. 检查Git状态

执行命令：

```powershell
git status
```

常见状态说明：

| 英文状态 | 含义 |
| --- | --- |
| `modified` | 已有文件被修改 |
| `Untracked files` | 新文件尚未由Git管理 |
| `Changes not staged for commit` | 改动尚未放入暂存区 |
| `Changes to be committed` | 文件已经暂存，将进入下一次Commit |
| `working tree clean` | 当前没有未提交改动 |
| `ahead of origin/main` | 本地存在尚未推送GitHub的Commit |

## 4. 查看具体修改

查看修改文件统计：

```powershell
git diff --stat
```

查看所有已跟踪文件的详细修改：

```powershell
git diff
```

查看某一个文件：

```powershell
git diff -- PROJECT_COMPLETION_PLAN.md
```

新文件在暂存前不会出现在普通 `git diff` 中。可以用VS Code打开：

```powershell
code "需要检查的文件路径"
```

也可以读取文本文件：

```powershell
Get-Content "需要检查的文件路径"
```

## 5. 配置当前终端使用Java 21

执行命令：

```powershell
$env:JAVA_HOME="C:\Users\21529\Documents\Codex\2026-09-08\bao\work\toolchain\jdk-zulu-21.0.11\zulu21.50.19-ca-jdk21.0.11-win_x64"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

正常结果应包含：

```text
openjdk version "21.0.11"
```

这项配置只对当前终端有效，不会修改电脑的全局Java设置。

## 6. 后端Java改动验证

### 6.1 验证单个模块

以临床模块为例：

```powershell
.\mvnw.cmd -pl his-clinical -am test
```

替换模块名即可验证其他模块：

```powershell
.\mvnw.cmd -pl his-patient -am test
.\mvnw.cmd -pl his-auth -am test
.\mvnw.cmd -pl his-pharma -am test
```

### 6.2 完整后端验证

修改根 `pom.xml`、公共模块、多个服务、依赖、数据库迁移、CI或发布配置时执行：

```powershell
.\mvnw.cmd clean verify
```

正常结果：

```text
BUILD SUCCESS
```

如果出现 `BUILD FAILURE`，不要只发送最后一行。应保留第一条 `[ERROR]` 到最终失败汇总。

### 6.3 使用项目验证脚本

也可以显式指定Java 21和本地Maven缓存：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\verify-local.ps1" -JavaHome "C:\Users\21529\Documents\Codex\2026-09-08\bao\work\toolchain\jdk-zulu-21.0.11\zulu21.50.19-ca-jdk21.0.11-win_x64" -MavenRepository ".mvn\repository"
```

## 7. 前端改动验证

进入前端目录：

```powershell
cd "C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source\smart-his-frontend"
```

依次执行：

```powershell
npm test
npm run lint
npm run build
```

如果修改了 `package.json` 或 `package-lock.json`，再执行：

```powershell
npm audit
```

正常结果应满足：

- Vitest测试全部通过。
- Oxlint没有错误。
- TypeScript编译通过。
- Vite构建完成。
- npm audit没有高危漏洞。

完成后返回仓库根目录：

```powershell
cd ..
```

## 8. 文档改动验证

项目完成计划：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\test-completion-plan.ps1"
```

编程技术指南：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\test-programming-technology-guide.ps1"
```

用户终端手册：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\test-user-terminal-checklist.ps1"
```

检查Git空白和冲突标记：

```powershell
git diff --check
```

命令没有输出且退出码为0，通常表示检查通过。

## 9. PostgreSQL与Flyway迁移验证

需要真实数据库验证时，先检查Docker：

```powershell
docker version
```

正常情况下应同时显示 `Client` 和 `Server`。只有Client而没有Server，表示Docker引擎没有运行。

强制执行PostgreSQL迁移测试：

```powershell
$env:REQUIRE_MIGRATION_TESTS="true"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\verify-local.ps1" -JavaHome "C:\Users\21529\Documents\Codex\2026-09-08\bao\work\toolchain\jdk-zulu-21.0.11\zulu21.50.19-ca-jdk21.0.11-win_x64" -MavenRepository ".mvn\repository"
```

如果Docker尚未安装，这项测试应记录为“待验证”，不能记录为“全部通过”。

`REQUIRE_MIGRATION_TESTS` 会在当前PowerShell会话中持续生效。强制迁移测试结束后，或者Docker尚未安装而需要运行其他后端测试时，应清除它：

```powershell
Remove-Item Env:REQUIRE_MIGRATION_TESTS -ErrorAction SilentlyContinue
```

确认变量已经清除：

```powershell
Test-Path Env:REQUIRE_MIGRATION_TESTS
```

正常结果为 `False`。清除变量后，Docker不可用时迁移测试会标记为跳过；这只能证明其他后端构建和测试通过，不能代替真实PostgreSQL迁移验收。

## 10. 查看端口和服务状态

查看8080端口：

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
```

查看5173端口：

```powershell
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue
```

没有输出表示当前没有程序监听该端口，不代表命令失败。

检查PowerShell命令是否成功：

```powershell
$?
```

检查Java、Maven、Git和npm等外部程序的退出码：

```powershell
$LASTEXITCODE
```

通常退出码 `0` 表示成功。

## 11. 启动前端并人工检查

```powershell
cd "C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source\smart-his-frontend"
npm run dev
```

浏览器打开：

```text
http://localhost:5173
```

人工检查：

- 页面可以打开，没有乱码和明显错位。
- 正常数据、空数据、加载中和加载失败都有合理显示。
- 表单必填项和错误提示有效。
- 新增、修改、删除和状态操作符合业务规则。
- 刷新页面后登录和数据状态正确。
- 不同用户和角色的权限正确。
- 浏览器Network中没有意外的401、403、404或500。
- 手机或窄窗口下仍能完成核心操作。

停止前端服务器时，在运行 `npm run dev` 的终端按：

```text
Ctrl+C
```

## 12. 检查敏感信息和无关文件

执行：

```powershell
git status
git diff
```

确认没有以下内容：

- 密码、Token、API密钥和私钥。
- 真实患者身份和病历数据。
- `.env`本地配置。
- `target/`、`dist/`、日志和缓存。
- 临时截图或错误命令产生的文件。
- 其他任务尚未完成的代码。

## 13. 暂存本次任务文件

通用格式：

```powershell
git add "文件1" "文件2" "目录3"
```

示例：

```powershell
git add smart-his-frontend/src/pages/clinical smart-his-frontend/src/services/clinicalApi.ts smart-his-frontend/src/services/clinicalApi.test.ts
```

不要用 `git add .`。

如果错误地暂存了文件，取消暂存：

```powershell
git restore --staged "错误文件路径"
```

取消暂存不会删除工作区中的文件内容。

## 14. 检查准备提交的内容

```powershell
git status
git diff --staged --stat
git diff --staged
```

重点确认：

- 暂存文件全部属于同一个任务。
- 新增文件没有遗漏。
- 没有敏感信息。
- 没有与任务无关的格式变化。
- 测试文件与功能代码放在同一个Commit。

查看完成后按 `q` 退出分页器。

## 15. 配置Git提交身份

查看当前仓库是否已有身份：

```powershell
git config --get user.name
git config --get user.email
```

如果没有输出，在当前仓库配置：

```powershell
git config user.name b3182
git config user.email b3182@users.noreply.github.com
```

该命令不使用 `--global`，只影响当前仓库。

## 16. 创建独立Commit

常规格式：

```powershell
git commit -m "类型(范围): 具体改动"
```

示例：

```powershell
git commit -m "feat(clinical): add encounter workflow"
git commit -m "fix(auth): handle expired refresh tokens"
git commit -m "test(database): verify Flyway migrations"
git commit -m "docs: add user terminal checklist"
```

常用类型：

| 类型 | 用途 |
| --- | --- |
| `feat` | 新功能 |
| `fix` | 修复问题 |
| `test` | 测试改动 |
| `docs` | 文档改动 |
| `build` | 构建或依赖改动 |
| `refactor` | 不改变行为的代码重构 |
| `chore` | 常规维护 |

如果PowerShell引号输入有问题，可以使用不含空格的消息：

```powershell
git commit -m docs:add-user-terminal-checklist
```

## 17. 提交后复核

```powershell
git status
git log -1 --oneline
git show --stat --oneline HEAD
```

确认：

- 最新Commit存在。
- Commit信息正确。
- 文件范围正确。
- 没有应提交但遗漏的相关文件。

终端显示的 `files changed`、`insertions`、`create mode` 是Git输出，不要重新输入。

## 18. 推送GitHub

先确认本地Commit和分支：

```powershell
git status
git log --oneline -5
git branch --show-current
```

需要把本地Commit上传到GitHub时执行：

```powershell
git push origin main
```

推送会修改远程仓库。执行前应确认本地Commit已经检查完毕，分支和远程地址正确。

查看远程地址：

```powershell
git remote -v
```

## 19. 失败时收集信息

向Agent提供以下内容：

```text
当前目录：
执行命令：
第一条错误：
完整错误区间：
最终结果：
最近修改：
截图或报告路径：
```

Git信息：

```powershell
Get-Location
git status
git log -3 --oneline
```

端口信息：

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue
```

Java和Maven信息：

```powershell
java -version
.\mvnw.cmd -version
```

前端环境信息：

```powershell
node --version
npm --version
```

## 20. 每次改动的最短标准流程

第一组，确认目录和改动：

```powershell
cd "C:\Users\21529\Documents\Codex\2026-09-08\bao\smart-his-source"
Get-Location
git status
git diff --stat
git diff
```

第二组，运行Agent要求的相关测试。前端改动通常执行：

```powershell
cd smart-his-frontend
npm test
npm run lint
npm run build
cd ..
```

后端改动通常执行：

```powershell
.\mvnw.cmd clean verify
```

第三组，只暂存本次文件并检查：

```powershell
git add "本次任务文件"
git diff --staged --stat
git diff --staged
```

第四组，提交并复核：

```powershell
git commit -m "类型(范围): 具体改动"
git status
git log -1 --oneline
```

## 21. 给Agent的结果反馈模板

```text
已完成用户配合检查：

1. 当前目录：正确
2. Git修改范围：正确
3. 后端测试：未涉及 / BUILD SUCCESS / 失败
4. 前端测试：未涉及 / 全部通过 / 失败
5. Lint：未涉及 / 通过 / 失败
6. 生产构建：未涉及 / 通过 / 失败
7. 数据库迁移：未涉及 / 通过 / 待Docker安装
8. 页面人工检查：未涉及 / 通过 / 存在问题
9. 敏感信息检查：通过
10. Commit：提交哈希和消息

遗留事项：
```

Agent收到结果后，应检查失败项或提交记录，并继续处理尚未完成的内容。
