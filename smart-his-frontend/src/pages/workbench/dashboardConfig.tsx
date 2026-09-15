import { CalendarOutlined, MedicineBoxOutlined, TeamOutlined } from '@ant-design/icons'

export const quickActions = [
  { label: '患者建档', description: '新建和查询患者档案', path: '/patient', icon: <TeamOutlined /> },
  { label: '门诊挂号', description: '查询号源并办理挂号', path: '/patient/registrations', icon: <CalendarOutlined /> },
  { label: '临床接诊', description: '病历、诊断与医嘱处理', path: '/clinical', icon: <MedicineBoxOutlined /> },
]

export const dashboardServices = ['auth', 'patient', 'clinical', 'operations'] as const
