import { api } from './client.js'

// { roomId, date: '2026-11-26', startTime: '10:00', hours: 2 } → 만들어진 예약
export async function createReservation(request) {
  const response = await api.post('/api/reservations', request)
  return response.data
}
