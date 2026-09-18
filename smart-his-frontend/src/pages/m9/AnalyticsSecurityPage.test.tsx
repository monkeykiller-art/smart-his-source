import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { ConfigProvider } from 'antd'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { http } from '@/services/http'
import { AnalyticsSecurityPage } from './AnalyticsSecurityPage'

vi.mock('@/services/http')
const mockedHttp = vi.mocked(http)

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <ConfigProvider theme={{ token: { motion: false } }}>
      <MemoryRouter>
        <QueryClientProvider client={client}>
          <AnalyticsSecurityPage />
        </QueryClientProvider>
      </MemoryRouter>
    </ConfigProvider>
  )
}

describe('AnalyticsSecurityPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedHttp.get.mockImplementation((url: string) => {
      if (url.includes('/operations/analytics/summary')) {
        return Promise.resolve({ data: { code: 200, data: { outpatientVisits: 150, billCount: 120, receivedAmount: 25000.50, refundedAmount: 500.00 } } })
      }
      if (url.includes('/operations/analytics/departments')) {
        return Promise.resolve({ data: { code: 200, data: [{ dept_id: 1, bill_count: 50, billed_amount: 10000, received_amount: 9500 }] } })
      }
      if (url.includes('/operations/analytics/doctors')) {
        return Promise.resolve({ data: { code: 200, data: [{ doctor_id: 101, dept_id: 1, visit_count: 30 }] } })
      }
      if (url.includes('/operations/analytics/filters')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/auth/logs/operations')) {
        return Promise.resolve({ data: { code: 200, data: { records: [{ id: 1, operationTime: '2026-09-18T10:00:00', username: 'admin', operationType: 'LOGIN', responseCode: 200, description: '登录成功' }] } } })
      }
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    mockedHttp.post.mockResolvedValue({ data: { code: 200, data: null } })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('displays summary metrics and loads default date range', async () => {
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('运营分析与安全')).toBeInTheDocument()
    }, { timeout: 5000 })
    await waitFor(() => {
      expect(mockedHttp.get).toHaveBeenCalledWith('/operations/analytics/summary', expect.any(Object))
    }, { timeout: 5000 })
  })

  it('shows department performance tab with data', async () => {
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('科室绩效')).toBeInTheDocument()
    }, { timeout: 5000 })
    fireEvent.click(screen.getByText('科室绩效'))
    await waitFor(() => {
      expect(screen.getByText('科室 ID')).toBeInTheDocument()
    }, { timeout: 5000 })
  })

  it('shows doctor workload tab with data', async () => {
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('医生工作量')).toBeInTheDocument()
    }, { timeout: 5000 })
    fireEvent.click(screen.getByText('医生工作量'))
    await waitFor(() => {
      expect(screen.getByText('医生 ID')).toBeInTheDocument()
    }, { timeout: 5000 })
  })

  it('displays audit log permission message when access denied', async () => {
    mockedHttp.get.mockImplementation((url: string) => {
      if (url.includes('/auth/logs/operations')) {
        return Promise.reject({ response: { status: 403 } })
      }
      if (url.includes('/operations/analytics/summary')) {
        return Promise.resolve({ data: { code: 200, data: { outpatientVisits: 0, billCount: 0, receivedAmount: 0, refundedAmount: 0 } } })
      }
      if (url.includes('/operations/analytics/departments')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/operations/analytics/doctors')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/operations/analytics/filters')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    renderPage()
    fireEvent.click(screen.getByText('操作审计'))
    await waitFor(() => {
      expect(screen.getByText('当前账号没有审计日志查看权限。')).toBeInTheDocument()
    }, { timeout: 5000 })
  })

  it('allows applying saved filter conditions', async () => {
    mockedHttp.get.mockImplementation((url: string) => {
      if (url.includes('/operations/analytics/filters')) {
        return Promise.resolve({ data: { code: 200, data: [{ id: 1, filterName: '本月内科', configJson: '{"from":"2026-09-01","to":"2026-09-30","deptId":1}' }] } })
      }
      if (url.includes('/operations/analytics/summary')) {
        return Promise.resolve({ data: { code: 200, data: { outpatientVisits: 0, billCount: 0, receivedAmount: 0, refundedAmount: 0 } } })
      }
      if (url.includes('/operations/analytics/departments')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/operations/analytics/doctors')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/auth/logs/operations')) {
        return Promise.resolve({ data: { code: 200, data: { records: [] } } })
      }
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('本月内科')).toBeInTheDocument()
    }, { timeout: 5000 })
    fireEvent.click(screen.getByText('本月内科'))
    await waitFor(() => {
      expect(mockedHttp.get).toHaveBeenCalledWith('/operations/analytics/summary', expect.objectContaining({ params: expect.objectContaining({ deptId: 1 }) }))
    }, { timeout: 5000 })
  })

  it('exports report in Excel and PDF formats', async () => {
    const downloadMock = vi.fn()
    mockedHttp.get.mockImplementation((url: string, config: any) => {
      if (url.includes('/operations/analytics/export')) {
        downloadMock(url, config)
        return Promise.resolve({ data: new Blob(['test']) })
      }
      if (url.includes('/operations/analytics/summary')) {
        return Promise.resolve({ data: { code: 200, data: { outpatientVisits: 0, billCount: 0, receivedAmount: 0, refundedAmount: 0 } } })
      }
      if (url.includes('/operations/analytics/departments')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/operations/analytics/doctors')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/operations/analytics/filters')) {
        return Promise.resolve({ data: { code: 200, data: [] } })
      }
      if (url.includes('/auth/logs/operations')) {
        return Promise.resolve({ data: { code: 200, data: { records: [] } } })
      }
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('导出 Excel')).toBeInTheDocument()
    }, { timeout: 5000 })
    fireEvent.click(screen.getByText('导出 Excel'))
    await waitFor(() => {
      expect(downloadMock).toHaveBeenCalledWith('/operations/analytics/export.xls', expect.objectContaining({ params: expect.objectContaining({}) }))
    }, { timeout: 5000 })
    fireEvent.click(screen.getByText('导出 PDF'))
    await waitFor(() => {
      expect(downloadMock).toHaveBeenCalledWith('/operations/analytics/export.pdf', expect.objectContaining({ params: expect.objectContaining({}) }))
    }, { timeout: 5000 })
  })

  it('shows error when saving filter fails', async () => {
    mockedHttp.post.mockRejectedValueOnce({ response: { status: 400, data: { message: 'Duplicate filter name' } } })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('保存条件')).toBeInTheDocument()
    }, { timeout: 5000 })
    fireEvent.click(screen.getByText('保存条件'))
    const input = await screen.findByPlaceholderText('例如：本月内科')
    fireEvent.change(input, { target: { value: '测试条件' } })
    fireEvent.click(screen.getByText('OK'))
    await waitFor(() => {
      expect(screen.getByText('查询条件保存失败，名称可能重复。')).toBeInTheDocument()
    }, { timeout: 5000 })
  })
})
