package com.studyroom.backend.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.cafe.StudyCafeRepository;

@SpringBootTest
@Transactional // 테스트가 끝나면 저장한 데이터를 롤백해서 DB에 남기지 않음
class RoomRepositoryTest {

    @Autowired
    StudyCafeRepository studyCafeRepository;

    @Autowired
    RoomRepository roomRepository;

    @Test
    void findRoomsOfCafe() {
        StudyCafe cafe = studyCafeRepository.save(StudyCafe.builder()
                .name("테스트점")
                .address("테스트 주소")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build());
        roomRepository.save(Room.builder().studyCafe(cafe).name("X룸").capacity(4).pricePerHour(8000).build());
        roomRepository.save(Room.builder().studyCafe(cafe).name("Y룸").capacity(6).pricePerHour(12000).build());

        List<Room> rooms = roomRepository.findByStudyCafeIdAndStatusOrderByIdAsc(cafe.getId(), RoomStatus.ACTIVE);

        // 저장한 순서(id 오름차순) 그대로 나와야 함
        assertThat(rooms).extracting(Room::getName).containsExactly("X룸", "Y룸");
    }
}
