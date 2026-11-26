import { toHour } from './format.js'

export const MAX_HOURS = 4

/*
 * 시간표 칸을 눌렀을 때 선택 범위를 정한다. 화면과 상관없는 규칙만 모아 둔 함수다.
 *
 * selection: null(아무것도 안 고름) 또는 { start, end } (시 단위 숫자, end 칸 포함)
 *   예: { start: 10, end: 11 } → 10시 칸과 11시 칸 → 10:00~12:00, 2시간
 *
 * 고르는 방법: 시작 칸을 누르고 → 끝 칸을 누른다.
 * 돌려주는 값: { selection, notice } (notice는 선택이 안 된 이유. 없으면 undefined)
 */
export function selectHour(selection, hour, slots) {
  // 처음 누르거나, 이미 범위를 다 골랐으면 → 누른 칸에서 새로 시작
  if (!selection || selection.start !== selection.end) {
    return { selection: { start: hour, end: hour } }
  }

  const { start } = selection

  // 시작 칸을 한 번 더 누르면 선택 해제
  if (hour === start) {
    return { selection: null }
  }

  // 시작 칸보다 앞을 누르면 거기서 새로 시작
  if (hour < start) {
    return { selection: { start: hour, end: hour } }
  }

  if (hour - start + 1 > MAX_HOURS) {
    return {
      selection: { start: hour, end: hour },
      notice: `최대 ${MAX_HOURS}시간까지 예약할 수 있어요.`,
    }
  }

  // 시작 칸과 끝 칸 사이에 예약할 수 없는 칸이 끼어 있으면 이어서 고를 수 없다
  const blocked = slots.some((slot) => {
    const slotHour = toHour(slot.time)
    return slotHour > start && slotHour < hour && !slot.available
  })
  if (blocked) {
    return {
      selection: { start: hour, end: hour },
      notice: '중간에 예약할 수 없는 시간이 있어요.',
    }
  }

  return { selection: { start, end: hour } }
}
