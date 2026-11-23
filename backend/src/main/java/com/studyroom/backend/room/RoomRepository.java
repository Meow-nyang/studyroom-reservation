package com.studyroom.backend.room;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByStudyCafeIdAndStatus(Long studyCafeId, RoomStatus status);
}
