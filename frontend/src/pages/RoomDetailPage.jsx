import { Link, useParams } from 'react-router'
import { HourGrid } from '../components/HourGrid.jsx'
import { Status } from '../components/Status.jsx'
import { useFetch } from '../hooks/useFetch.js'
import page from '../styles/page.module.css'
import { formatPrice } from '../utils/format.js'
import styles from './RoomDetailPage.module.css'

export function RoomDetailPage() {
  const { roomId } = useParams()
  const { data: room, error, loading } = useFetch(`/api/rooms/${roomId}`)

  if (loading || error) return <Status loading={loading} error={error} />

  return (
    <section>
      <Link to={`/cafes/${room.cafeId}`} className={page.back}>← {room.cafeName}</Link>
      <h1 className={page.title}>{room.name}</h1>
      {room.description && <p className={page.lead}>{room.description}</p>}

      <dl className={styles.facts}>
        <div>
          <dt>최대 인원</dt>
          <dd>{room.capacity}명</dd>
        </div>
        <div>
          <dt>시간당 가격</dt>
          <dd>{formatPrice(room.pricePerHour)}</dd>
        </div>
      </dl>

      <div className={page.section}>
        <h2 className={page.sectionTitle}>
          운영 시간 <span className={page.hours}>{room.openTime}–{room.closeTime}</span>
        </h2>
        <p className={`${page.muted} ${styles.note}`}>칸 하나가 1시간이에요. 예약은 1시간 단위로 해요.</p>
        <HourGrid openTime={room.openTime} closeTime={room.closeTime} />
      </div>
    </section>
  )
}
