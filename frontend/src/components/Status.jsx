import styles from './Status.module.css'

// 불러오는 중이거나 에러가 났을 때 보여 주는 공통 문구
export function Status({ loading, error }) {
  if (loading) return <p className={styles.status}>불러오는 중...</p>
  if (error) return <p className={`${styles.status} ${styles.error}`} role="alert">{error}</p>
  return null
}
