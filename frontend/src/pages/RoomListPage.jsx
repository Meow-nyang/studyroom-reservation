import { Link, useParams } from 'react-router'
import { Status } from '../components/Status.jsx'
import { useFetch } from '../hooks/useFetch.js'
import { formatPrice, formatTime } from '../utils/format.js'

export function RoomListPage() {
  const { cafeId } = useParams()
  const cafe = useFetch(`/api/cafes/${cafeId}`)
  const rooms = useFetch(`/api/cafes/${cafeId}/rooms`)

  const loading = cafe.loading || rooms.loading
  const error = cafe.error || rooms.error
  if (loading || error) return <Status loading={loading} error={error} />

  return (
    <section>
      <Link to="/" className="back-link">← 지점 목록</Link>
      <h2>{cafe.data.name}</h2>
      <p className="page-meta">
        {cafe.data.address} · {formatTime(cafe.data.openTime)} ~ {formatTime(cafe.data.closeTime)}
      </p>

      {rooms.data.length === 0 ? (
        <p className="status">예약 가능한 방이 없습니다.</p>
      ) : (
        <ul className="card-list">
          {rooms.data.map((room) => (
            <li key={room.id}>
              <Link to={`/rooms/${room.id}`} className="card">
                <strong className="card-title">{room.name}</strong>
                <span>최대 {room.capacity}명</span>
                <span className="card-meta">시간당 {formatPrice(room.pricePerHour)}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
