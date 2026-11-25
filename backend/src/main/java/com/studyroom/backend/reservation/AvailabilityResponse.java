package com.studyroom.backend.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.studyroom.backend.cafe.StudyCafe;

/**
 * 한 방의 하루 시간표. 운영 시간을 1시간 칸으로 나누고 칸마다 예약할 수 있는지 알려 준다.
 * 예: 09:00~23:00 운영이면 09:00, 10:00, ..., 22:00 칸 14개
 * 누가 예약했는지는 보내지 않는다.
 */
public record AvailabilityResponse(LocalDate date, List<Slot> slots) {

    public record Slot(LocalTime time, boolean available) {
    }

    /**
     * @param reservations 이 날짜에 걸친 확정 예약들
     * @param now          지금 시각. 이미 시작된 칸을 막는 데 쓴다.
     */
    public static AvailabilityResponse of(LocalDate date, StudyCafe cafe, List<Reservation> reservations,
            LocalDateTime now) {
        List<Slot> slots = new ArrayList<>();
        LocalDateTime closeAt = date.atTime(cafe.getCloseTime());

        for (LocalDateTime slotStart = date.atTime(cafe.getOpenTime());
                slotStart.isBefore(closeAt);
                slotStart = slotStart.plusHours(1)) {
            LocalDateTime slotEnd = slotStart.plusHours(1);
            boolean started = !slotStart.isAfter(now);
            boolean booked = overlapsAny(slotStart, slotEnd, reservations);
            slots.add(new Slot(slotStart.toLocalTime(), !started && !booked));
        }
        return new AvailabilityResponse(date, slots);
    }

    // 겹침 조건은 ReservationRepository.existsOverlapping과 같다: A의 시작 < B의 끝 그리고 B의 시작 < A의 끝
    private static boolean overlapsAny(LocalDateTime start, LocalDateTime end, List<Reservation> reservations) {
        return reservations.stream()
                .anyMatch(r -> r.getStartAt().isBefore(end) && start.isBefore(r.getEndAt()));
    }
}
