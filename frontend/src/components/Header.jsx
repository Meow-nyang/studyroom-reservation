import { Link, useLocation } from 'react-router'
import { useAuth } from '../auth/useAuth.js'
import styles from './Header.module.css'

const AUTH_PAGES = ['/login', '/signup']

export function Header() {
  const { status, member, logout } = useAuth()
  const location = useLocation()

  // 로그인이 끝나면 지금 보던 화면으로 돌아오도록 현재 주소를 함께 넘긴다
  const from = AUTH_PAGES.includes(location.pathname)
    ? location.state?.from
    : location.pathname + location.search

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        <Link to="/" className={styles.logo}>스터디룸</Link>

        {/* 로그인 여부를 확인하는 동안에는 아무것도 보여 주지 않는다 (깜빡임 방지) */}
        {status === 'authenticated' && (
          <nav className={styles.nav}>
            <span className={styles.name}>{member.name}님</span>
            <button type="button" className={styles.textButton} onClick={logout}>
              로그아웃
            </button>
          </nav>
        )}
        {status === 'anonymous' && (
          <nav className={styles.nav}>
            <Link to="/login" state={{ from }} className={styles.link}>로그인</Link>
            <Link to="/signup" state={{ from }} className={styles.link}>회원가입</Link>
          </nav>
        )}
      </div>
    </header>
  )
}
