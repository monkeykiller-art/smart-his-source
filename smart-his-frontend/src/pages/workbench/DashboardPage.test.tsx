import { describe, expect, it } from 'vitest'
import { dashboardServices, quickActions } from './dashboardConfig'

describe('dashboard quick actions', () => {
  it('links the core outpatient workflow in operational order', () => {
    expect(quickActions.map((action) => action.path)).toEqual([
      '/patient',
      '/patient/registrations',
      '/clinical',
    ])
  })

  it('monitors only services required by the basic outpatient workflow', () => {
    expect(dashboardServices).toEqual(['auth', 'patient', 'clinical', 'operations'])
  })
})
