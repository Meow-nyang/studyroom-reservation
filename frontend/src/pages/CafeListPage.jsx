import { Link } from 'react-router'
import { Status } from '../components/Status.jsx'
import { useFetch } from '../hooks/useFetch.js'

export function CafeListPage() {
  const { data: cafes, error, loading } = useFetch('/api/cafes')

  if (loading || error) return <Status loading={loading} error={error} />

  return (
    <section>
      <h2>지점 선택</h2>
      <ul className="card-list">
        {cafes.map((cafe) => (
          <li key={cafe.id}>
            <Link to={`/cafes/${cafe.id}`} className="card">
              <strong className="card-title">{cafe.name}</strong>
              <span>{cafe.address}</span>
              <span className="card-meta">
                {cafe.openTime} ~ {cafe.closeTime}
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}
