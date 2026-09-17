import { describe, expect, it } from 'vitest'
import { tokenStorage, type AuthSession } from './tokenStorage'

const session: AuthSession = { accessToken: 'access', refreshToken: 'refresh', userId: 1, username: 'doctor', realName: '张医生', deptId: 2, deptName: '内科', roles: ['DOCTOR'], permissions: ['patient:read'] }
describe('tokenStorage', () => {
  it('stores and updates a session', () => { tokenStorage.write(session); tokenStorage.updateAccessToken('new-access'); expect(tokenStorage.read()).toEqual({ ...session, accessToken: 'new-access' }) })
  it('clears malformed data', () => { sessionStorage.setItem('smart-his.auth', '{bad json'); expect(tokenStorage.read()).toBeNull(); expect(sessionStorage.length).toBe(0) })
})
