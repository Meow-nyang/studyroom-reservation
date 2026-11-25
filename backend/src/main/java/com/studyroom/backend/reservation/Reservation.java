package com.studyroom.backend.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.common.BaseTimeEntity;
import com.studyroom.backend.common.DomainValidator;
import com.studyroom.backend.common.exception.InvalidValueException;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.room.Room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 방 하나를 [startAt, endAt) 동안 쓰는 예약.
 * 끝 시각은 포함하지 않는다. 10:00~12:00 예약 다음에 12:00~13:00 예약이 올 수 있다.
 */
@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    public static final int MIN_HOURS = 1;
    public static final int MAX_HOURS = 4;
    public static final int BOOKABLE_DAYS = 14;   // 오늘부터 14일 뒤까지

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    private LocalDateTime canceledAt;

    private Reservation(Member member, Room room, LocalDateTime startAt, LocalDateTime endAt, int totalPrice) {
        this.member = member;
        this.room = room;
        this.startAt = startAt;
        this.endAt = endAt;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * 예약 규칙을 모두 확인하고 확정 상태의 예약을 만든다.
     * 다른 예약과 겹치는지는 DB를 봐야 알 수 있어서 여기가 아니라 ReservationService에서 확인한다.
     *
     * @param now 지금 시각. 테스트에서 원하는 시각을 넣을 수 있게 밖에서 받는다.
     */
    public static Reservation create(Member member, Room room, LocalDate date, LocalTime startTime, int hours,
            LocalDateTime now) {
        DomainValidator.requireNonNull(member, "회원");
        DomainValidator.requireNonNull(room, "방");
        DomainValidator.requireNonNull(date, "날짜");
        DomainValidator.requireNonNull(startTime, "시작 시간");
        DomainValidator.requireNonNull(now, "현재 시각");

        if (hours < MIN_HOURS || hours > MAX_HOURS) {
            throw new InvalidValueException("이용 시간은 " + MIN_HOURS + "~" + MAX_HOURS + "시간이어야 해요.");
        }
        if (startTime.getMinute() != 0 || startTime.getSecond() != 0 || startTime.getNano() != 0) {
            throw new InvalidValueException("예약은 정각에 시작해야 해요.");
        }

        LocalDate today = now.toLocalDate();
        if (date.isBefore(today) || date.isAfter(today.plusDays(BOOKABLE_DAYS))) {
            throw new InvalidValueException("오늘부터 " + BOOKABLE_DAYS + "일 뒤까지만 예약할 수 있어요.");
        }

        // LocalTime끼리 더하면 자정에서 0시로 돌아가 버린다(22:00 + 4시간 = 02:00).
        // 날짜를 붙인 LocalDateTime으로 계산해서 "다음 날 02:00"이 되게 한다.
        LocalDateTime startAt = date.atTime(startTime);
        LocalDateTime endAt = startAt.plusHours(hours);

        StudyCafe cafe = room.getStudyCafe();
        LocalDateTime openAt = date.atTime(cafe.getOpenTime());
        LocalDateTime closeAt = date.atTime(cafe.getCloseTime());
        if (startAt.isBefore(openAt) || endAt.isAfter(closeAt)) {
            throw new InvalidValueException(
                    "운영 시간(" + cafe.getOpenTime() + "~" + cafe.getCloseTime() + ") 안에서만 예약할 수 있어요.");
        }

        // 14:20이면 14:00 칸은 이미 시작됐으므로 안 된다. 시작 시각이 지금보다 뒤여야 한다.
        if (!startAt.isAfter(now)) {
            throw new InvalidValueException("이미 시작된 시간은 예약할 수 없어요.");
        }

        return new Reservation(member, room, startAt, endAt, room.getPricePerHour() * hours);
    }
}
