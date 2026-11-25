import { useContext } from 'react'
import { AuthContext } from './authContext.js'

/**
 * 로그인 상태와 기능을 꺼낸다.
 * { status, member, login, signup, logout }
 */
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth는 AuthProvider 안에서만 쓸 수 있어요.')
  }
  return context
}
