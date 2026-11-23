// 8000 → "8,000원"
export function formatPrice(price) {
  return `${price.toLocaleString('ko-KR')}원`
}

// "09:00" → 9
export function toHour(time) {
  return Number(time.slice(0, 2))
}
