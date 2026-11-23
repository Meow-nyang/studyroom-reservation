package com.studyroom.backend.room;

/**
 * 방 목록 화면용. 목록에서는 설명처럼 긴 내용은 빼고 핵심 정보만 보낸다.
 */
public record RoomSummaryResponse(
        Long id,
        String name,
        int capacity,
        int pricePerHour) {

    public static RoomSummaryResponse from(Room room) {
        return new RoomSummaryResponse(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getPricePerHour());
    }
}
