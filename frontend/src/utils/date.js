// 날짜는 서버와 같은 'YYYY-MM-DD' 문자열로 다룬다. 예: '2026-11-26'

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

// 브라우저가 어느 시간대에 있든 한국 기준 오늘. ('en-CA' 형식이 마침 YYYY-MM-DD라서 사용)
export function seoulToday() {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Seoul' }).format(new Date())
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
