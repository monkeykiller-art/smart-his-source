import type { LoginResponse } from '@/types/api'

const SESSION_KEY = 'smart-his.auth'
export type AuthSession = Pick<LoginResponse, 'accessToken' | 'refreshToken' | 'userId' | 'username' | 'realName' | 'deptId' | 'deptName' | 'roles' | 'permissions'>

export const tokenStorage = {
  read(): AuthSession | null {
    const raw = sessionStorage.getItem(SESSION_KEY)
    if (!raw) return null
    try { return JSON.parse(raw) as AuthSession } catch { sessionStorage.removeItem(SESSION_KEY); return null }
  },
  write(session: AuthSession) { sessionStorage.setItem(SESSION_KEY, JSON.stringify(session)) },
  updateAccessToken(accessToken: string) {
    const session = this.read()
    if (session) this.write({ ...session, accessToken })
  },
  clear() { sessionStorage.removeItem(SESSION_KEY) },
}
