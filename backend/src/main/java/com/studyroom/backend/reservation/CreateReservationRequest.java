package com.studyroom.backend.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/**
 * 예약 생성 요청. 예: {"roomId": 1, "date": "2026-11-26", "startTime": "14:00", "hours": 2}
 * 값이 있는지만 여기서(@Valid) 보고, 시간 수·운영 시간 같은 예약 규칙은 Reservation.create에서 본다.
 */
public record CreateReservationRequest(

        @NotNull(message = "방을 선택해 주세요.")
        Long roomId,

        @NotNull(message = "날짜를 선택해 주세요.")
        LocalDate date,

        @NotNull(message = "시작 시간을 선택해 주세요.")
        LocalTime startTime,

        @NotNull(message = "이용 시간을 선택해 주세요.")
        Integer hours) {
}
