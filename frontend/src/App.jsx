import { BrowserRouter, Link, Route, Routes } from 'react-router'
import { CafeListPage } from './pages/CafeListPage.jsx'
import { NotFoundPage } from './pages/NotFoundPage.jsx'
import { RoomDetailPage } from './pages/RoomDetailPage.jsx'
import { RoomListPage } from './pages/RoomListPage.jsx'
import styles from './App.module.css'

function App() {
  return (
    <BrowserRouter>
      <header className={styles.header}>
        <div className={styles.inner}>
          <Link to="/" className={styles.logo}>스터디룸</Link>
        </div>
      </header>
      <main className={styles.inner}>
        <Routes>
          <Route path="/" element={<CafeListPage />} />
          <Route path="/cafes/:cafeId" element={<RoomListPage />} />
          <Route path="/rooms/:roomId" element={<RoomDetailPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
