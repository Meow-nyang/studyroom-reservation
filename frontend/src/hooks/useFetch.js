import { useEffect, useState } from 'react'
import axios from 'axios'
import { api, errorMessage } from '../api/client.js'

/**
 * url로 GET 요청을 보내고 { data, error, loading } 상태를 돌려준다.
 * url이 바뀌면 다시 요청하고, 화면을 벗어나면 진행 중인 요청을 취소한다.
 * 공통 axios 인스턴스(api)를 써서, 로그인했으면 토큰이 붙고 만료되면 자동으로 재발급된다.
 */
export function useFetch(url) {
  // 응답이 어느 url의 결과인지 같이 저장한다.
  // 저장된 url과 지금 url이 다르면 아직 새 응답이 안 온 것이므로 "불러오는 중"이다.
  const [result, setResult] = useState({ url: null, data: null, error: null })

  useEffect(() => {
    const controller = new AbortController()

    api
      .get(url, { signal: controller.signal })
      .then((response) => setResult({ url, data: response.data, error: null }))
      .catch((err) => {
        if (axios.isCancel(err)) return
        setResult({ url, data: null, error: errorMessage(err) })
      })

    return () => controller.abort()
  }, [url])

  const loading = result.url !== url
  return {
    data: loading ? null : result.data,
    error: loading ? null : result.error,
    loading,
  }
}
