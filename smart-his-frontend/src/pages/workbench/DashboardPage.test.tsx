import { describe, expect, it } from 'vitest'
import { quickActions } from './dashboardConfig'

describe('dashboard quick actions', () => {
  it('links the core outpatient workflow in operational order', () => {
    expect(quickActions.map((action) => action.path)).toEqual([
      '/patient',
      '/patient/registrations',
      '/clinical',
    ])
  })
})
