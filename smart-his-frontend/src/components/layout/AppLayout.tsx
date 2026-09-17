import { AppstoreOutlined, BarChartOutlined, CalendarOutlined, ExperimentOutlined, LogoutOutlined, MedicineBoxOutlined, TeamOutlined, AlertOutlined } from '@ant-design/icons'
import { Alert, Avatar, Button, Input, Layout, Menu, Modal, Space, Typography } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { hasAnyPermission } from '@/security/authz'
import { m9Api } from '@/services/m9Api'

const { Header, Sider, Content } = Layout
const pageTitles: Record<string, string> = {
  '/': '工作台',
  '/patient': '患者服务',
  '/patient/registrations': '门诊挂号',
  '/clinical': '临床诊疗',
  '/operations': '运营管理',
  '/pharma': '药房与库存',
  '/inpatient-emergency': '住院与急诊',
  '/analytics': '运营分析与安全',
}
const menuDefinitions = [
  { key: '/', icon: <AppstoreOutlined />, label: '工作台', permissions: [] },
  { key: '/patient', icon: <TeamOutlined />, label: '患者服务', permissions: ['patient:patient:list', 'patient:patient:read'] },
  { key: '/patient/registrations', icon: <CalendarOutlined />, label: '门诊挂号', permissions: ['patient:registration:list'] },
  { key: '/clinical', icon: <MedicineBoxOutlined />, label: '临床诊疗', permissions: ['clinical:record:read'] },
  { key: '/operations', icon: <ExperimentOutlined />, label: '运营管理', permissions: ['operations:bill:list'] },
  { key: '/pharma', icon: <MedicineBoxOutlined />, label: '药房与库存', permissions: ['resource:drug:list', 'pharma:review:list'] },
  { key: '/inpatient-emergency', icon: <AlertOutlined />, label: '住院与急诊', permissions: ['resource:ward:list', 'emergency:triage:list'] },
  { key: '/analytics', icon: <BarChartOutlined />, label: '运营分析', permissions: ['operations:revenue:stat', 'operations:workload:stat', 'auth:log:list'] },
]

export function AppLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const session = useAuthStore((state) => state.session)
  const logout = useAuthStore((state) => state.logout)
  const [mfaOpen, setMfaOpen] = useState(false)
  const [mfaSetup, setMfaSetup] = useState<{ secret: string; otpauthUri: string }>()
  const [mfaCode, setMfaCode] = useState('')
  const [mfaMessage, setMfaMessage] = useState('')
  const handleLogout = async () => { await logout(); navigate('/login', { replace: true }) }
  const pageTitle = pageTitles[location.pathname] || '工作台'
  const menuItems = menuDefinitions.filter(item => item.permissions.length === 0 || hasAnyPermission(session, item.permissions))
  const setupMfa = async () => { setMfaMessage(''); try { setMfaSetup(await m9Api.setupMfa()) } catch { setMfaMessage('二次认证初始化失败，请检查认证服务。') } }
  const enableMfa = async () => { try { await m9Api.enableMfa(mfaCode); setMfaMessage('二次认证已启用，下次登录需填写动态验证码。') } catch { setMfaMessage('验证码无效，请使用认证器当前显示的 6 位数字。') } }

  return (
    <Layout className="app-shell">
      <Sider width={224} className="app-sider">
        <div className="sider-brand"><span className="brand-mark"><MedicineBoxOutlined /></span><div><strong>Smart HIS</strong><small>智慧医院信息系统</small></div></div>
        <div className="sider-section-title">业务导航</div>
        <Menu theme="dark" mode="inline" items={menuItems} selectedKeys={[location.pathname]} onClick={({ key }) => navigate(key)} />
        <div className="sider-footer">院内工作站 · v0.1</div>
      </Sider>
      <Layout className="app-main">
        <Header className="app-header">
          <div className="header-context"><Typography.Text className="header-title">{pageTitle}</Typography.Text><Typography.Text type="secondary">门诊工作站</Typography.Text></div>
          <div className="header-user">
            <Avatar size={30}>{session?.realName?.slice(0, 1) || session?.username.slice(0, 1)}</Avatar>
            <div className="header-user-copy"><div>{session?.realName || session?.username}</div><Typography.Text type="secondary">{session?.deptName || '未分配科室'}</Typography.Text></div>
            <Button type="text" onClick={() => setMfaOpen(true)}>安全设置</Button>
            <Button type="text" icon={<LogoutOutlined />} onClick={handleLogout}>退出</Button>
          </div>
        </Header>
        <Content className="app-content"><Outlet /></Content>
      </Layout>
      <Modal title="二次认证设置" open={mfaOpen} onCancel={() => setMfaOpen(false)} footer={null}>
        {mfaMessage && <Alert type={mfaMessage.includes('已启用') ? 'success' : 'warning'} showIcon message={mfaMessage} style={{ marginBottom: 12 }} />}
        {!mfaSetup ? <Button type="primary" onClick={setupMfa}>生成认证器密钥</Button> : <Space direction="vertical" style={{ width: '100%' }}>
          <Typography.Text>在 Microsoft Authenticator、Google Authenticator 等应用中添加以下密钥：</Typography.Text>
          <Input value={mfaSetup.secret} readOnly />
          <Typography.Text copyable={{ text: mfaSetup.otpauthUri }}>复制完整 otpauth 配置</Typography.Text>
          <Input value={mfaCode} onChange={event => setMfaCode(event.target.value.replace(/\D/g, '').slice(0, 6))} placeholder="输入认证器中的 6 位验证码" />
          <Button type="primary" disabled={mfaCode.length !== 6} onClick={enableMfa}>验证并启用</Button>
        </Space>}
      </Modal>
    </Layout>
  )
}
