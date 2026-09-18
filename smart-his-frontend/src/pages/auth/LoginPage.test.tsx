import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { ConfigProvider } from 'antd'
import { AxiosError, AxiosHeaders } from 'axios'
import { MemoryRouter, Route, Routes, useLocation, useNavigationType } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { authApi } from '@/services/authApi'
import { tokenStorage } from '@/services/tokenStorage'
import { useAuthStore } from '@/stores/authStore'
import type { LoginResponse } from '@/types/api'
import { LoginPage } from './LoginPage'

vi.mock('@/services/authApi', () => ({ authApi: { login: vi.fn(), logout: vi.fn() } }))

const session: LoginResponse = {
  accessToken: 'test-access', refreshToken: 'test-refresh', expiresIn: 3600,
  userId: 8, username: 'doctor', realName: '张医生', deptId: 2, deptName: '内科',
  roles: ['DOCTOR'], permissions: ['patient:read'],
}
const credentials = { username: 'doctor', password: 'test-password' }
const fallbackMessage = '登录失败，请检查账号、密码和服务连接。'
const testTimeout = 15_000
const waitOptions = { timeout: 5_000 }

function resetStore() {
  useAuthStore.setState({ ...useAuthStore.getInitialState(), session: null, loading: false }, true)
  tokenStorage.clear()
}

function NavigationState() {
  const location = useLocation()
  const navigationType = useNavigationType()
  return <><output aria-label="当前路径">{location.pathname}</output><output aria-label="导航方式">{navigationType}</output></>
}

function renderPage(state?: { from?: { pathname?: string } }) {
  // App uses BrowserRouter; keep the same declarative routing mode in these tests.
  render(
    <ConfigProvider theme={{ token: { motion: false } }}>
      <MemoryRouter initialEntries={[{ pathname: '/login', state }]}>
        <NavigationState />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<h1>工作台首页</h1>} />
          <Route path="/patient" element={<h1>患者管理</h1>} />
        </Routes>
      </MemoryRouter>
    </ConfigProvider>,
  )
}

function fillCredentials(username = credentials.username, password = credentials.password) {
  fireEvent.change(screen.getByLabelText('工号或账号'), { target: { value: username } })
  fireEvent.change(screen.getByLabelText('登录密码'), { target: { value: password } })
}

async function submit() {
  await act(async () => {
    fireEvent.click(screen.getByRole('button', { name: /登\s*录/ }))
  })
}

function axiosFailure(message?: string) {
  const config = { headers: new AxiosHeaders() }
  return new AxiosError('Request failed with status code 401', AxiosError.ERR_BAD_REQUEST, config, undefined, {
    config, status: 401, statusText: 'Unauthorized', headers: {}, data: { message },
  })
}

describe.sequential('LoginPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    resetStore()
    vi.mocked(authApi.login).mockResolvedValue(session)
  })

  afterEach(() => {
    cleanup()
    resetStore()
    vi.resetAllMocks()
  })

  it.each([
    { name: 'both credentials', username: '', password: '', messages: ['请输入账号', '请输入密码'] },
    { name: 'the account', username: '', password: credentials.password, messages: ['请输入账号'] },
    { name: 'the password', username: credentials.username, password: '', messages: ['请输入密码'] },
  ])('validates missing $name without attempting login', async ({ username, password, messages }) => {
    renderPage()
    fillCredentials(username, password)

    await submit()

    for (const message of messages) {
      expect(await screen.findByText(message, {}, waitOptions)).toBeVisible()
    }
    expect(authApi.login).not.toHaveBeenCalled()
    expect(useAuthStore.getState()).toMatchObject({ session: null, loading: false })
    expect(screen.getByLabelText('当前路径').textContent).toBe('/login')
  }, testTimeout)

  it.each([
    { name: 'without optional OTP', otpCode: undefined, state: undefined, target: '/', heading: '工作台首页' },
    { name: 'with OTP and a return location', otpCode: '012345', state: { from: { pathname: '/patient' } }, target: '/patient', heading: '患者管理' },
    { name: 'with a return location lacking a pathname', otpCode: undefined, state: { from: {} }, target: '/', heading: '工作台首页' },
  ])('logs in $name and replaces the login route', async ({ otpCode, state, target, heading }) => {
    renderPage(state)
    fillCredentials()
    const otpInput = screen.getByLabelText('动态验证码（已启用二次认证时填写）')
    expect(otpInput).toHaveAttribute('inputmode', 'numeric')
    expect(otpInput).toHaveAttribute('maxlength', '6')
    if (otpCode !== undefined) fireEvent.change(otpInput, { target: { value: otpCode } })

    await submit()

    expect(await screen.findByRole('heading', { name: heading }, waitOptions)).toBeVisible()
    expect(authApi.login).toHaveBeenCalledExactlyOnceWith({ ...credentials, otpCode })
    expect(useAuthStore.getState()).toMatchObject({ session, loading: false })
    expect(tokenStorage.read()).toEqual(session)
    expect(screen.getByLabelText('当前路径').textContent).toBe(target)
    expect(screen.getByLabelText('导航方式')).toHaveTextContent('REPLACE')
  }, testTimeout)

  it.each([
    { name: 'an Axios response message', failure: axiosFailure('账号或密码错误'), message: '账号或密码错误' },
    { name: 'an Axios response without a message', failure: axiosFailure(), message: fallbackMessage },
    { name: 'an empty Axios response message', failure: axiosFailure(''), message: fallbackMessage },
    { name: 'an Axios network error', failure: new AxiosError('Network Error', AxiosError.ERR_NETWORK), message: fallbackMessage },
    { name: 'a non-Axios error', failure: new Error('Internal implementation detail'), message: fallbackMessage },
  ])('shows the appropriate failure hint for $name and retains the form', async ({ failure, message }) => {
    vi.mocked(authApi.login).mockRejectedValueOnce(failure)
    renderPage({ from: { pathname: '/patient' } })
    fillCredentials()
    fireEvent.change(screen.getByLabelText('动态验证码（已启用二次认证时填写）'), { target: { value: '012345' } })

    await submit()

    expect(await screen.findByRole('alert', {}, waitOptions)).toHaveTextContent(message)
    expect(screen.getByLabelText('工号或账号')).toHaveValue(credentials.username)
    expect(screen.getByLabelText('登录密码')).toHaveValue(credentials.password)
    expect(screen.getByLabelText('动态验证码（已启用二次认证时填写）')).toHaveValue('012345')
    expect(authApi.login).toHaveBeenCalledExactlyOnceWith({ ...credentials, otpCode: '012345' })
    expect(useAuthStore.getState()).toMatchObject({ session: null, loading: false })
    expect(tokenStorage.read()).toBeNull()
    expect(screen.getByLabelText('当前路径').textContent).toBe('/login')
  }, testTimeout)

  it('clears a previous error while retrying, prevents duplicate submission and navigates after success', async () => {
    let resolveLogin!: (value: LoginResponse) => void
    const pendingLogin = new Promise<LoginResponse>((resolve) => { resolveLogin = resolve })
    vi.mocked(authApi.login).mockRejectedValueOnce(axiosFailure('动态验证码错误')).mockReturnValueOnce(pendingLogin)
    renderPage({ from: { pathname: '/patient' } })
    fillCredentials()
    fireEvent.change(screen.getByLabelText('动态验证码（已启用二次认证时填写）'), { target: { value: '111111' } })

    try {
      await submit()
      expect(await screen.findByRole('alert', {}, waitOptions)).toHaveTextContent('动态验证码错误')
      fireEvent.change(screen.getByLabelText('动态验证码（已启用二次认证时填写）'), { target: { value: '012345' } })
      await submit()

      await waitFor(() => {
        expect(authApi.login).toHaveBeenCalledTimes(2)
        expect(screen.queryByRole('alert')).not.toBeInTheDocument()
        expect(screen.getByRole('button', { name: /登\s*录/ })).toHaveClass('ant-btn-loading')
      }, waitOptions)
      expect(useAuthStore.getState()).toMatchObject({ session: null, loading: true })
      expect(screen.getByLabelText('当前路径').textContent).toBe('/login')
      await submit()
      expect(authApi.login).toHaveBeenCalledTimes(2)
      expect(authApi.login).toHaveBeenLastCalledWith({ ...credentials, otpCode: '012345' })
    } finally {
      // Settle the deferred request even if an assertion fails, before resetting the store.
      await act(async () => { resolveLogin(session); await pendingLogin })
    }

    expect(await screen.findByRole('heading', { name: '患者管理' }, waitOptions)).toBeVisible()
    expect(useAuthStore.getState()).toMatchObject({ session, loading: false })
    expect(screen.getByLabelText('当前路径').textContent).toBe('/patient')
    expect(screen.getByLabelText('导航方式')).toHaveTextContent('REPLACE')
  }, testTimeout)

  it('redirects an already authenticated user to the home page without logging in again', async () => {
    useAuthStore.setState({ session })
    renderPage({ from: { pathname: '/patient' } })

    expect(await screen.findByRole('heading', { name: '工作台首页' }, waitOptions)).toBeVisible()
    expect(screen.queryByLabelText('工号或账号')).not.toBeInTheDocument()
    expect(authApi.login).not.toHaveBeenCalled()
    expect(screen.getByLabelText('当前路径').textContent).toBe('/')
    expect(screen.getByLabelText('导航方式')).toHaveTextContent('REPLACE')
  }, testTimeout)
})
