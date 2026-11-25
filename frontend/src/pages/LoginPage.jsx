import { useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router'
import { errorMessage } from '../api/client.js'
import { useAuth } from '../auth/useAuth.js'
import form from '../styles/form.module.css'
import page from '../styles/page.module.css'

export function LoginPage() {
  const { status, login } = useAuth()
  const location = useLocation()
  const from = location.state?.from ?? '/'

  const [values, setValues] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  // 로그인에 성공하면 status가 바뀌면서 이 줄이 원래 보던 화면으로 보낸다
  if (status === 'authenticated') return <Navigate to={from} replace />

  function handleChange(event) {
    setValues({ ...values, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(values)
    } catch (err) {
      setError(errorMessage(err))
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h1 className={page.title}>로그인</h1>

      <form className={form.form} onSubmit={handleSubmit}>
        <label className={form.field}>
          <span className={form.label}>이메일</span>
          <input className={form.input} type="email" name="email" autoComplete="email" required
            value={values.email} onChange={handleChange} />
        </label>
        <label className={form.field}>
          <span className={form.label}>비밀번호</span>
          <input className={form.input} type="password" name="password" autoComplete="current-password" required
            value={values.password} onChange={handleChange} />
        </label>

        {error && <p className={form.error} role="alert">{error}</p>}

        <button className={form.submit} type="submit" disabled={submitting}>
          {submitting ? '로그인하는 중...' : '로그인'}
        </button>
      </form>

      <p className={form.switch}>
        계정이 없나요? <Link to="/signup" state={{ from }}>회원가입</Link>
      </p>
    </section>
  )
}
