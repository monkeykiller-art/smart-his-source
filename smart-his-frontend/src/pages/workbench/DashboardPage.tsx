import { CheckCircleFilled, CloudServerOutlined, WarningFilled } from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import { Button, Card, Progress, Tag } from 'antd'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { dashboardServices, quickActions } from './dashboardConfig'

const serviceNames: Record<(typeof dashboardServices)[number], string> = { auth: '统一认证', patient: '患者服务', clinical: '临床服务', operations: '运营管理', pharma: '药房库存' }
async function checkService(service: string) { try { await axios.get(`/api/${service}/health`, { timeout: 3500 }); return true } catch { return false } }

export function DashboardPage() {
  const navigate = useNavigate()
  const health = useQuery({ queryKey: ['service-health'], queryFn: async () => Promise.all(dashboardServices.map(async (service) => ({ service, online: await checkService(service) }))), refetchInterval: 30_000 })
  const online = health.data?.filter((item) => item.online).length || 0
  const percent = Math.round((online / dashboardServices.length) * 100)
  return (
    <>
      <div className="page-heading"><h1>工作台</h1><p>查看系统运行状态并进入常用业务。</p></div>
      <section className="quick-entry-grid" aria-label="常用业务">
        {quickActions.map((action) => <button type="button" className="quick-entry" key={action.path} onClick={() => navigate(action.path)}><span className="quick-entry-icon">{action.icon}</span><span><strong>{action.label}</strong><small>{action.description}</small></span><span className="quick-entry-arrow">›</span></button>)}
      </section>
      <h2 className="content-section-title">运行概况</h2>
      <section className="metric-grid">
        <Card size="small" className="metric-card"><div className="metric-label">在线服务</div><div className="metric-value">{online}<small> / {dashboardServices.length}</small></div><div className="metric-note">每 30 秒自动检测</div></Card>
        <Card size="small" className="metric-card"><div className="metric-label">服务可用率</div><div className="metric-value">{percent}<small>%</small></div><Progress percent={percent} showInfo={false} strokeColor="#1769aa" size="small" /></Card>
        <Card size="small" className="metric-card"><div className="metric-label">异常服务</div><div className="metric-value metric-value-warning">{dashboardServices.length - online}<small> 项</small></div><div className="metric-note">请检查未连接服务</div></Card>
        <Card size="small" className="metric-card"><div className="metric-label">最近检测</div><div className="metric-time">{health.dataUpdatedAt ? new Date(health.dataUpdatedAt).toLocaleTimeString('zh-CN', { hour12: false }) : '检测中'}</div><div className="metric-note"><Button type="link" size="small" onClick={() => health.refetch()}>立即刷新</Button></div></Card>
      </section>
      <Card size="small" className="service-card" title={<span><CloudServerOutlined /> 服务运行状态</span>}><div className="service-grid">{dashboardServices.map((service) => { const item = health.data?.find((entry) => entry.service === service); return <div className="service-row" key={service}><span><strong>{serviceNames[service]}</strong><small>his-{service}</small></span>{item?.online ? <Tag icon={<CheckCircleFilled />} color="success">运行中</Tag> : <Tag icon={<WarningFilled />}>未连接</Tag>}</div> })}</div></Card>
    </>
  )
}
