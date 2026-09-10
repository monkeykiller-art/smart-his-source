import { BuildOutlined } from '@ant-design/icons'
import { Card } from 'antd'

export function PlaceholderPage({ title }: { title: string }) {
  return <><div className="page-heading"><h1>{title}</h1><p>模块入口已接入统一布局和权限路由。</p></div><Card className="placeholder-card"><div><BuildOutlined style={{ fontSize: 40, color: '#288e91' }} /><h2>业务页面将在下一阶段接入</h2><p>当前 P0 先完成前端工程、身份认证和工作台基线。</p></div></Card></>
}
