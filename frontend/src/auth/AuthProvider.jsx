import { useCallback, useEffect, useMemo, useState } from 'react'
import * as authApi from '../api/auth.js'
import { refreshAccessToken, setSessionExpiredHandler } from '../api/client.js'
import { AuthContext } from './authContext.js'

/*
 * 로그인 상태를 앱 전체에 알려 준다.
 * status: 'loading'(확인 중) | 'authenticated'(로그인됨) | 'anonymous'(비로그인)
 * member: /api/members/me 응답. 회원 정보는 여기 한 곳에만 둔다(sessionStorage 등에 따로 저장하지 않음).
 */
export function AuthProvider({ children }) {
  const [state, setState] = useState({ status: 'loading', member: null })

  const becomeAnonymous = useCallback(() => {
    setState({ status: 'anonymous', member: null })
  }, [])

  const loadMe = useCallback(async () => {
    const member = await authApi.fetchMe()
    setState({ status: 'authenticated', member })
  }, [])

  // 앱이 처음 열리거나 새로고침됐을 때: 쿠키로 access token을 다시 받고 → 내 정보를 불러온다
  useEffect(() => {
    setSessionExpiredHandler(becomeAnonymous)
    refreshAccessToken()
      .then(loadMe)
      .catch(becomeAnonymous)
  }, [becomeAnonymous, loadMe])

  const login = useCallback(async (credentials) => {
    await authApi.login(credentials)
    await loadMe()
  }, [loadMe])

  // 가입 직후 같은 이메일·비밀번호로 바로 로그인한다
  const signup = useCallback(async (form) => {
    await authApi.signup(form)
    await login({ email: form.email, password: form.password })
  }, [login])

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      becomeAnonymous()
    }
  }, [becomeAnonymous])

  const value = useMemo(
    () => ({ ...state, login, signup, logout }),
    [state, login, signup, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
