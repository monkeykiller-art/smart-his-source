import { Result } from 'antd'
import type { ReactNode } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { hasAnyPermission } from '@/security/authz'

export function PermissionGuard({ anyOf, children }: { anyOf: string[]; children: ReactNode }) {
  const session = useAuthStore(state => state.session)
  return hasAnyPermission(session, anyOf)
    ? children
    : <Result status="403" title="403" subTitle="当前账号没有访问此功能的权限。" />
}
