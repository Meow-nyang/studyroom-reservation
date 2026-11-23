package com.studyroom.backend.cafe;

import java.time.LocalTime;

import com.studyroom.backend.common.BaseTimeEntity;
import com.studyroom.backend.common.DomainValidator;
import com.studyroom.backend.common.exception.InvalidValueException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "study_cafe")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyCafe extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    @Builder
    private StudyCafe(String name, String address, String phone, LocalTime openTime, LocalTime closeTime) {
        DomainValidator.requireText(name, "지점 이름");
        DomainValidator.requireText(address, "지점 주소");
        validateOperatingHours(openTime, closeTime);

        this.name = name;
        this.address = address;
        this.phone = phone;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    // 예약을 1시간 단위로 받으므로 운영 시간은 정각이어야 하고, 자정을 넘기는 운영은 지원하지 않는다.
    private static void validateOperatingHours(LocalTime openTime, LocalTime closeTime) {
        DomainValidator.requireNonNull(openTime, "여는 시간");
        DomainValidator.requireNonNull(closeTime, "닫는 시간");
        if (!isOnTheHour(openTime) || !isOnTheHour(closeTime)) {
            throw new InvalidValueException("운영 시간은 정각 단위여야 합니다.");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new InvalidValueException("닫는 시간은 여는 시간보다 늦어야 합니다.");
        }
    }

    private static boolean isOnTheHour(LocalTime time) {
        return time.getMinute() == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }
}
