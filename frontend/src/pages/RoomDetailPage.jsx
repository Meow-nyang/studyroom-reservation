import { Link, useParams } from 'react-router'
import { Status } from '../components/Status.jsx'
import { useFetch } from '../hooks/useFetch.js'
import { formatPrice, formatTime } from '../utils/format.js'

export function RoomDetailPage() {
  const { roomId } = useParams()
  const { data: room, error, loading } = useFetch(`/api/rooms/${roomId}`)

  if (loading || error) return <Status loading={loading} error={error} />

  return (
    <section>
      <Link to={`/cafes/${room.cafeId}`} className="back-link">← {room.cafeName} 방 목록</Link>
      <h2>{room.name}</h2>
      <p className="page-meta">{room.cafeName}</p>

      <dl className="detail">
        <dt>최대 인원</dt>
        <dd>{room.capacity}명</dd>
        <dt>가격</dt>
        <dd>시간당 {formatPrice(room.pricePerHour)}</dd>
        <dt>운영 시간</dt>
        <dd>{formatTime(room.openTime)} ~ {formatTime(room.closeTime)}</dd>
        <dt>설명</dt>
        <dd>{room.description || '-'}</dd>
      </dl>
    </section>
  )
}
