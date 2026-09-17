import type { AuthSession } from '@/services/tokenStorage'

export function hasAnyPermission(session: AuthSession | null, permissions: string[]): boolean {
  if (!session) return false
  if (session.roles?.includes('ADMIN')) return true
  const granted = new Set(session.permissions || [])
  return permissions.some(permission => granted.has(permission))
}
