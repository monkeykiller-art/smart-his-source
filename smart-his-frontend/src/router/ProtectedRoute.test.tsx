import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it } from 'vitest'
import { useAuthStore } from '@/stores/authStore'
import { ProtectedRoute } from './ProtectedRoute'

describe('ProtectedRoute', () => {
  beforeEach(() => useAuthStore.setState({ session: null, loading: false }))
  it('redirects anonymous users', () => { render(<MemoryRouter initialEntries={['/patient']}><Routes><Route path="/login" element={<div>登录页</div>} /><Route element={<ProtectedRoute />}><Route path="/patient" element={<div>患者页</div>} /></Route></Routes></MemoryRouter>); expect(screen.getByText('登录页')).toBeInTheDocument() })
  it('renders content for an authenticated session', () => { const session = { accessToken: 'a', refreshToken: 'r', userId: 1, username: 'doctor', realName: '张医生', deptId: 2, deptName: '内科', roles: ['DOCTOR'], permissions: ['patient:read'] }; useAuthStore.setState({ session }); render(<MemoryRouter initialEntries={['/patient']}><Routes><Route element={<ProtectedRoute />}><Route path="/patient" element={<div>患者页</div>} /></Route></Routes></MemoryRouter>); expect(screen.getByText('患者页')).toBeInTheDocument() })
})
