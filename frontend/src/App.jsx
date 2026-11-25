import { BrowserRouter, Route, Routes } from 'react-router'
import { Header } from './components/Header.jsx'
import { CafeListPage } from './pages/CafeListPage.jsx'
import { LoginPage } from './pages/LoginPage.jsx'
import { NotFoundPage } from './pages/NotFoundPage.jsx'
import { RoomDetailPage } from './pages/RoomDetailPage.jsx'
import { RoomListPage } from './pages/RoomListPage.jsx'
import { SignupPage } from './pages/SignupPage.jsx'
import styles from './App.module.css'

function App() {
  return (
    <BrowserRouter>
      <Header />
      <main className={styles.main}>
        <Routes>
          <Route path="/" element={<CafeListPage />} />
          <Route path="/cafes/:cafeId" element={<RoomListPage />} />
          <Route path="/rooms/:roomId" element={<RoomDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
