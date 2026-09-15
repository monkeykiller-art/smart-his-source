import { AxiosError } from 'axios'
import { afterEach, describe, expect, it } from 'vitest'
import { http } from './http'
import { tokenStorage } from './tokenStorage'

describe('http authentication recovery', () => {
  afterEach(() => {
    tokenStorage.clear()
    window.history.replaceState({}, '', '/')
  })

  it('clears an unusable session when a protected request has no refresh token', async () => {
    window.history.replaceState({}, '', '/login')
    sessionStorage.setItem('smart-his.auth', JSON.stringify({ accessToken: 'expired-access' }))
    const originalAdapter = http.defaults.adapter
    http.defaults.adapter = async (config) => {
      throw new AxiosError('unauthorized', 'ERR_BAD_REQUEST', config, undefined, {
        status: 401,
        statusText: 'Unauthorized',
        headers: {},
        config,
        data: { code: 5004, message: 'missing or invalid authorization header', data: null },
      })
    }

    await expect(http.get('/patient/patients')).rejects.toMatchObject({ response: { status: 401 } })
    expect(tokenStorage.read()).toBeNull()
    http.defaults.adapter = originalAdapter
  })
})
