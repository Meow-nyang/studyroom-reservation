package com.studyroom.backend.reservation;

import java.time.LocalDateTime;

import com.studyroom.backend.room.Room;

/**
 * 예약 한 건. 내 예약 목록에서 바로 보여 줄 수 있게 지점·방 이름을 함께 보낸다.
 */
public record ReservationResponse(
        Long id,
        String cafeName,
        Long roomId,
        String roomName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int totalPrice,
        ReservationStatus status) {

    public static ReservationResponse from(Reservation reservation) {
        Room room = reservation.getRoom();
        return new ReservationResponse(
                reservation.getId(),
                room.getStudyCafe().getName(),
                room.getId(),
                room.getName(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getTotalPrice(),
                reservation.getStatus());
    }
}
