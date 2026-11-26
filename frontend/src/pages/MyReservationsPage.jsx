import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { errorMessage } from '../api/client.js'
import { cancelReservation } from '../api/reservations.js'
import { Status } from '../components/Status.jsx'
import { useFetch } from '../hooks/useFetch.js'
import page from '../styles/page.module.css'
import { dayLabel, seoulNow } from '../utils/date.js'
import { formatPrice } from '../utils/format.js'
import styles from './MyReservationsPage.module.css'

/**
 * 내 예약. "예정된 예약"과 "지난·취소된 예약" 두 묶음으로 나눠 보여 준다.
 * 예정된 예약에만 취소 버튼이 있다.
 */
export function MyReservationsPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { data: reservations, error, loading, reload } = useFetch('/api/reservations/me')
  const [cancelError, setCancelError] = useState(null)
  const [cancelingId, setCancelingId] = useState(null)

  // 예약 화면에서 넘어왔으면 "예약했어요"를 한 번만 보여 준다.
  // state는 브라우저 방문 기록에 남아서 새로고침해도 그대로라, 읽은 뒤 바로 지운다.
  const [justReserved, setJustReserved] = useState(location.state?.reserved === true)
  useEffect(() => {
    if (location.state?.reserved) {
      navigate(location.pathname, { replace: true, state: null })
    }
  }, [location, navigate])

  if (!reservations) return <Status loading={loading} error={error} />

  // 서버는 시작 시각 최신순으로 준다. 예정된 예약은 가까운 것부터 보이게 뒤집는다.
  const now = seoulNow()
  const upcoming = reservations
    .filter((r) => r.status === 'CONFIRMED' && r.startAt > now)
    .reverse()
  const past = reservations.filter((r) => !upcoming.includes(r))

  async function handleCancel(reservation) {
    const when = `${dayLabel(reservation.startAt)} ${reservation.startAt.slice(11)}–${reservation.endAt.slice(11)}`
    if (!window.confirm(`${when} ${reservation.roomName} 예약을 취소할까요?`)) return

    setJustReserved(false)
    setCancelError(null)
    setCancelingId(reservation.id)
    try {
      await cancelReservation(reservation.id)
      reload()
    } catch (err) {
      setCancelError(errorMessage(err))
    } finally {
      setCancelingId(null)
    }
  }

  return (
    <section>
      <h1 className={page.title}>내 예약</h1>
      {justReserved && <p className={page.lead} role="status">예약했어요.</p>}
      {cancelError && <p className={styles.error} role="alert">{cancelError}</p>}

      <div className={page.section}>
        <h2 className={page.sectionTitle}>예정된 예약</h2>
        {upcoming.length === 0 ? (
          <p className={page.muted}>
            예정된 예약이 없어요. <Link to="/">지점 둘러보기</Link>
          </p>
        ) : (
          <ul className={page.list}>
            {upcoming.map((reservation) => (
              <li key={reservation.id} className={styles.row}>
                <ReservationSummary reservation={reservation} highlight />
                <button
                  type="button"
                  className={styles.cancel}
                  onClick={() => handleCancel(reservation)}
                  disabled={cancelingId === reservation.id}
                >
                  {cancelingId === reservation.id ? '취소하는 중...' : '취소'}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {past.length > 0 && (
        <div className={page.section}>
          <h2 className={page.sectionTitle}>지난·취소된 예약</h2>
          <ul className={page.list}>
            {past.map((reservation) => (
              <li key={reservation.id} className={`${styles.row} ${styles.past}`}>
                <ReservationSummary reservation={reservation} />
                <span className={styles.badge}>
                  {reservation.status === 'CANCELED' ? '취소됨' : '이용 완료'}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}

// 예약 한 줄: 날짜·시간(예정된 예약은 형광펜), 지점·방, 금액
function ReservationSummary({ reservation, highlight = false }) {
  return (
    <div>
      <p className={styles.when}>
        {dayLabel(reservation.startAt)}{' '}
        <span className={highlight ? styles.time : undefined}>
          {reservation.startAt.slice(11)}–{reservation.endAt.slice(11)}
        </span>
      </p>
      <p className={page.muted}>
        <Link to={`/rooms/${reservation.roomId}`} className={styles.room}>
          {reservation.cafeName} · {reservation.roomName}
        </Link>
        {' · '}{formatPrice(reservation.totalPrice)}
      </p>
    </div>
  )
}
