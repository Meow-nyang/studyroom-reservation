import { toHour } from '../utils/format.js'
import styles from './HourGrid.module.css'

/**
 * 운영 시간을 1시간 칸으로 나눠 보여 준다. 칸 하나가 예약 한 단위다.
 * 3단계에서 이 칸을 눌러 예약 시간을 고르게 된다.
 */
export function HourGrid({ openTime, closeTime }) {
  const open = toHour(openTime)
  const close = toHour(closeTime)
  const hours = Array.from({ length: close - open }, (_, i) => open + i)

  return (
    <ol className={styles.grid} aria-label={`${openTime}부터 ${closeTime}까지 1시간 단위`}>
      {hours.map((hour) => (
        <li key={hour} className={styles.cell}>
          {String(hour).padStart(2, '0')}
        </li>
      ))}
    </ol>
  )
}
