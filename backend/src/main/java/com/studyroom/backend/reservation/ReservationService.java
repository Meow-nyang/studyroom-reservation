package com.studyroom.backend.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.common.exception.ConflictException;
import com.studyroom.backend.common.exception.InvalidValueException;
import com.studyroom.backend.common.exception.NotFoundException;
import com.studyroom.backend.common.exception.UnauthorizedException;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.MemberRepository;
import com.studyroom.backend.room.Room;
import com.studyroom.backend.room.RoomRepository;
import com.studyroom.backend.room.RoomStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional
    public ReservationResponse create(Long memberId, CreateReservationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException("다시 로그인해 주세요."));
        Room room = findActiveRoom(request.roomId());

        Reservation reservation = Reservation.create(member, room, request.date(), request.startTime(),
                request.hours(), LocalDateTime.now(clock));

        // [4단계에서 해결할 문제] "겹치는 예약이 있나 조회 → 없으면 저장"은 두 단계로 나뉘어 있다.
        // 두 요청이 거의 동시에 들어오면 둘 다 조회에서 "없음"을 보고 둘 다 저장할 수 있다(중복 예약).
        // 3단계에서는 일부러 막지 않고 두어서, 4단계에서 이 문제를 재현하고 락·제약 조건으로 해결한다.
        boolean overlapping = reservationRepository.existsOverlapping(
                room.getId(), reservation.getStartAt(), reservation.getEndAt(), ReservationStatus.CONFIRMED);
        if (overlapping) {
            throw new ConflictException("이미 예약된 시간이에요.");
        }

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public AvailabilityResponse findAvailability(Long roomId, LocalDate date) {
        Room room = findActiveRoom(roomId);

        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today) || date.isAfter(today.plusDays(Reservation.BOOKABLE_DAYS))) {
            throw new InvalidValueException("오늘부터 " + Reservation.BOOKABLE_DAYS + "일 뒤까지만 조회할 수 있어요.");
        }

        // 그날 00:00부터 다음 날 00:00 전까지 걸친 확정 예약
        List<Reservation> reservations = reservationRepository.findOverlapping(
                roomId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), ReservationStatus.CONFIRMED);

        return AvailabilityResponse.of(date, room.getStudyCafe(), reservations, LocalDateTime.now(clock));
    }

    // 운영을 멈춘 방(INACTIVE)은 방 조회 API와 똑같이 없는 방으로 본다
    private Room findActiveRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .filter(r -> r.getStatus() == RoomStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다. id=" + roomId));
    }
}
