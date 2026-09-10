# Smart HIS 前端构建结构改进计划

> 目标：把 `smart-his-source` 从「Spring Boot static 目录下的占位 HTML」，升级到符合业界主流 HIS 前端工程标准的现代化工作台。

---

## 一、调研对象

下面 7 个仓库是从 GitHub 上筛出来 HIS / 医疗管理系统领域有代表性、且**前端构建结构差异明显**的样本，方便我们横向对比。

| # | 仓库 | 定位 | 前端构建栈（来自源码 / README） |
|---|---|---|---|
| 1 | [15673312611/Hospital_HIS](https://github.com/15673312611/Hospital_HIS) | HIS 风格的中后台模板（实为价格系统借皮，30 个 HIS 命名页面） | React 18 + TS + Vite + AntD 5 + Tailwind + Zustand + React Query + React Hook Form + Storybook + ESLint/Prettier + Husky + lint-staged |
| 2 | [Jamesuchechi/HealVista-...](https://github.com/Jamesuchechi/HealVista-hospital-System-react-node-express-sqlserver-app) | 三角色（Admin/Doctor/Patient）医院管理 | React + Tailwind + Zustand + Lucide React + React Router + Vite proxy + Node/Express + MS SQL Server |
| 3 | [Sambhav-Gautam/HealthCareSystem](https://github.com/Sambhav-Gautam/HealthCareSystem) | 微服务架构 HIS（api-gateway / auth-service / medical-service） | React 19 + Vite + Tailwind + React Router + Axios + JWT，**前后端分仓** |
| 4 | [FaithRono/hope_healthcare](https://github.com/FaithRono/hope_healthcare) | 经典 admin-backend 分仓 React | React + Express + MongoDB，前后端各一目录，无构建工具现代化 |
| 5 | [Mwantech/Info-System](https://github.com/Mwantech/Info-System) | 健康项目管理 | React + Node/Express + MongoDB + JWT，传统前后端分仓 |
| 6 | [cloveropen/his](https://github.com/cloveropen/his)（本地已克隆 `his/`） | 经典国产 HIS 前端 | Vue 2.6 + Vue CLI（Webpack） + Vuetify 2 + Vuex + Vue Router，JavaScript 无 TS |
| 7 | 小苹果 `apple_webhis`（Gitee，未克隆） | 信创国产化 HIS | .NET Core 6 + Vue 3 + LayUI + OpenGauss |

---

## 二、前端构建结构对比（横向 8 维）

| 维度 | 1. Hospital_HIS | 2. HealVista | 3. HealthCareSystem | 6. cloveropen/his | **smart-his-source 现状** |
|---|---|---|---|---|---|
| **框架** | React 18 | React（无版本声明）| React 19 | Vue 2.6 | ❌ 无框架 |
| **构建工具** | Vite 5 | Vite | Vite | Webpack（Vue CLI） | ❌ 无 |
| **TypeScript** | ✅ 严格模式 + 路径别名 | ❌ JS | ❌ JS | ❌ JS | ❌ |
| **UI 组件库** | Ant Design 5 + Tailwind | Tailwind + Lucide | Tailwind | Vuetify 2 + Element UI 暗示 | ❌ |
| **状态管理** | Zustand + React Query | Zustand | Context | Vuex | ❌ |
| **数据请求** | React Query + Axios | fetch 包装 | Axios | Axios | ❌ |
| **表单** | React Hook Form + Yup | 原生 | 原生 | Element Form | ❌ |
| **工程化** | ESLint + Prettier + Husky + lint-staged + Storybook + tsconfig strict | 基础 | 基础 | ESLint + Mocha | ❌ |
| **路由** | React Router 6 | React Router | React Router | Vue Router | ❌ |
| **国际化** | ❌（可扩展） | ❌ | ❌ | ❌ | ❌ |
| **测试** | Storybook（视觉） | 无 | 无 | Mocha（3 个测试） | ❌ |
| **部署产物** | `dist/` 静态 | `dist/` 静态 | `dist/` 静态 | `dist/`（SPA） | Spring Boot 打包的 static/ |
| **前后端协同** | 独立前端 | Vite proxy /api/* | Vite proxy /api/* | 直接调 nginx 后端 | 嵌在网关 static/ 里 |

---

## 三、smart-his-source 前端现状（再确认）

> 这部分之前在 `IMPROVEMENT_PLAN.md` 里点过名，这里给出真实文件证据。

```
his-gateway/src/main/resources/static/
├── index.html            # 81 行：纯占位，"Smart HIS 工作台"标题 + 几个空 div
└── assets/app.js         # 控制台日志，没有业务逻辑
```

**问题：**

1. ❌ **没有前端工程**：无 `package.json`、无构建工具、无模块系统、无依赖管理
2. ❌ **没有 UI 框架**：81 行 HTML 是裸 markup，肉眼可见的"骨架感"
3. ❌ **没有路由**：多个模块（his-auth/clinical/patient/...）都接入了同一个工作台，但工作台无法呈现它们
4. ❌ **没有鉴权 UI**：登录态、角色路由、权限控制在网关层做了（JWT 鉴权有），但前端没有承接
5. ❌ **没有 API 客户端**：207 个 REST 端点、13 个微服务，前端没有 axios / fetch 包装
6. ❌ **后端已经在按"前端 SPA"准备**：`his-gateway` 自带 static/ 目录就是为单页应用留的

---

## 四、目标架构（推荐方案）

**主推方案 A**：**React 18 + TypeScript + Vite + Ant Design 5 + Zustand + React Query + React Router 6**（对齐 #1）

理由：
- 与 `smart-his-source` 的 Java 后端完全解耦（前后端分离，符合 Sambhav-Gautam 微服务风格）
- AntD 5 是国内中后台**事实标准**（与 cloveropen Element、apple LayUI 同源更稳）
- Vite 构建比 webpack 快 5-10 倍，对应 dev loop 友好
- React Query 把 207 个 REST 端点的缓存/重试/失效管理接管，契合 OpenFeign 微服务
- Zustand 比 Redux 轻量 60%，比 Vuex 跨框架学习成本低
- TypeScript 严格模式 + 路径别名 = 工程化基线

**备选方案 B**：**Vue 3 + TS + Vite + Element Plus + Pinia + Vue Router 4**（对齐 cloveropen/his，但升级到 Vue3）

适用场景：团队 Vue 经验 > React 经验；想延续 cloveropen/his 的 Vuetify 风格。

**反对方案 C**：继续把前端嵌在 `his-gateway/src/main/resources/static/`

理由：
- 单仓构建链路长（先 maven 打包前端），每次改一行 JS 都要走整个 Spring Boot 构建
- 未来要拆 micro-frontend、移动端、患者端 App 时无法复用代码

---

## 五、推荐目录结构（参考 #1 调整后）

```
smart-his-frontend/                       # 新建独立仓库或子目录
├── public/                              # 静态资源（favicon、robots）
├── src/
│   ├── assets/                          # 图片、Logo、SVG
│   ├── components/                      # 通用组件
│   │   ├── Layout/
│   │   │   ├── index.tsx                # 主布局：Header + Sidebar + Content
│   │   │   ├── Header.tsx               # 用户、消息、登出
│   │   │   └── Sidebar.tsx              # 菜单：基于 RBAC 动态生成
│   │   ├── Business/                    # 业务组件（如 PatientCard、OrderItem）
│   │   └── Common/                      # AntD 包装（LoadingEmpty、ErrorBoundary）
│   ├── pages/                           # 页面（与后端模块一一对应）
│   │   ├── auth/                        #   Login、ForgotPassword
│   │   ├── patient/                     #   PatientList、PatientDetail
│   │   ├── clinical/                    #   Order、EMR、Prescription
│   │   ├── resource/                    #   Bed、Pharmacy、Inventory
│   │   ├── operations/                  #   Billing、Account、Settlement
│   │   ├── pharma/                      #   PrescriptionReview、DDI
│   │   ├── cdss/                        #   AlertCenter（待后端实现后接入）
│   │   ├── drg/                         #   DRGGrouping（待后端实现后接入）
│   │   ├── platform/                    #   MasterData（待后端实现后接入）
│   │   └── workbench/                   #   Dashboard（首屏 KPI）
│   ├── router/                          # React Router 6 配置 + 路由守卫
│   │   ├── index.tsx
│   │   ├── routes.ts                    # 路由表（懒加载）
│   │   └── guards.tsx                    # AuthGuard + RoleGuard
│   ├── services/                        # API 客户端
│   │   ├── http.ts                      # 拦截器、JWT 注入、统一错误
│   │   ├── api/                         # 按后端模块分文件
│   │   │   ├── auth.ts
│   │   │   ├── patient.ts
│   │   │   └── ...
│   ├── stores/                          # Zustand stores
│   │   ├── auth.ts                      # 登录态、token、用户信息
│   │   ├── menu.ts                     # 动态菜单
│   │   └── theme.ts
│   ├── hooks/                           # 自定义 hooks（useAuth、useTable）
│   ├── types/                           # 全局 TS 类型 + 后端 DTO 镜像
│   ├── utils/                           # 工具函数（date、format、validate）
│   ├── styles/                          # 全局样式 + Tailwind config
│   ├── App.tsx                          # 根组件
│   ├── main.tsx                         # 入口
│   └── env.d.ts                         # Vite 环境变量类型
├── .eslintrc.cjs                        # ESLint + TS 规则
├── .prettierrc
├── tailwind.config.js                   # 主题色 = 医疗蓝绿
├── postcss.config.js
├── tsconfig.json                        # 严格模式 + 路径别名
├── tsconfig.node.json
├── vite.config.ts                       # Vite 配置 + proxy /api/*
├── package.json
├── pnpm-lock.yaml                       # 推荐 pnpm（比 npm 快 3 倍、磁盘友好）
└── README.md
```

---

## 六、与 Spring Cloud 后端的集成

### 6.1 开发期：Vite proxy

```ts
// vite.config.ts
export default defineConfig({
  plugins: [react()],
  resolve: { alias: { '@': '/src' } },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // his-gateway
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api/, ''),
      },
    },
  },
})
```

开发时前端跑 5173、网关跑 8080，所有 `/api/*` 由 Vite proxy 转发到网关，再由网关路由到下游 13 个微服务。

### 6.2 生产期：独立部署 + Nginx

```nginx
# /etc/nginx/conf.d/smart-his.conf
server {
  listen 80;
  server_name his.example.com;
  root /opt/smart-his/dist;        # 前端构建产物
  index index.html;
  location / {
    try_files $uri $uri/ /index.html;  # SPA history fallback
  }
  location /api/ {
    proxy_pass http://his-gateway:8080/;  # 指向网关服务
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

> 之前 `IMPROVEMENT_PLAN.md` 已提到 Docker 卸载后 Nginx 部署链路更轻，这条路径不再依赖 Docker。

### 6.3 单点登录：承接现有 JWT

`his-auth` 已有 `/auth/login` 返回 JWT 的接口，前端只需：

```ts
// src/services/http.ts
http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
http.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);
```

---

## 七、改进路线图（6 周可上线基础版本）

| 阶段 | 周 | 交付物 | 验收标准 |
|---|---|---|---|
| **P0 基线** | W1 | 脚手架 + 鉴权 + 工作台占位 | `pnpm dev` 跑通；登录页对接 `/auth/login`；Vite proxy 通网关 |
| **P1 核心域** | W2-W3 | patient/clinical/resource/operations/pharma 五域 CRUD 页面（基于 AntD ProTable 模板）| 13 个微服务中的 7 个关键模块端点全覆盖 UI |
| **P2 工程化** | W3-W4 | ESLint + Prettier + Husky + lint-staged + TS strict + 路径别名 + Storybook | CI 跑通；`pnpm lint && pnpm build` 零 warning |
| **P3 路由权限** | W4 | 动态菜单 + RBAC 路由守卫 + 403 页 | 不同角色登录看到不同菜单；越权访问跳 403 |
| **P4 微前端预留** | W5 | 预留 qiankun/wujie 接入点（基座模式）| 文档说明 micro-app 接入步骤 |
| **P5 打包优化** | W5-W6 | 路由懒加载、CDN 注入、按需引入 AntD | 首屏 < 2s；gzip 后 < 1MB |
| **P6 智能增量** | W6+ | 工作台数据接入、ECharts 看板、CDSS 告警可视化 | 后续根据后端进度接入 |

---

## 八、风险与决策点（提交给架构 owner 拍板）

| # | 决策点 | 选项 | 建议 |
|---|---|---|---|
| 1 | 框架：React vs Vue3 | A / B | React（团队现状 + AntD 生态更广） |
| 2 | 包管理器：pnpm vs npm vs yarn | — | pnpm（速度 + 磁盘 + monorepo 友好） |
| 3 | CSS 方案：Tailwind vs CSS-in-JS vs AntD-only | — | AntD + 轻量 Tailwind（按需） |
| 4 | 状态管理：Zustand vs Redux Toolkit vs Jotai | — | Zustand（轻量、TS 友好） |
| 5 | 部署：单仓嵌入 vs 独立仓库 | — | 独立仓库 + Nginx（参考 #3） |
| 6 | 数据请求：React Query vs SWR vs 原生 | — | React Query（缓存/重试/失效齐全） |
| 7 | 微前端：现在 vs 未来 | — | 现在不引入，路由层留 hook（避免过度设计） |
| 8 | 国际化：now vs later | — | 后续迭代，先 zh-CN |
| 9 | 测试：单测 vs e2e | — | Vitest 单测覆盖 hooks/utils；e2e 后续 Playwright |
| 10 | 工作台来源：自研 vs 模板 | — | 模板自研（用 AntD ProComponents 起步，二开模板代码） |

---

## 九、与 `IMPROVEMENT_PLAN.md` 的关系

| 文档 | 范围 | 状态 |
|---|---|---|
| `IMPROVEMENT_PLAN.md`（业务维度）| 13 模块的业务空壳补齐、智慧化增量 | 已有 |
| `FRONTEND_BUILD_PLAN.md`（前端工程维度）| 工作台从 0 到可用 SPA 的工程路线 | **本计划** |
| 后续：后端细化 | DRG/CDSS/EMPI 的实现细节 | 待出 |

两份计划是正交的，可以并行执行。本计划是「给后端业务能力一个可被使用的 UI 入口」。

---

## 十一、可立即执行的第一步（5 分钟跑通）

```bash
# 1. 在 smart-his-source 同级创建前端仓
mkdir smart-his-frontend && cd smart-his-frontend

# 2. 用 Vite 官方模板初始化
pnpm create vite . --template react-ts
pnpm install

# 3. 安装核心依赖
pnpm add antd @ant-design/icons react-router-dom zustand @tanstack/react-query axios dayjs
pnpm add -D tailwindcss postcss autoprefixer @types/node

# 4. 启动
pnpm dev
# → http://localhost:5173
```

跑通后，下一步加 `vite.config.ts` 的 proxy（6.1 节）、实现 Login 页 + Layout + 一个测试页面（如 PatientList 对接 `/api/patient/**`）。

---

**最后一句话**：smart-his-source 的 Java 后端已经做了 70%，但前端从 0 开始。这一步早晚要做，越晚做集成成本越高——因为再往后就要带着「业务空壳 + UI 占位 + 无测试」三件套一起改了。建议本周内拉一个前端脚手架先跑起来，2 周内把 W1-W2 交付掉。