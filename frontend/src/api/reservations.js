import { api } from './client.js'

// { roomId, date: '2026-11-26', startTime: '10:00', hours: 2 } → 만들어진 예약
export async function createReservation(request) {
  const response = await api.post('/api/reservations', request)
  return response.data
}

// 지우지 않고 취소 상태로 바꾼다 → 바뀐 예약
export async function cancelReservation(reservationId) {
  const response = await api.patch(`/api/reservations/${reservationId}/cancel`)
  return response.data
}
