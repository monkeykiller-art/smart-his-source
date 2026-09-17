import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ConfigProvider } from 'antd'
import { Spin } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { lazy, Suspense } from 'react'
import { AppLayout } from '@/components/layout/AppLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { DashboardPage } from '@/pages/workbench/DashboardPage'
import { ProtectedRoute } from '@/router/ProtectedRoute'
import { PermissionGuard } from '@/router/PermissionGuard'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
})
const PatientPage = lazy(() => import('@/pages/patient/PatientPage').then((module) => ({ default: module.PatientPage })))
const RegistrationPage = lazy(() => import('@/pages/patient/RegistrationPage').then((module) => ({ default: module.RegistrationPage })))
const ClinicalPage = lazy(() => import('@/pages/clinical/ClinicalPage').then((module) => ({ default: module.ClinicalPage })))
const OperationsPage = lazy(() => import('@/pages/operations/OperationsPage').then((module) => ({ default: module.OperationsPage })))
const PharmaPage = lazy(() => import('@/pages/pharma/PharmaPage').then((module) => ({ default: module.PharmaPage })))
const InpatientEmergencyPage = lazy(() => import('@/pages/m8/InpatientEmergencyPage').then((module) => ({ default: module.InpatientEmergencyPage })))
const AnalyticsSecurityPage = lazy(() => import('@/pages/m9/AnalyticsSecurityPage').then((module) => ({ default: module.AnalyticsSecurityPage })))

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
                <Route path="patient" element={<PermissionGuard anyOf={['patient:patient:list', 'patient:patient:read']}><Suspense fallback={<Spin fullscreen tip="正在加载患者服务" />}><PatientPage /></Suspense></PermissionGuard>} />
                <Route path="patient/registrations" element={<PermissionGuard anyOf={['patient:registration:list']}><Suspense fallback={<Spin fullscreen tip="正在加载挂号服务" />}><RegistrationPage /></Suspense></PermissionGuard>} />
                <Route path="clinical" element={<PermissionGuard anyOf={['clinical:record:read']}><Suspense fallback={<Spin fullscreen tip="正在加载临床服务" />}><ClinicalPage /></Suspense></PermissionGuard>} />
                <Route path="operations" element={<PermissionGuard anyOf={['operations:bill:list']}><Suspense fallback={<Spin fullscreen tip="正在加载运营服务" />}><OperationsPage /></Suspense></PermissionGuard>} />
                <Route path="pharma" element={<PermissionGuard anyOf={['resource:drug:list', 'pharma:review:list']}><Suspense fallback={<Spin fullscreen tip="正在加载药房服务" />}><PharmaPage /></Suspense></PermissionGuard>} />
                <Route path="inpatient-emergency" element={<PermissionGuard anyOf={['resource:ward:list', 'emergency:triage:list']}><Suspense fallback={<Spin fullscreen tip="正在加载住院与急诊" />}><InpatientEmergencyPage /></Suspense></PermissionGuard>} />
                <Route path="analytics" element={<PermissionGuard anyOf={['operations:revenue:stat', 'operations:workload:stat', 'auth:log:list']}><Suspense fallback={<Spin fullscreen tip="正在加载运营分析" />}><AnalyticsSecurityPage /></Suspense></PermissionGuard>} />
              </Route>
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </QueryClientProvider>
    </ConfigProvider>
  )
}
