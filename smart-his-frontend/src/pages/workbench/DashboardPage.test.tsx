import { describe, expect, it } from 'vitest'
import { dashboardServices, quickActions } from './dashboardConfig'

describe('dashboard quick actions', () => {
  it('links the core outpatient workflow in operational order', () => {
    expect(quickActions.map((action) => action.path)).toEqual([
      '/patient',
      '/patient/registrations',
      '/clinical',
      '/pharma',
    ])
  })

  it('monitors all active services including emergency', () => {
    expect(dashboardServices).toEqual(['auth', 'patient', 'clinical', 'operations', 'pharma', 'emergency'])
  })
})
