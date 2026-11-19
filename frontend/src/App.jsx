import { useEffect, useState } from 'react'
import axios from 'axios'
import './App.css'

function App() {
  const [status, setStatus] = useState('확인 중...')
  const [error, setError] = useState(null)

  useEffect(() => {
    const controller = new AbortController()

    axios
      .get('/api/health', { signal: controller.signal })
      .then((response) => {
        setStatus(response.data.status)
        setError(null)
      })
      .catch((err) => {
        if (axios.isCancel(err)) return
        setStatus('연결 실패')
        setError(err.message)
      })

    return () => controller.abort()
  }, [])

  return (
    <main className="health">
      <h1>스터디룸 예약</h1>
      <p className="health-status">서버 상태: {status}</p>
      {error && <p className="health-error">{error}</p>}
    </main>
  )
}

export default App
