package com.studyroom.backend.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.common.exception.InvalidValueException;

/**
 * Spring 없이 도는 순수 단위 테스트.
 */
class RoomTest {

    private final StudyCafe cafe = StudyCafe.builder()
            .name("강남점")
            .address("서울 강남구")
            .openTime(LocalTime.of(9, 0))
            .closeTime(LocalTime.of(23, 0))
            .build();

    @Test
    void createWithValidValuesIsActive() {
        Room room = room(cafe, "A룸", 4, 8000);

        assertThat(room.getStatus()).isEqualTo(RoomStatus.ACTIVE);
    }

    @Test
    void rejectMissingCafe() {
        assertThatThrownBy(() -> room(null, "A룸", 4, 8000))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("지점은(는) 필수입니다.");
    }

    @Test
    void rejectBlankName() {
        assertThatThrownBy(() -> room(cafe, " ", 4, 8000))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("방 이름은(는) 비어 있을 수 없습니다.");
    }

    @Test
    void rejectZeroCapacity() {
        assertThatThrownBy(() -> room(cafe, "A룸", 0, 8000))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("최대 인원은 1명 이상이어야 합니다.");
    }

    @Test
    void rejectZeroPrice() {
        assertThatThrownBy(() -> room(cafe, "A룸", 4, 0))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("시간당 가격은 0원보다 커야 합니다.");
    }

    private Room room(StudyCafe studyCafe, String name, int capacity, int pricePerHour) {
        return Room.builder()
                .studyCafe(studyCafe)
                .name(name)
                .capacity(capacity)
                .pricePerHour(pricePerHour)
                .build();
    }
}
