import { createContext } from 'react'

// 값은 AuthProvider가 채우고, 꺼내 쓸 때는 useAuth()를 쓴다
export const AuthContext = createContext(null)
