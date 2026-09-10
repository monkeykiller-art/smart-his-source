import { CheckCircleFilled, ClockCircleOutlined, CloudServerOutlined, WarningFilled } from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import { Card, Progress, Tag } from 'antd'
import axios from 'axios'

const services = ['auth', 'patient', 'clinical', 'resource', 'operations', 'collaboration', 'pharma', 'cdss', 'drg', 'emergency', 'platform']
async function checkService(service: string) { try { await axios.get(`/api/${service}/health`, { timeout: 3500 }); return true } catch { return false } }

export function DashboardPage() {
  const health = useQuery({ queryKey: ['service-health'], queryFn: async () => Promise.all(services.map(async (service) => ({ service, online: await checkService(service) }))), refetchInterval: 30_000 })
  const online = health.data?.filter((item) => item.online).length || 0
  const percent = Math.round((online / services.length) * 100)
  return (
    <>
      <div className="page-heading"><h1>欢迎进入 Smart HIS</h1><p>集中查看核心服务运行状态和今日工作概况。</p></div>
      <section className="metric-grid">
        <Card className="metric-card"><div className="metric-label">在线服务</div><div className="metric-value">{online}/{services.length}</div><div className="metric-note">每 30 秒自动检测</div></Card>
        <Card className="metric-card"><div className="metric-label">系统可用率</div><div className="metric-value">{percent}%</div><Progress percent={percent} showInfo={false} strokeColor="#1d948c" /></Card>
        <Card className="metric-card"><div className="metric-label">待处理事项</div><div className="metric-value">—</div><div className="metric-note">业务接口接入后显示</div></Card>
        <Card className="metric-card"><div className="metric-label">最近同步</div><div className="metric-value"><ClockCircleOutlined /></div><div className="metric-note">{health.dataUpdatedAt ? new Date(health.dataUpdatedAt).toLocaleTimeString('zh-CN') : '正在检测'}</div></Card>
      </section>
      <Card className="service-card"><h2 className="section-title"><CloudServerOutlined /> 服务运行状态</h2><div className="service-grid">{services.map((service) => { const item = health.data?.find((entry) => entry.service === service); return <div className="service-row" key={service}><span className="service-name">his-{service}</span>{item?.online ? <Tag icon={<CheckCircleFilled />} color="success">运行中</Tag> : <Tag icon={<WarningFilled />} color="default">未连接</Tag>}</div> })}</div></Card>
    </>
  )
}
