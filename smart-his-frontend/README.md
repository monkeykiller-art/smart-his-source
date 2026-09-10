# Smart HIS Frontend

React 18、TypeScript、Vite 和 Ant Design 5 构建的 Smart HIS 工作台。

## 开发

```bash
npm install
npm run dev
```

开发服务器运行在 `http://localhost:5173`，并将 `/api` 原样代理到 `http://localhost:8080`。后端网关必须保留 `/api` 前缀才能匹配现有路由。

## 验证

```bash
npm run lint
npm test
npm run build
npm audit --audit-level=moderate
```

认证会话保存在 `sessionStorage`，关闭浏览器会话后自动清除。API 返回 401 时，客户端只执行一个并发刷新请求；刷新失败会清理本地会话并返回登录页。
