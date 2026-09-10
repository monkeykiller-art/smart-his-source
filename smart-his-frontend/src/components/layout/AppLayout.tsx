import { AppstoreOutlined, BankOutlined, ExperimentOutlined, LogoutOutlined, MedicineBoxOutlined, SafetyCertificateOutlined, TeamOutlined } from '@ant-design/icons'
import { Avatar, Button, Layout, Menu, Typography } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

const { Header, Sider, Content } = Layout
const menuItems = [
  { key: '/', icon: <AppstoreOutlined />, label: '工作台' },
  { key: '/patient', icon: <TeamOutlined />, label: '患者服务' },
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

  return (
    <Layout className="app-shell">
      <Sider width={224} className="app-sider">
        <div className="sider-brand"><span className="brand-mark"><MedicineBoxOutlined /></span><span>Smart HIS</span></div>
        <Menu theme="dark" mode="inline" items={menuItems} selectedKeys={[location.pathname]} onClick={({ key }) => navigate(key)} />
      </Sider>
      <Layout className="app-main">
        <Header className="app-header">
          <Typography.Text className="header-title">智慧医院工作台</Typography.Text>
          <div className="header-user">
            <Avatar>{session?.realName?.slice(0, 1) || session?.username.slice(0, 1)}</Avatar>
            <div><div>{session?.realName || session?.username}</div><Typography.Text type="secondary">{session?.deptName || '未分配科室'}</Typography.Text></div>
            <Button type="text" icon={<LogoutOutlined />} onClick={handleLogout}>退出</Button>
          </div>
        </Header>
        <Content className="app-content"><Outlet /></Content>
      </Layout>
    </Layout>
  )
}
