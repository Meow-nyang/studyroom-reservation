// 날짜는 서버와 같은 'YYYY-MM-DD' 문자열로 다룬다. 예: '2026-11-26'

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

// 브라우저가 어느 시간대에 있든 한국 기준 오늘. ('en-CA' 형식이 마침 YYYY-MM-DD라서 사용)
export function seoulToday() {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Seoul' }).format(new Date())
}

// 한국 기준 지금 시각을 서버 응답과 같은 형식으로. 예: '2026-11-26T14:20'
// 형식이 같으면 문자열 비교로 시각의 앞뒤를 알 수 있다('2026-11-26T14:20' < '2026-11-27T10:00').
export function seoulNow() {
  const parts = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Seoul',
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
    }).formatToParts(new Date()).map((part) => [part.type, part.value]),
  )
  return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}`
}

// '2026-11-30' + 1일 → '2026-12-01'. 시간대 영향을 받지 않게 UTC 기준으로 계산한다.
export function addDays(date, days) {
  const [year, month, day] = date.split('-').map(Number)
  return new Date(Date.UTC(year, month - 1, day + days)).toISOString().slice(0, 10)
}

// '2026-11-26' → '목'
export function weekday(date) {
  const [year, month, day] = date.split('-').map(Number)
  return WEEKDAYS[new Date(Date.UTC(year, month - 1, day)).getUTCDay()]
}

// '2026-11-26' → '11/26'
export function monthDay(date) {
  const [, month, day] = date.split('-').map(Number)
  return `${month}/${day}`
}

// '2026-11-27T14:00' → '11/27 (금)'
export function dayLabel(dateTime) {
  const date = dateTime.slice(0, 10)
  return `${monthDay(date)} (${weekday(date)})`
}
