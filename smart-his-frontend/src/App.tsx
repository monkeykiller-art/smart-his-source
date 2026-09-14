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
const OperationsPage = lazy(() => import('@/pages/operations/OperationsPage').then((module) => ({ default: module.OperationsPage })))

export default function App() {
  return (
    <ConfigProvider locale={zhCN} theme={{
      token: {
        colorPrimary: '#285b88',
        colorInfo: '#285b88',
        colorSuccess: '#347145',
        colorWarning: '#a56b16',
        colorError: '#a12b2b',
        colorBgLayout: '#d8e0e8',
        colorBorder: '#aebdcb',
        borderRadius: 1,
        controlHeight: 30,
        fontSize: 13,
      },
      components: {
        Card: { headerBg: '#dce6f0' },
        Menu: { darkItemBg: '#244765', darkItemSelectedBg: '#3a76a7', darkItemHoverBg: '#315c7e' },
        Table: { headerBg: '#dce6f0', headerColor: '#263e54', rowHoverBg: '#edf4fb' },
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
                <Route path="operations" element={<Suspense fallback={<Spin fullscreen tip="正在加载运营服务" />}><OperationsPage /></Suspense>} />
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
