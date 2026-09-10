import { LockOutlined, MedicineBoxOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons'
import { Alert, Button, Form, Input } from 'antd'
import axios from 'axios'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuthStore } from '@/stores/authStore'
import type { LoginRequest } from '@/types/api'

export function LoginPage() {
  const session = useAuthStore((state) => state.session)
  const login = useAuthStore((state) => state.login)
  const loading = useAuthStore((state) => state.loading)
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState('')
  if (session) return <Navigate to="/" replace />

  const handleSubmit = async (values: LoginRequest) => {
    setError('')
    try {
      await login(values)
      const target = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname || '/'
      navigate(target, { replace: true })
    } catch (requestError) {
      const message = axios.isAxiosError(requestError) ? requestError.response?.data?.message : undefined
      setError(message || '登录失败，请检查账号、密码和服务连接。')
    }
  }

  return (
    <main className="login-shell">
      <section className="login-hero">
        <div className="brand"><span className="brand-mark"><MedicineBoxOutlined /></span>SMART HIS</div>
        <div className="hero-copy"><h1>让每一次诊疗<br />更准确、更从容</h1><p>统一连接患者、临床、药事、资源与运营数据，为医护人员提供清晰、可靠的协作工作台。</p><div className="hero-points"><span>统一身份认证</span><span>全过程审计</span><span>实时服务监测</span></div></div>
      </section>
      <section className="login-panel">
        <div className="login-card">
          <h2>欢迎回来</h2><p>请使用医院统一账号登录系统</p>
          {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 20 }} />}
          <Form<LoginRequest> layout="vertical" size="large" onFinish={handleSubmit} requiredMark={false}>
            <Form.Item name="username" label="账号" rules={[{ required: true, message: '请输入账号' }]}><Input prefix={<UserOutlined />} autoComplete="username" placeholder="请输入工号或账号" /></Form.Item>
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}><Input.Password prefix={<LockOutlined />} autoComplete="current-password" placeholder="请输入密码" /></Form.Item>
            <Button block type="primary" htmlType="submit" loading={loading}>登录系统</Button>
          </Form>
          <div className="login-footnote"><SafetyCertificateOutlined /> 连接受保护，请勿向他人透露账号凭据</div>
        </div>
      </section>
    </main>
  )
}
