import { toHour } from '../utils/format.js'
import styles from './HourStrip.module.css'

const HOURS = Array.from({ length: 24 }, (_, hour) => hour)
const TICKS = [0, 6, 12, 18, 24]

/**
 * 하루 24시간을 한 줄 띠로 보여 주고, 운영 시간을 형광펜으로 칠한다.
 * 지점끼리 여는 시간과 닫는 시간을 한눈에 비교하기 위한 그림이라, 화면 읽기 프로그램에는 숨기고
 * 같은 정보를 글자(운영 시간)로 따로 보여 준다.
 */
export function HourStrip({ openTime, closeTime }) {
  const open = toHour(openTime)
  const close = toHour(closeTime)

  return (
    <div className={styles.strip} aria-hidden="true">
      <div className={styles.cells}>
        {HOURS.map((hour) => (
          <span
            key={hour}
            className={hour >= open && hour < close ? styles.open : styles.closed}
          />
        ))}
      </div>
      <div className={styles.ticks}>
        {TICKS.map((tick) => (
          <span key={tick}>{tick}</span>
        ))}
      </div>
    </div>
  )
}
