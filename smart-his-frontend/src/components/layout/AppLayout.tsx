import { AppstoreOutlined, BankOutlined, CalendarOutlined, ExperimentOutlined, LogoutOutlined, MedicineBoxOutlined, SafetyCertificateOutlined, TeamOutlined } from '@ant-design/icons'
import { Avatar, Button, Layout, Menu, Typography } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

const { Header, Sider, Content } = Layout
const pageTitles: Record<string, string> = {
  '/': '工作台',
  '/patient': '患者服务',
  '/patient/registrations': '门诊挂号',
  '/clinical': '临床诊疗',
  '/resource': '资源保障',
  '/operations': '运营管理',
  '/pharma': '药事管理',
}
const menuItems = [
  { key: '/', icon: <AppstoreOutlined />, label: '工作台' },
  { key: '/patient', icon: <TeamOutlined />, label: '患者服务' },
  { key: '/patient/registrations', icon: <CalendarOutlined />, label: '门诊挂号' },
  { key: '/clinical', icon: <MedicineBoxOutlined />, label: '临床诊疗' },
  { key: '/resource', icon: <BankOutlined />, label: '资源保障' },
  { key: '/operations', icon: <ExperimentOutlined />, label: '运营管理' },
  { key: '/pharma', icon: <SafetyCertificateOutlined />, label: '药事管理' },
]

export function AppLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const session = useAuthStore((state) => state.session)
  const logout = useAuthStore((state) => state.logout)
  const handleLogout = async () => { await logout(); navigate('/login', { replace: true }) }
  const pageTitle = pageTitles[location.pathname] || '工作台'

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
            <Button type="text" icon={<LogoutOutlined />} onClick={handleLogout}>退出</Button>
          </div>
        </Header>
        <Content className="app-content"><Outlet /></Content>
      </Layout>
    </Layout>
  )
}
