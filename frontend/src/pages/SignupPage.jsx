import { useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router'
import { errorMessage } from '../api/client.js'
import { useAuth } from '../auth/useAuth.js'
import form from '../styles/form.module.css'
import page from '../styles/page.module.css'

// 백엔드 SignupRequest와 같은 규칙. 서버도 다시 검사하지만, 보내기 전에 미리 알려 준다
const PASSWORD_RULE = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/
const PASSWORD_HINT = '영문과 숫자를 포함해 8~20자'

export function SignupPage() {
  const { status, signup } = useAuth()
  const location = useLocation()
  const from = location.state?.from ?? '/'

  const [values, setValues] = useState({ email: '', password: '', name: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  // 가입하면 바로 로그인되고, 원래 보던 화면으로 돌아간다
  if (status === 'authenticated') return <Navigate to={from} replace />

  function handleChange(event) {
    setValues({ ...values, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (!PASSWORD_RULE.test(values.password)) {
      setError(`비밀번호는 ${PASSWORD_HINT}여야 해요.`)
      return
    }
    setError(null)
    setSubmitting(true)
    try {
      await signup(values)
    } catch (err) {
      setError(errorMessage(err))
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h1 className={page.title}>회원가입</h1>

      <form className={form.form} onSubmit={handleSubmit}>
        <label className={form.field}>
          <span className={form.label}>이메일</span>
          <input className={form.input} type="email" name="email" autoComplete="email" required maxLength={100}
            value={values.email} onChange={handleChange} />
        </label>
        <label className={form.field}>
          <span className={form.label}>비밀번호</span>
          <input className={form.input} type="password" name="password" autoComplete="new-password" required
            maxLength={20} aria-describedby="password-hint"
            value={values.password} onChange={handleChange} />
          <span id="password-hint" className={form.hint}>{PASSWORD_HINT}</span>
        </label>
        <label className={form.field}>
          <span className={form.label}>이름</span>
          <input className={form.input} type="text" name="name" autoComplete="name" required maxLength={20}
            value={values.name} onChange={handleChange} />
        </label>

        {error && <p className={form.error} role="alert">{error}</p>}

        <button className={form.submit} type="submit" disabled={submitting}>
          {submitting ? '가입하는 중...' : '가입하기'}
        </button>
      </form>

      <p className={form.switch}>
        이미 계정이 있나요? <Link to="/login" state={{ from }}>로그인</Link>
      </p>
    </section>
  )
}
