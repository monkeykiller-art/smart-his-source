import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse, LoginResponse } from '@/types/api'
import { tokenStorage } from './tokenStorage'

interface RetriableRequest extends InternalAxiosRequestConfig { _retry?: boolean }
export const http = axios.create({ baseURL: '/api', timeout: 12_000, headers: { 'Content-Type': 'application/json' } })

http.interceptors.request.use((config) => {
  const token = tokenStorage.read()?.accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let refreshRequest: Promise<string> | null = null
http.interceptors.response.use((response) => response, async (error: AxiosError) => {
  const request = error.config as RetriableRequest | undefined
  const session = tokenStorage.read()
  const isAuthRequest = request?.url?.startsWith('/auth/')
  if (error.response?.status !== 401 || !request || isAuthRequest) return Promise.reject(error)
  if (request._retry || !session?.refreshToken) {
    tokenStorage.clear()
    if (window.location.pathname !== '/login') window.location.assign('/login')
    return Promise.reject(error)
  }
  request._retry = true
  refreshRequest ??= axios.post<ApiResponse<LoginResponse>>('/api/auth/refresh', { refreshToken: session.refreshToken })
    .then(({ data }) => { tokenStorage.updateAccessToken(data.data.accessToken); return data.data.accessToken })
    .finally(() => { refreshRequest = null })
  try {
    request.headers.Authorization = `Bearer ${await refreshRequest}`
    return http(request)
  } catch (refreshError) {
    tokenStorage.clear()
    window.location.assign('/login')
    return Promise.reject(refreshError)
  }
})
