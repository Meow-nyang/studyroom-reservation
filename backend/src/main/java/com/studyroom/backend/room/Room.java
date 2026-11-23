package com.studyroom.backend.room;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_cafe_id", nullable = false)
    private StudyCafe studyCafe;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int pricePerHour;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Builder
    private Room(StudyCafe studyCafe, String name, int capacity, int pricePerHour, String description) {
        this.studyCafe = studyCafe;
        this.name = name;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.description = description;
        this.status = RoomStatus.ACTIVE;
    }
}
