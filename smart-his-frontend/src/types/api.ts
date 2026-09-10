export interface ApiResponse<T> { code: number; message: string; data: T }
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
