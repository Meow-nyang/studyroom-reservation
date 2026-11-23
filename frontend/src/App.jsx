import { BrowserRouter, Link, Route, Routes } from 'react-router'
import { CafeListPage } from './pages/CafeListPage.jsx'
import { RoomDetailPage } from './pages/RoomDetailPage.jsx'
import { RoomListPage } from './pages/RoomListPage.jsx'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <header className="header">
        <Link to="/" className="logo">스터디룸 예약</Link>
      </header>
      <main className="main">
        <Routes>
          <Route path="/" element={<CafeListPage />} />
          <Route path="/cafes/:cafeId" element={<RoomListPage />} />
          <Route path="/rooms/:roomId" element={<RoomDetailPage />} />
          <Route path="*" element={<p className="status">없는 페이지입니다.</p>} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
