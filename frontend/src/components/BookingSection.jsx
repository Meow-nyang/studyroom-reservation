import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { errorMessage } from '../api/client.js'
import { createReservation } from '../api/reservations.js'
import { useAuth } from '../auth/useAuth.js'
import { useFetch } from '../hooks/useFetch.js'
import form from '../styles/form.module.css'
import page from '../styles/page.module.css'
import { addDays, monthDay, seoulToday, weekday } from '../utils/date.js'
import { formatPrice, toTime } from '../utils/format.js'
import { MAX_HOURS, selectHour } from '../utils/slotSelection.js'
import { DateStrip } from './DateStrip.jsx'
import { SlotPicker } from './SlotPicker.jsx'
import { Status } from './Status.jsx'
import styles from './BookingSection.module.css'

const BOOKABLE_DAYS = 14   // 서버와 같은 규칙: 오늘부터 14일 뒤까지

/**
 * 방 상세 화면 아래의 예약 영역: 날짜 고르기 → 시간 고르기 → 예약하기
 * 시간표는 로그인하지 않아도 볼 수 있고, 예약하기를 누를 때 로그인을 확인한다.
 */
export function BookingSection({ room }) {
  const { status } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  // 처음 그릴 때 한 번만 계산한다
  const [today] = useState(seoulToday)
  const dates = Array.from({ length: BOOKABLE_DAYS + 1 }, (_, i) => addDays(today, i))

  const [date, setDate] = useState(today)
  const [selection, setSelection] = useState(null)
  const [notice, setNotice] = useState(null)       // 선택이 안 된 이유 (예: 최대 4시간)
  const [error, setError] = useState(null)         // 예약 요청 실패 문구
  const [reserved, setReserved] = useState(null)   // 방금 만든 예약
  const [submitting, setSubmitting] = useState(false)

  const availability = useFetch(`/api/rooms/${room.id}/availability?date=${date}`)

  const hours = selection ? selection.end - selection.start + 1 : 0

  function clearMessages() {
    setNotice(null)
    setError(null)
    setReserved(null)
  }

  function handleDateChange(nextDate) {
    setDate(nextDate)
    setSelection(null)
    clearMessages()
  }

  function handleSelect(hour) {
    const result = selectHour(selection, hour, availability.data.slots)
    setSelection(result.selection)
    clearMessages()
    setNotice(result.notice ?? null)
  }

  async function handleReserve() {
    // 로그인하지 않았으면 로그인 화면으로. 로그인하면 이 방으로 돌아온다(고른 시간은 초기화).
    if (status !== 'authenticated') {
      navigate('/login', { state: { from: location.pathname } })
      return
    }

    setSubmitting(true)
    clearMessages()
    try {
      const reservation = await createReservation({
        roomId: room.id,
        date,
        startTime: toTime(selection.start),
        hours,
      })
      setReserved(reservation)
      setSelection(null)
      availability.reload()
    } catch (err) {
      setError(errorMessage(err))
      // 409: 고르는 사이에 다른 사람이 먼저 예약했다. 선택을 풀고 최신 시간표를 다시 받는다.
      if (err.response?.status === 409) {
        setSelection(null)
        availability.reload()
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className={page.section}>
      <h2 className={page.sectionTitle}>
        예약하기 <span className={page.hours}>{room.openTime}–{room.closeTime}</span>
      </h2>

      <DateStrip dates={dates} today={today} selected={date} onChange={handleDateChange} />

      <p className={`${page.muted} ${styles.guide}`}>
        시작 시간을 누르고, 끝 시간을 누르세요. 칸 하나가 1시간이고 최대 {MAX_HOURS}시간까지 예약할 수 있어요.
      </p>

      {availability.data ? (
        <SlotPicker slots={availability.data.slots} selection={selection} onSelect={handleSelect} />
      ) : (
        <Status loading={availability.loading} error={availability.error} />
      )}

      {notice && <p className={styles.notice}>{notice}</p>}

      {selection && (
        <div className={styles.summary}>
          <p>
            <span className={styles.when}>
              {monthDay(date)} ({weekday(date)}) {toTime(selection.start)}–{toTime(selection.end + 1)}
            </span>
            <span className={page.muted}> · {hours}시간 · </span>
            <span className={styles.price}>{formatPrice(room.pricePerHour * hours)}</span>
          </p>
          <button
            type="button"
            className={form.submit}
            onClick={handleReserve}
            disabled={submitting || status === 'loading'}
          >
            {submitting ? '예약하는 중...' : '예약하기'}
          </button>
        </div>
      )}

      {error && <p className={`${form.error} ${styles.message}`} role="alert">{error}</p>}
      {reserved && (
        <p className={styles.message} role="status">
          예약했어요. {monthDay(date)} {reserved.startAt.slice(11)}–{reserved.endAt.slice(11)}
        </p>
      )}
    </div>
  )
}
