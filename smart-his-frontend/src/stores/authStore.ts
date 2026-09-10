import { create } from 'zustand'
import { authApi } from '@/services/authApi'
import { tokenStorage, type AuthSession } from '@/services/tokenStorage'
import type { LoginRequest } from '@/types/api'

interface AuthState {
  session: AuthSession | null
  loading: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set, get) => ({
  session: tokenStorage.read(),
  loading: false,
  async login(request) {
    set({ loading: true })
    try {
      const session: AuthSession = await authApi.login(request)
      tokenStorage.write(session)
      set({ session })
    } finally { set({ loading: false }) }
  },
  async logout() {
    const refreshToken = get().session?.refreshToken
    tokenStorage.clear()
    set({ session: null })
    try { await authApi.logout(refreshToken) } catch { /* Local logout is already complete. */ }
  },
}))
