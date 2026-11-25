package com.studyroom.backend.reservation;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 이 방에 [startAt, endAt)과 겹치는 확정 예약이 있는지 본다.
     *
     * 두 시간 구간 A, B가 겹치는 조건: A의 시작 < B의 끝 그리고 B의 시작 < A의 끝
     * 끝 시각은 포함하지 않으므로 10:00~12:00과 12:00~13:00은 겹치지 않는다.
     */
    @Query("""
            select count(r) > 0
            from Reservation r
            where r.room.id = :roomId
              and r.status = :status
              and r.startAt < :endAt
              and :startAt < r.endAt
            """)
    boolean existsOverlapping(@Param("roomId") Long roomId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("status") ReservationStatus status);
}
