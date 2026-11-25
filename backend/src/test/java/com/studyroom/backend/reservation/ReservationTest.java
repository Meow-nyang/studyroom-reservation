package com.studyroom.backend.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.common.exception.InvalidValueException;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.Role;
import com.studyroom.backend.room.Room;

/**
 * 예약 규칙을 확인하는 순수 단위 테스트. "지금"은 2026-11-25 14:20으로 고정한다.
 */
class ReservationTest {

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

    @Test
    void createConfirmedReservation() {
        Reservation reservation = create(TOMORROW, LocalTime.of(10, 0), 2);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getStartAt()).isEqualTo(LocalDateTime.of(2026, 11, 26, 10, 0));
        assertThat(reservation.getEndAt()).isEqualTo(LocalDateTime.of(2026, 11, 26, 12, 0));
        assertThat(reservation.getTotalPrice()).isEqualTo(16000);   // 8000원 × 2시간
        assertThat(reservation.getCanceledAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    void rejectHoursOutOfRange(int hours) {
        assertThatThrownBy(() -> create(TOMORROW, LocalTime.of(10, 0), hours))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("이용 시간은 1~4시간이어야 해요.");
    }

    @Test
    void allowMaxHours() {
        assertThat(create(TOMORROW, LocalTime.of(10, 0), 4).getEndAt())
                .isEqualTo(LocalDateTime.of(2026, 11, 26, 14, 0));
    }

    @Test
    void rejectNotOnTheHour() {
        assertThatThrownBy(() -> create(TOMORROW, LocalTime.of(10, 30), 1))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("예약은 정각에 시작해야 해요.");
    }

    @Test
    void rejectBeforeOpen() {
        assertThatThrownBy(() -> create(TOMORROW, LocalTime.of(8, 0), 2))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("운영 시간(09:00~23:00) 안에서만 예약할 수 있어요.");
    }

    @Test
    void rejectEndAfterClose() {
        assertThatThrownBy(() -> create(TOMORROW, LocalTime.of(22, 0), 2))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("운영 시간(09:00~23:00) 안에서만 예약할 수 있어요.");
    }

    // 22:00 + 4시간을 LocalTime으로 계산하면 02:00이 되어 "닫는 시간 전"으로 잘못 통과할 수 있다
    @Test
    void rejectPastMidnight() {
        assertThatThrownBy(() -> create(TOMORROW, LocalTime.of(22, 0), 4))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("운영 시간(09:00~23:00) 안에서만 예약할 수 있어요.");
    }

    @Test
    void allowEndingExactlyAtClose() {
        assertThat(create(TOMORROW, LocalTime.of(21, 0), 2).getEndAt())
                .isEqualTo(LocalDateTime.of(2026, 11, 26, 23, 0));
    }

    @Test
    void allowLastBookableDay() {
        assertThat(create(TODAY.plusDays(14), LocalTime.of(10, 0), 1).getStartAt().toLocalDate())
                .isEqualTo(LocalDate.of(2026, 12, 9));
    }

    @Test
    void rejectAfterBookableDays() {
        assertThatThrownBy(() -> create(TODAY.plusDays(15), LocalTime.of(10, 0), 1))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("오늘부터 14일 뒤까지만 예약할 수 있어요.");
    }

    @Test
    void rejectPastDate() {
        assertThatThrownBy(() -> create(TODAY.minusDays(1), LocalTime.of(10, 0), 1))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("오늘부터 14일 뒤까지만 예약할 수 있어요.");
    }

    // 지금이 14:20이면 14:00 칸은 이미 시작됐다
    @Test
    void rejectAlreadyStartedSlot() {
        assertThatThrownBy(() -> create(TODAY, LocalTime.of(14, 0), 1))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("이미 시작된 시간은 예약할 수 없어요.");
    }

    @Test
    void allowLaterSlotToday() {
        assertThat(create(TODAY, LocalTime.of(15, 0), 1).getStartAt())
                .isEqualTo(LocalDateTime.of(2026, 11, 25, 15, 0));
    }

    private Reservation create(LocalDate date, LocalTime startTime, int hours) {
        return Reservation.create(member, room, date, startTime, hours, NOW);
    }
}
