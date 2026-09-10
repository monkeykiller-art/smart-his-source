import { http } from './http'
import type { ApiResponse, LoginRequest, LoginResponse } from '@/types/api'

export const authApi = {
  async login(request: LoginRequest) {
    const response = await http.post<ApiResponse<LoginResponse>>('/auth/login', request)
    return response.data.data
  },
  async logout(refreshToken?: string) { await http.post('/auth/logout', { refreshToken }) },
}
