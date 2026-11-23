package com.studyroom.backend.config;

import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.cafe.StudyCafeRepository;
import com.studyroom.backend.room.Room;
import com.studyroom.backend.room.RoomRepository;

import lombok.RequiredArgsConstructor;

/**
 * 개발용 초기 데이터. 지점이 하나도 없을 때만 넣어서, 앱을 여러 번 켜도 중복으로 쌓이지 않는다.
 */
@Component
@RequiredArgsConstructor
public class LocalDataInitializer implements ApplicationRunner {

    private final StudyCafeRepository studyCafeRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (studyCafeRepository.count() > 0) {
            return;
        }

        StudyCafe gangnam = studyCafeRepository.save(StudyCafe.builder()
                .name("강남점")
                .address("서울 강남구 테헤란로 123")
                .phone("02-555-1234")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(23, 0))
                .build());

        StudyCafe hongdae = studyCafeRepository.save(StudyCafe.builder()
                .name("홍대점")
                .address("서울 마포구 양화로 45")
                .phone("02-333-5678")
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .build());

        StudyCafe pangyo = studyCafeRepository.save(StudyCafe.builder()
                .name("판교점")
                .address("경기 성남시 분당구 판교역로 67")
                .phone("031-777-9012")
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(22, 0))
                .build());

        roomRepository.saveAll(List.of(
                room(gangnam, "A룸", 4, 8000, "화이트보드, 모니터"),
                room(gangnam, "B룸", 6, 12000, "화이트보드, 빔프로젝터"),
                room(gangnam, "C룸", 10, 20000, "대형 테이블, 빔프로젝터"),
                room(hongdae, "1번방", 2, 5000, "2인 집중실"),
                room(hongdae, "2번방", 4, 8000, "화이트보드"),
                room(pangyo, "세미나룸", 12, 25000, "빔프로젝터, 마이크"),
                room(pangyo, "포커스룸", 1, 3000, "1인 집중실")));
    }

    private Room room(StudyCafe cafe, String name, int capacity, int pricePerHour, String description) {
        return Room.builder()
                .studyCafe(cafe)
                .name(name)
                .capacity(capacity)
                .pricePerHour(pricePerHour)
                .description(description)
                .build();
    }
}
