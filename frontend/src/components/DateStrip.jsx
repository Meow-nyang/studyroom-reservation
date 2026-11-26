import { monthDay, weekday } from '../utils/date.js'
import styles from './DateStrip.module.css'

/**
 * 예약할 날짜를 고르는 가로 줄. 칸이 넘치면 옆으로 넘겨 본다.
 * 선택한 날짜는 잉크색으로 칠한다(형광펜 노랑은 시간에만 쓰므로).
 */
export function DateStrip({ dates, today, selected, onChange }) {
  return (
    <ol className={styles.strip} aria-label="날짜 선택">
      {dates.map((date) => (
        <li key={date}>
          <button
            type="button"
            className={`${styles.day} ${date === selected ? styles.selected : ''}`}
            aria-pressed={date === selected}
            onClick={() => onChange(date)}
          >
            <span className={styles.weekday}>{date === today ? '오늘' : weekday(date)}</span>
            <span className={styles.date}>{monthDay(date)}</span>
          </button>
        </li>
      ))}
    </ol>
  )
}
