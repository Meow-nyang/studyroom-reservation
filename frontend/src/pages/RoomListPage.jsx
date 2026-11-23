import { Link, useParams } from 'react-router'
import { HourStrip } from '../components/HourStrip.jsx'
import { Status } from '../components/Status.jsx'
import { useFetch } from '../hooks/useFetch.js'
import page from '../styles/page.module.css'
import { formatPrice } from '../utils/format.js'
import styles from './RoomListPage.module.css'

export function RoomListPage() {
  const { cafeId } = useParams()
  const cafe = useFetch(`/api/cafes/${cafeId}`)
  const rooms = useFetch(`/api/cafes/${cafeId}/rooms`)

  const loading = cafe.loading || rooms.loading
  const error = cafe.error || rooms.error
  if (loading || error) return <Status loading={loading} error={error} />

  return (
    <section>
      <Link to="/" className={page.back}>← 지점 목록</Link>
      <h1 className={page.title}>{cafe.data.name}</h1>
      <p className={page.lead}>{cafe.data.address}</p>
      {cafe.data.phone && <p className={page.muted}>{cafe.data.phone}</p>}

      <div className={styles.hours}>
        <p className={page.hours}>
          <span className="sr-only">운영 시간 </span>
          {cafe.data.openTime}–{cafe.data.closeTime}
        </p>
        <HourStrip openTime={cafe.data.openTime} closeTime={cafe.data.closeTime} />
      </div>

      <div className={page.section}>
        <h2 className={page.sectionTitle}>방 {rooms.data.length}개</h2>
        {rooms.data.length === 0 ? (
          <p className={page.muted}>지금 예약할 수 있는 방이 없어요. 다른 지점을 골라 주세요.</p>
        ) : (
          <ul className={page.list}>
            {rooms.data.map((room) => (
              <li key={room.id}>
                <Link to={`/rooms/${room.id}`} className={`${page.row} ${styles.room}`}>
                  <span className={styles.name}>{room.name}</span>
                  <span className={page.muted}>최대 {room.capacity}명</span>
                  <span className={styles.price}>시간당 {formatPrice(room.pricePerHour)}</span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}
