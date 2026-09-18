import { AxiosError, type AxiosAdapter, type InternalAxiosRequestConfig } from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { LoginRequest, LoginResponse } from '@/types/api'
import { authApi } from './authApi'
import { http } from './http'
import { tokenStorage } from './tokenStorage'

const session: LoginResponse = {
  accessToken: 'test-access', refreshToken: 'test-refresh', expiresIn: 3600,
  userId: 8, username: 'doctor', realName: '张医生', deptId: 2, deptName: '内科',
  roles: ['DOCTOR'], permissions: ['patient:read'],
}
const credentials = { username: 'doctor', password: 'test-password' }
const originalAdapter = http.defaults.adapter
const transport = vi.fn<AxiosAdapter>()
const testTimeout = 10_000

function response(config: InternalAxiosRequestConfig, data: unknown) {
  return {
    config, status: 200, statusText: 'OK', headers: {},
    data: JSON.stringify({ code: 200, message: '成功', data }),
  }
}

function expectPost(url: string, body: unknown) {
  expect(transport).toHaveBeenCalledTimes(1)
  const [config] = transport.mock.calls[0]
  expect(config).toMatchObject({ baseURL: '/api', method: 'post', url })
  expect(JSON.parse(config.data)).toEqual(body)
}

describe.sequential('authApi', () => {
  beforeEach(() => {
    transport.mockReset()
    tokenStorage.clear()
    http.defaults.adapter = transport
  })

  afterEach(() => {
    http.defaults.adapter = originalAdapter
    transport.mockReset()
    tokenStorage.clear()
  })

  it.each<{ name: string; request: LoginRequest }>([
    { name: 'without optional OTP', request: credentials },
    { name: 'with a leading-zero OTP', request: { ...credentials, otpCode: '012345' } },
  ])('posts login $name and unwraps the response data', async ({ request }) => {
    transport.mockImplementation(async (config) => response(config, session))

    await expect(authApi.login(request)).resolves.toEqual(session)

    expectPost('/auth/login', request)
    expect(tokenStorage.read()).toBeNull()
  }, testTimeout)

  it('propagates a rejected login without retrying or clearing an existing session', async () => {
    tokenStorage.write(session)
    const failure = new AxiosError('Request failed with status code 401', AxiosError.ERR_BAD_REQUEST)
    transport.mockImplementation(async (config) => {
      failure.config = config
      failure.response = {
        config, status: 401, statusText: 'Unauthorized', headers: {},
        data: { code: 401, message: '账号或密码错误', data: null },
      }
      throw failure
    })

    await expect(authApi.login(credentials)).rejects.toBe(failure)

    expectPost('/auth/login', credentials)
    expect(tokenStorage.read()).toEqual(session)
  }, testTimeout)

  it('propagates a login network failure', async () => {
    const failure = new AxiosError('Network Error', AxiosError.ERR_NETWORK)
    transport.mockRejectedValue(failure)

    await expect(authApi.login(credentials)).rejects.toBe(failure)

    expectPost('/auth/login', credentials)
  }, testTimeout)

  it.each([
    { name: 'with a refresh token', refreshToken: 'test-refresh', body: { refreshToken: 'test-refresh' } },
    { name: 'without a refresh token', refreshToken: undefined, body: {} },
  ])('posts logout $name and returns no data', async ({ refreshToken, body }) => {
    transport.mockImplementation(async (config) => response(config, { ignored: true }))

    await expect(authApi.logout(refreshToken)).resolves.toBeUndefined()

    expectPost('/auth/logout', body)
  }, testTimeout)

  it('propagates a logout failure to its caller', async () => {
    const failure = new AxiosError('Network Error', AxiosError.ERR_NETWORK)
    transport.mockRejectedValue(failure)

    await expect(authApi.logout('test-refresh')).rejects.toBe(failure)

    expectPost('/auth/logout', { refreshToken: 'test-refresh' })
  }, testTimeout)
})
