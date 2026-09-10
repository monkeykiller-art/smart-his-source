import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

export function ProtectedRoute() {
  const session = useAuthStore((state) => state.session)
  const location = useLocation()
  return session ? <Outlet /> : <Navigate to="/login" replace state={{ from: location }} />
}
