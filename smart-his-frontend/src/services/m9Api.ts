import { http } from './http'
import type { ApiResponse, PageResult } from '@/types/api'

export interface AnalyticsSummary {
  outpatientVisits: number; billCount: number; billedAmount: number | string; receivedAmount: number | string
  refundedAmount: number | string; inventoryQuantity: number | string; expiringInventoryQuantity: number | string
}
export interface DepartmentPerformance { dept_id: number; bill_count: number; billed_amount: number | string; received_amount: number | string }
export interface DoctorWorkload { doctor_id: number; dept_id: number; visit_count: number }
export interface SavedReportFilter { id: number; reportCode: string; filterName: string; configJson: string; createdTime?: string }
export interface OperationLog { id: number; username?: string; operationType: string; module?: string; description?: string; responseCode?: number; operationTime: string }
export interface ReportQuery { from?: string; to?: string; deptId?: number }

export const m9Api = {
  async summary(params: ReportQuery): Promise<AnalyticsSummary> { const response = await http.get<ApiResponse<AnalyticsSummary>>('/operations/analytics/summary', { params }); return response.data.data },
  async departments(params: ReportQuery): Promise<DepartmentPerformance[]> { const response = await http.get<ApiResponse<DepartmentPerformance[]>>('/operations/analytics/departments', { params }); return response.data.data },
  async doctors(params: ReportQuery): Promise<DoctorWorkload[]> { const response = await http.get<ApiResponse<DoctorWorkload[]>>('/operations/analytics/doctors', { params }); return response.data.data },
  async filters(): Promise<SavedReportFilter[]> { const response = await http.get<ApiResponse<SavedReportFilter[]>>('/operations/analytics/filters', { params: { reportCode: 'OPERATIONS' } }); return response.data.data },
  async saveFilter(filterName: string, query: ReportQuery): Promise<SavedReportFilter> { const response = await http.post<ApiResponse<SavedReportFilter>>('/operations/analytics/filters', { reportCode: 'OPERATIONS', filterName, configJson: JSON.stringify(query) }); return response.data.data },
  async operationLogs(page = 1): Promise<PageResult<OperationLog>> { const response = await http.get<ApiResponse<PageResult<OperationLog>>>('/auth/logs/operations', { params: { page, size: 20 } }); return response.data.data },
  async setupMfa(): Promise<{ secret: string; otpauthUri: string }> { const response = await http.post<ApiResponse<{ secret: string; otpauthUri: string }>>('/auth/mfa/setup'); return response.data.data },
  async enableMfa(code: string): Promise<void> { await http.post('/auth/mfa/enable', { code }) },
  async exportReport(format: 'xls' | 'pdf', params: ReportQuery): Promise<void> {
    const response = await http.get(`/operations/analytics/export.${format}`, { params, responseType: 'blob' })
    const url = URL.createObjectURL(response.data)
    const link = document.createElement('a'); link.href = url; link.download = `smart-his-report.${format}`; link.click(); URL.revokeObjectURL(url)
  },
}
