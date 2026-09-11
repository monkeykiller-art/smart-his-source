import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ConfigProvider } from 'antd'
import { Spin } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { lazy, Suspense } from 'react'
import { AppLayout } from '@/components/layout/AppLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { PlaceholderPage } from '@/pages/PlaceholderPage'
import { DashboardPage } from '@/pages/workbench/DashboardPage'
import { ProtectedRoute } from '@/router/ProtectedRoute'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
})
const PatientPage = lazy(() => import('@/pages/patient/PatientPage').then((module) => ({ default: module.PatientPage })))
const RegistrationPage = lazy(() => import('@/pages/patient/RegistrationPage').then((module) => ({ default: module.RegistrationPage })))
const ClinicalPage = lazy(() => import('@/pages/clinical/ClinicalPage').then((module) => ({ default: module.ClinicalPage })))

export default function App() {
  return (
    <ConfigProvider locale={zhCN} theme={{
      token: {
        colorPrimary: '#1769aa',
        colorInfo: '#1769aa',
        colorSuccess: '#2f7d4a',
        colorWarning: '#b26a00',
        colorError: '#b42318',
        colorBgLayout: '#eef1f5',
        colorBorder: '#d9dee5',
        borderRadius: 4,
        controlHeight: 34,
        fontSize: 14,
      },
      components: {
        Card: { headerBg: '#f7f8fa' },
        Menu: { darkItemBg: '#17324d', darkItemSelectedBg: '#1769aa', darkItemHoverBg: '#244866' },
        Table: { headerBg: '#f3f5f7', headerColor: '#344054', rowHoverBg: '#f5f9fd' },
      },
    }}>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route index element={<DashboardPage />} />
                <Route path="patient" element={<Suspense fallback={<Spin fullscreen tip="正在加载患者服务" />}><PatientPage /></Suspense>} />
                <Route path="patient/registrations" element={<Suspense fallback={<Spin fullscreen tip="正在加载挂号服务" />}><RegistrationPage /></Suspense>} />
                <Route path="clinical" element={<Suspense fallback={<Spin fullscreen tip="正在加载临床服务" />}><ClinicalPage /></Suspense>} />
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
