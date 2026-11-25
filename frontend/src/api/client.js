import axios from 'axios'

/*
 * 모든 API 요청이 거쳐 가는 axios 인스턴스.
 *
 * access token은 localStorage가 아니라 이 파일의 변수(메모리)에만 둔다.
 * 새로고침하면 사라지지만, 그때는 HttpOnly 쿠키의 refresh token으로 다시 받는다(AuthContext 참고).
 * 스크립트가 localStorage를 읽어 가는 XSS 공격에서 토큰을 지키기 위해서다.
 */

let accessToken = null
let onSessionExpired = () => {}

export function setAccessToken(token) {
  accessToken = token
}

// 재발급까지 실패해서 로그인이 풀렸을 때 부를 함수를 등록한다 (AuthContext가 등록)
export function setSessionExpiredHandler(handler) {
  onSessionExpired = handler
}

export const api = axios.create()

// 요청을 보내기 전: 토큰이 있으면 Authorization 헤더에 붙인다
api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

// 응답을 받은 뒤: 401이면 토큰을 재발급하고 원래 요청을 한 번 다시 보낸다
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const isAuthRequest = original?.url?.startsWith('/api/auth/')

    // 401이 아니거나, 로그인·재발급 요청 자체거나, 이미 한 번 다시 보낸 요청이면 그대로 실패
    if (error.response?.status !== 401 || isAuthRequest || original._retried) {
      return Promise.reject(error)
    }

    original._retried = true
    try {
      await refreshAccessToken()
    } catch {
      setAccessToken(null)
      onSessionExpired()
      return Promise.reject(error)
    }
    return api(original)
  },
)

/*
 * 재발급은 동시에 하나만 진행한다.
 * 토큰이 만료된 순간 요청 3개가 함께 401을 받아도 재발급 요청은 1번만 나가고, 셋 다 그 결과를 기다린다.
 * (서버는 refresh token을 한 번 쓰면 바꿔 버리므로, 여러 번 보내면 하나 빼고 다 실패한다)
 */
let refreshPromise = null

export function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = refreshWithOneRetry().finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

/*
 * 다른 탭이 거의 동시에 재발급해서 쿠키가 막 바뀐 경우, 우리 요청은 옛 쿠키로 나가서 실패할 수 있다.
 * 그 사이 브라우저의 쿠키는 새 값으로 바뀌어 있으니 딱 한 번만 다시 시도한다.
 */
async function refreshWithOneRetry() {
  try {
    return await requestRefresh()
  } catch (error) {
    if (error.response?.status === 401) {
      return requestRefresh()
    }
    throw error
  }
}

// api 인스턴스가 아니라 기본 axios로 보낸다. api로 보내면 401 처리 규칙이 다시 걸려서 꼬일 수 있다.
// refresh token은 HttpOnly 쿠키라 코드에서는 보이지 않고, 브라우저가 알아서 붙여 보낸다.
async function requestRefresh() {
  const response = await axios.post('/api/auth/refresh')
  setAccessToken(response.data.accessToken)
  return response.data.accessToken
}

// 서버의 에러 응답 {"message": "..."}에서 문구를 꺼낸다
export function errorMessage(error, fallback = '서버에 연결할 수 없습니다.') {
  return error.response?.data?.message ?? fallback
}
