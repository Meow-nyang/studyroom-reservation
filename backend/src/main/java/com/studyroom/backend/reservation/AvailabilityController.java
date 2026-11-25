package com.studyroom.backend.reservation;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// 주소는 /api/rooms/** 아래라서 SecurityConfig 규칙에 따라 로그인 없이 누구나 볼 수 있다
@RestController
@RequiredArgsConstructor
public class AvailabilityController {

    private final ReservationService reservationService;

    // 예: GET /api/rooms/1/availability?date=2026-11-26
    @GetMapping("/api/rooms/{roomId}/availability")
    public AvailabilityResponse findAvailability(@PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reservationService.findAvailability(roomId, date);
    }
}
