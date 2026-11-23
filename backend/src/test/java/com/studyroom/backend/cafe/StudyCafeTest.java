package com.studyroom.backend.cafe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.studyroom.backend.common.exception.InvalidValueException;

/**
 * Spring 없이 도는 순수 단위 테스트. 엔티티를 직접 만들어서 규칙 검사만 확인한다.
 */
class StudyCafeTest {

    @Test
    void createWithValidValues() {
        StudyCafe cafe = cafe("강남점", "서울 강남구", LocalTime.of(9, 0), LocalTime.of(23, 0));

        assertThat(cafe.getName()).isEqualTo("강남점");
        assertThat(cafe.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void rejectCloseTimeBeforeOpenTime() {
        assertThatThrownBy(() -> cafe("강남점", "서울 강남구", LocalTime.of(22, 0), LocalTime.of(2, 0)))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("닫는 시간은 여는 시간보다 늦어야 합니다.");
    }

    @Test
    void rejectSameOpenAndCloseTime() {
        assertThatThrownBy(() -> cafe("강남점", "서울 강남구", LocalTime.of(9, 0), LocalTime.of(9, 0)))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("닫는 시간은 여는 시간보다 늦어야 합니다.");
    }

    @Test
    void rejectTimeNotOnTheHour() {
        assertThatThrownBy(() -> cafe("강남점", "서울 강남구", LocalTime.of(9, 30), LocalTime.of(23, 0)))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("운영 시간은 정각 단위여야 합니다.");
    }

    @Test
    void rejectMissingTime() {
        assertThatThrownBy(() -> cafe("강남점", "서울 강남구", null, LocalTime.of(23, 0)))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("여는 시간은(는) 필수입니다.");
    }

    @Test
    void rejectBlankName() {
        assertThatThrownBy(() -> cafe("   ", "서울 강남구", LocalTime.of(9, 0), LocalTime.of(23, 0)))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("지점 이름은(는) 비어 있을 수 없습니다.");
    }

    @Test
    void rejectBlankAddress() {
        assertThatThrownBy(() -> cafe("강남점", "", LocalTime.of(9, 0), LocalTime.of(23, 0)))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("지점 주소은(는) 비어 있을 수 없습니다.");
    }

    private StudyCafe cafe(String name, String address, LocalTime open, LocalTime close) {
        return StudyCafe.builder()
                .name(name)
                .address(address)
                .openTime(open)
                .closeTime(close)
                .build();
    }
}
