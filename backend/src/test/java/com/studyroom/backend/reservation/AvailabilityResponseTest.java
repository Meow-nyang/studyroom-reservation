package com.studyroom.backend.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.Role;
import com.studyroom.backend.reservation.AvailabilityResponse.Slot;
import com.studyroom.backend.room.Room;

/**
 * 하루 시간표 계산을 확인하는 순수 단위 테스트. "지금"은 2026-11-25 14:20으로 고정한다.
 */
class AvailabilityResponseTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 25, 14, 20);
    private static final LocalDate TODAY = NOW.toLocalDate();
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    private final StudyCafe cafe = StudyCafe.builder()
            .name("강남점")
            .address("서울 강남구")
            .openTime(LocalTime.of(9, 0))
            .closeTime(LocalTime.of(23, 0))
            .build();

    private final Room room = Room.builder()
            .studyCafe(cafe)
            .name("A룸")
            .capacity(4)
            .pricePerHour(8000)
            .build();

    private final Member member = Member.builder()
            .email("me@example.com")
            .encodedPassword("{encoded}")
            .name("나")
            .role(Role.USER)
            .build();

    // 09:00~23:00 운영이면 09:00부터 22:00까지 14칸 (23:00 칸은 닫는 시간이라 없음)
    @Test
    void slotsCoverOperatingHours() {
        List<Slot> slots = AvailabilityResponse.of(TOMORROW, cafe, List.of(), NOW).slots();

        assertThat(slots).hasSize(14);
        assertThat(slots.get(0).time()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(13).time()).isEqualTo(LocalTime.of(22, 0));
        assertThat(slots).allMatch(Slot::available);
    }

    // 10:00~12:00 예약이 있으면 10시·11시 칸만 막히고, 바로 뒤 12시 칸은 열려 있다
    @Test
    void bookedSlotsAreUnavailable() {
        Reservation booked = Reservation.create(member, room, TOMORROW, LocalTime.of(10, 0), 2, NOW);

        List<Slot> slots = AvailabilityResponse.of(TOMORROW, cafe, List.of(booked), NOW).slots();

        assertThat(availableAt(slots, 9)).isTrue();
        assertThat(availableAt(slots, 10)).isFalse();
        assertThat(availableAt(slots, 11)).isFalse();
        assertThat(availableAt(slots, 12)).isTrue();
    }

    // 지금이 14:20이면 오늘의 14시 칸까지는 이미 시작됐다
    @Test
    void startedSlotsAreUnavailable() {
        List<Slot> slots = AvailabilityResponse.of(TODAY, cafe, List.of(), NOW).slots();

        assertThat(availableAt(slots, 9)).isFalse();
        assertThat(availableAt(slots, 14)).isFalse();
        assertThat(availableAt(slots, 15)).isTrue();
    }

    private static boolean availableAt(List<Slot> slots, int hour) {
        return slots.stream()
                .filter(slot -> slot.time().getHour() == hour)
                .findFirst()
                .orElseThrow()
                .available();
    }
}
