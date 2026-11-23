import { Link } from 'react-router'
import { HourStrip } from '../components/HourStrip.jsx'
import { Status } from '../components/Status.jsx'
import { useFetch } from '../hooks/useFetch.js'
import page from '../styles/page.module.css'
import styles from './CafeListPage.module.css'

export function CafeListPage() {
  const { data: cafes, error, loading } = useFetch('/api/cafes')

  if (loading || error) return <Status loading={loading} error={error} />

  return (
    <section>
      <h1 className={page.title}>지점을 골라 주세요</h1>
      <p className={page.lead}>막대의 칠해진 부분이 운영 시간이에요.</p>

      <ul className={page.list}>
        {cafes.map((cafe) => (
          <li key={cafe.id}>
            <Link to={`/cafes/${cafe.id}`} className={page.row}>
              <div className={styles.head}>
                <h2 className={styles.name}>{cafe.name}</h2>
                <p className={page.hours}>
                  <span className="sr-only">운영 시간 </span>
                  {cafe.openTime}–{cafe.closeTime}
                </p>
              </div>
              <p className={`${page.muted} ${styles.address}`}>{cafe.address}</p>
              <HourStrip openTime={cafe.openTime} closeTime={cafe.closeTime} />
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}
