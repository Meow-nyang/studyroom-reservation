// 8000 → "8,000원"
export function formatPrice(price) {
  return `${price.toLocaleString('ko-KR')}원`
}
