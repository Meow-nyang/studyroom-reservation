package com.studyroom.backend.room;

import java.time.LocalTime;

import com.studyroom.backend.cafe.StudyCafe;

/**
 * 방 상세 화면용. 예약 시간을 고를 때 필요한 지점 운영 시간도 함께 보낸다.
 */
public record RoomDetailResponse(
        Long id,
        String name,
        int capacity,
        int pricePerHour,
        String description,
        Long cafeId,
        String cafeName,
        LocalTime openTime,
        LocalTime closeTime) {

    public static RoomDetailResponse from(Room room) {
        StudyCafe cafe = room.getStudyCafe();
        return new RoomDetailResponse(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getPricePerHour(),
                room.getDescription(),
                cafe.getId(),
                cafe.getName(),
                cafe.getOpenTime(),
                cafe.getCloseTime());
    }
}
