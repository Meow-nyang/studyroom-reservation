// 불러오는 중이거나 에러가 났을 때 보여 주는 공통 문구
export function Status({ loading, error }) {
  if (loading) return <p className="status">불러오는 중...</p>
  if (error) return <p className="status status-error">{error}</p>
  return null
}
