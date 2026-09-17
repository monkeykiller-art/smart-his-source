import { describe, expect, it } from 'vitest'
import { hasAnyPermission } from './authz'
import type { AuthSession } from '@/services/tokenStorage'

const session = (roles: string[], permissions: string[]): AuthSession => ({
  accessToken: 'a', refreshToken: 'r', userId: 1, username: 'doctor', realName: '医生',
  deptId: 10, deptName: '内科', roles, permissions,
})

describe('hasAnyPermission', () => {
  it('allows an exact granted permission and rejects another permission', () => {
    const doctor = session(['INPATIENT_DOCTOR'], ['resource:ward:list'])
    expect(hasAnyPermission(doctor, ['resource:ward:list'])).toBe(true)
    expect(hasAnyPermission(doctor, ['operations:bill:list'])).toBe(false)
  })

  it('allows administrators even when the permission array is empty', () => {
    expect(hasAnyPermission(session(['ADMIN'], []), ['platform:audit:list'])).toBe(true)
  })
})
