// "09:00:00" → "09:00"
export function formatTime(time) {
  return time.slice(0, 5)
}

// 8000 → "8,000원"
export function formatPrice(price) {
  return `${price.toLocaleString('ko-KR')}원`
}
