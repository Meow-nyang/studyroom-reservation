package com.studyroom.backend.room;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/cafes/{cafeId}/rooms")
    public List<RoomSummaryResponse> findRoomsOfCafe(@PathVariable Long cafeId) {
        return roomService.findRoomsOfCafe(cafeId);
    }

    @GetMapping("/rooms/{roomId}")
    public RoomDetailResponse findRoom(@PathVariable Long roomId) {
        return roomService.findRoom(roomId);
    }
}
