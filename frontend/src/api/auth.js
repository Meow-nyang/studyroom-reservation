import { api, setAccessToken } from './client.js'

export async function signup({ email, password, name }) {
  const response = await api.post('/api/auth/signup', { email, password, name })
  return response.data
}

// 성공하면 access token은 메모리에 저장되고, refresh token은 서버가 HttpOnly 쿠키로 심는다
export async function login({ email, password }) {
  const response = await api.post('/api/auth/login', { email, password })
  setAccessToken(response.data.accessToken)
}

export async function logout() {
  try {
    await api.post('/api/auth/logout')
  } finally {
    // 서버 요청이 실패해도 이 브라우저에서는 로그아웃 상태로 만든다
    setAccessToken(null)
  }
}

export async function fetchMe() {
  const response = await api.get('/api/members/me')
  return response.data
}
