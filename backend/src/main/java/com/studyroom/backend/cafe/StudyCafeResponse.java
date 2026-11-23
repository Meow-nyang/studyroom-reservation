package com.studyroom.backend.cafe;

import java.time.LocalTime;

public record StudyCafeResponse(
        Long id,
        String name,
        String address,
        String phone,
        LocalTime openTime,
        LocalTime closeTime) {

    public static StudyCafeResponse from(StudyCafe cafe) {
        return new StudyCafeResponse(
                cafe.getId(),
                cafe.getName(),
                cafe.getAddress(),
                cafe.getPhone(),
                cafe.getOpenTime(),
                cafe.getCloseTime());
    }
}
