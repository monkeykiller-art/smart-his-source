import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { m9Api } from './m9Api'

vi.mock('./http', () => ({ http: { get: vi.fn(), post: vi.fn() } }))

describe('m9Api', () => {
  beforeEach(() => vi.clearAllMocks())
  it('loads scoped analytics and saves serialized report filters', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { data: { outpatientVisits: 3, billCount: 2 } } })
    vi.mocked(http.post).mockResolvedValue({ data: { data: { id: 1 } } })
    await m9Api.summary({ from: '2026-09-01', to: '2026-09-17', deptId: 10 })
    await m9Api.saveFilter('本月', { from: '2026-09-01', to: '2026-09-17' })
    expect(http.get).toHaveBeenCalledWith('/operations/analytics/summary', { params: { from: '2026-09-01', to: '2026-09-17', deptId: 10 } })
    expect(http.post).toHaveBeenCalledWith('/operations/analytics/filters', expect.objectContaining({ filterName: '本月', configJson: expect.any(String) }))
  })
})
