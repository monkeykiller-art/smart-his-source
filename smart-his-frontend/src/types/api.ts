export interface ApiResponse<T> { code: number; message: string; data: T }
export interface PageResult<T> { records: T[]; total: number; page: number; size: number; totalPages: number }
export interface LoginRequest { username: string; password: string }
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userId: number
  username: string
  realName: string
  deptId: number
  deptName: string
  roles: string[]
}
