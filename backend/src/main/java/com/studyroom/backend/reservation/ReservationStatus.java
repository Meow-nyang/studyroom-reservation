package com.studyroom.backend.reservation;

/**
 * 예약 상태. 결제 대기(PENDING_PAYMENT)는 5단계에서 추가한다.
 * "이용 완료"는 상태로 두지 않고 끝나는 시각(endAt)이 지났는지로 판단한다.
 */
public enum ReservationStatus {
    CONFIRMED,
    CANCELED
}
