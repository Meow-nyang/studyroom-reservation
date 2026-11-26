import { Navigate, useLocation } from 'react-router'
import { useAuth } from './useAuth.js'

/**
 * 로그인해야 볼 수 있는 화면을 감싼다.
 * - 로그인 여부를 확인하는 중: 아무것도 안 보여 준다 (로그인 화면으로 잘못 보냈다가 돌아오는 깜빡임 방지)
 * - 로그인 안 함: 로그인 화면으로 보내고, 로그인하면 이 주소로 돌아오게 한다
 * - 로그인함: 원래 화면을 보여 준다
 */
export function RequireAuth({ children }) {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'loading') return null
  if (status === 'anonymous') {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }
  return children
}
