import { Link } from 'react-router'
import page from '../styles/page.module.css'

export function NotFoundPage() {
  return (
    <section>
      <h1 className={page.title}>없는 페이지예요</h1>
      <p className={page.lead}>주소가 바뀌었거나 잘못 입력됐을 수 있어요.</p>
      <p className={page.section}>
        <Link to="/">지점 목록으로 가기</Link>
      </p>
    </section>
  )
}
