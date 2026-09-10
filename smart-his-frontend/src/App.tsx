import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { PlaceholderPage } from '@/pages/PlaceholderPage'
import { DashboardPage } from '@/pages/workbench/DashboardPage'
import { ProtectedRoute } from '@/router/ProtectedRoute'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
})

export default function App() {
  return (
    <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#167f8c', borderRadius: 10, colorBgLayout: '#f4f8fa' } }}>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route index element={<DashboardPage />} />
                <Route path="patient" element={<PlaceholderPage title="患者服务" />} />
                <Route path="clinical" element={<PlaceholderPage title="临床诊疗" />} />
                <Route path="resource" element={<PlaceholderPage title="资源保障" />} />
                <Route path="operations" element={<PlaceholderPage title="运营管理" />} />
                <Route path="pharma" element={<PlaceholderPage title="药事管理" />} />
              </Route>
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </QueryClientProvider>
    </ConfigProvider>
  )
}
