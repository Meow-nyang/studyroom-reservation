package com.studyroom.backend.room;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.cafe.StudyCafeRepository;
import com.studyroom.backend.common.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final StudyCafeRepository studyCafeRepository;

    public List<RoomSummaryResponse> findRoomsOfCafe(Long cafeId) {
        if (!studyCafeRepository.existsById(cafeId)) {
            throw new NotFoundException("지점을 찾을 수 없습니다. id=" + cafeId);
        }
        return roomRepository.findByStudyCafeIdAndStatusOrderByIdAsc(cafeId, RoomStatus.ACTIVE).stream()
                .map(RoomSummaryResponse::from)
                .toList();
    }

    public RoomDetailResponse findRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .filter(r -> r.getStatus() == RoomStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다. id=" + roomId));
        return RoomDetailResponse.from(room);
    }
}
