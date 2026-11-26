import { toHour } from '../utils/format.js'
import styles from './SlotPicker.module.css'

/**
 * 하루 시간표. 칸 하나가 1시간이고, 누르면 onSelect(시)를 부른다.
 * - 예약할 수 있는 칸: 흰 칸
 * - 고른 칸: 형광펜 노랑
 * - 예약할 수 없는 칸(이미 예약됨 또는 이미 지난 시간): 빗금, 누를 수 없음
 */
export function SlotPicker({ slots, selection, onSelect }) {
  return (
    <ol className={styles.grid} aria-label="시간 선택">
      {slots.map((slot) => {
        const hour = toHour(slot.time)
        const selected = selection !== null && hour >= selection.start && hour <= selection.end

        return (
          <li key={slot.time}>
            <button
              type="button"
              className={`${styles.cell} ${selected ? styles.selected : ''}`}
              disabled={!slot.available}
              aria-pressed={selected}
              aria-label={slot.available ? slot.time : `${slot.time}, 예약할 수 없음`}
              onClick={() => onSelect(hour)}
            >
              {String(hour).padStart(2, '0')}
            </button>
          </li>
        )
      })}
    </ol>
  )
}
