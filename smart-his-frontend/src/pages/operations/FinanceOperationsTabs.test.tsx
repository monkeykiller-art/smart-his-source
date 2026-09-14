import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { operationsApi } from '@/services/operationsApi'
import { useAuthStore } from '@/stores/authStore'
import { FinanceOperationsTabs } from './FinanceOperationsTabs'

vi.mock('@/services/operationsApi', () => ({ operationsApi: {
  queryAccounts: vi.fn(), generateAccount: vi.fn(), submitAccount: vi.fn(), receiveAccount: vi.fn(),
  querySettlements: vi.fn(), previewSettlement: vi.fn(), createSettlement: vi.fn(),
} }))

const emptyPage = { records: [], total: 0, page: 1, size: 20, totalPages: 0 }

describe('FinanceOperationsTabs', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    useAuthStore.setState({ session: { accessToken: 'access', refreshToken: 'refresh', userId: 8, username: 'cashier8', realName: '张收费员', deptId: 1, deptName: '收费处', roles: ['CASHIER'] } })
    vi.mocked(operationsApi.queryAccounts).mockResolvedValue({ ...emptyPage, records: [{ id: 5, accountNo: 'DZ001', cashierId: '8', cashierName: '张收费员', totalAmount: 0, cashAmount: 0, posAmount: 0, otherAmount: 0, billCount: 0, accountDate: '2026-09-14', accountStatus: 'PENDING' }] })
    vi.mocked(operationsApi.querySettlements).mockResolvedValue(emptyPage)
    vi.mocked(operationsApi.submitAccount).mockResolvedValue({ id: 5, accountNo: 'DZ001', cashierId: '8', cashierName: '张收费员', totalAmount: 0, cashAmount: 0, posAmount: 0, otherAmount: 0, billCount: 0, accountDate: '2026-09-14', accountStatus: 'SUBMITTED' })
    vi.mocked(operationsApi.previewSettlement).mockResolvedValue({ patientId: 12, admissionId: 44, totalAmount: 100, insuranceAmount: 20, depositAmount: 30, selfPayAmount: 50, depositBalance: 60 })
    vi.mocked(operationsApi.createSettlement).mockResolvedValue({ id: 9, settleNo: 'JS001', patientId: 12, admissionId: 44, totalAmount: 100, insuranceAmount: 20, depositAmount: 30, selfPayAmount: 50, settleStatus: 'SETTLED' })
  })

  it('submits a pending cashier account and refreshes the account list', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}><FinanceOperationsTabs /></QueryClientProvider>)

    fireEvent.click(await screen.findByRole('button', { name: '提交交班' }))
    await waitFor(() => expect(operationsApi.submitAccount).toHaveBeenCalledWith(5))
  })

  it('previews an inpatient stay and submits the server-resolved patient id', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}><FinanceOperationsTabs /></QueryClientProvider>)
    fireEvent.click(screen.getByRole('tab', { name: '住院结算' }))

    fireEvent.change(screen.getByRole('spinbutton', { name: '住院号' }), { target: { value: '44' } })
    fireEvent.click(screen.getByRole('button', { name: '费用预览并结算' }))
    expect(await screen.findByText('费用合计：¥100.00')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: '确认结算' }))

    await waitFor(() => expect(operationsApi.createSettlement).toHaveBeenCalledWith(expect.objectContaining({ patientId: 12, admissionId: 44, cashierId: '8' })))
  })
})
