import { useCallback, useEffect, useState } from 'react'
import axios from 'axios'
import { api, errorMessage } from '../api/client.js'

/**
 * url로 GET 요청을 보내고 { data, error, loading, reload } 상태를 돌려준다.
 * url이 바뀌면 다시 요청하고, 화면을 벗어나면 진행 중인 요청을 취소한다.
 * 공통 axios 인스턴스(api)를 써서, 로그인했으면 토큰이 붙고 만료되면 자동으로 재발급된다.
 *
 * reload()를 부르면 같은 url을 다시 요청한다. 그동안에는 이전 data를 그대로 둔다(화면 깜빡임 방지).
 */
export function useFetch(url) {
  // reload할 때마다 1씩 올려서 같은 url이라도 effect가 다시 실행되게 한다
  const [version, setVersion] = useState(0)

  // 응답이 어느 요청(url + version)의 결과인지 같이 저장한다.
  // 저장된 key와 지금 key가 다르면 아직 새 응답이 안 온 것이므로 "불러오는 중"이다.
  const [result, setResult] = useState({ key: null, url: null, data: null, error: null })
  const key = `${url}#${version}`

  useEffect(() => {
    const controller = new AbortController()
    const requestKey = `${url}#${version}`

    api
      .get(url, { signal: controller.signal })
      .then((response) => setResult({ key: requestKey, url, data: response.data, error: null }))
      .catch((err) => {
        if (axios.isCancel(err)) return
        setResult({ key: requestKey, url, data: null, error: errorMessage(err) })
      })

    return () => controller.abort()
  }, [url, version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])

  const loading = result.key !== key
  return {
    // url이 바뀌었으면 이전 data는 다른 화면의 것이므로 버린다
    data: result.url === url ? result.data : null,
    error: loading ? null : result.error,
    loading,
    reload,
  }
}
