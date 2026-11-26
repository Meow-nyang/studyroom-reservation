import { Link, useParams } from 'react-router'
import { BookingSection } from '../components/BookingSection.jsx'
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

      <BookingSection room={room} />
    </section>
  )
}
