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
      <header className="login-header">
        <div className="login-brand"><span className="login-brand-mark"><MedicineBoxOutlined /></span><div><strong>Smart HIS</strong><span>智慧医院信息系统</span></div></div>
        <span className="login-header-note">医院内部业务系统</span>
      </header>
      <div className="login-main">
        <section className="login-intro">
          <span className="system-label">HOSPITAL INFORMATION SYSTEM</span>
          <h1>统一诊疗工作入口</h1>
          <p>连接患者、挂号、临床、药事、资源与运营业务，为院内工作人员提供统一的日常操作入口。</p>
          <dl className="login-system-info">
            <div><dt>系统状态</dt><dd><span className="status-dot" />正常服务</dd></div>
            <div><dt>访问范围</dt><dd>院内授权用户</dd></div>
            <div><dt>技术支持</dt><dd>信息中心</dd></div>
          </dl>
        </section>
        <section className="login-panel">
          <div className="login-card">
            <div className="login-card-heading"><span>用户登录</span><h2>登录 Smart HIS</h2><p>请输入医院统一身份认证账号</p></div>
            {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 18 }} />}
            <Form<LoginRequest> layout="vertical" onFinish={handleSubmit} requiredMark={false}>
              <Form.Item name="username" label="工号或账号" rules={[{ required: true, message: '请输入账号' }]}><Input prefix={<UserOutlined />} autoComplete="username" placeholder="请输入工号或账号" /></Form.Item>
              <Form.Item name="password" label="登录密码" rules={[{ required: true, message: '请输入密码' }]}><Input.Password prefix={<LockOutlined />} autoComplete="current-password" placeholder="请输入登录密码" /></Form.Item>
              <Button block type="primary" htmlType="submit" loading={loading}>登录</Button>
            </Form>
            <div className="login-footnote"><SafetyCertificateOutlined /> 请使用本人账号，所有操作均记录审计日志</div>
          </div>
        </section>
      </div>
      <footer className="login-footer">Smart HIS · 院内系统请勿在公共设备上保存密码</footer>
    </main>
  )
}
