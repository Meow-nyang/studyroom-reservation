import { useEffect, useState } from 'react'
import axios from 'axios'

/**
 * url로 GET 요청을 보내고 { data, error, loading } 상태를 돌려준다.
 * url이 바뀌면 다시 요청하고, 화면을 벗어나면 진행 중인 요청을 취소한다.
 */
export function useFetch(url) {
  // 응답이 어느 url의 결과인지 같이 저장한다.
  // 저장된 url과 지금 url이 다르면 아직 새 응답이 안 온 것이므로 "불러오는 중"이다.
  const [result, setResult] = useState({ url: null, data: null, error: null })

  useEffect(() => {
    const controller = new AbortController()

    axios
      .get(url, { signal: controller.signal })
      .then((response) => setResult({ url, data: response.data, error: null }))
      .catch((err) => {
        if (axios.isCancel(err)) return
        const message = err.response?.data?.message ?? '서버에 연결할 수 없습니다.'
        setResult({ url, data: null, error: message })
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
