-- 3단계: 예약 테이블

CREATE TABLE reservation (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    member_id   BIGINT      NOT NULL,
    room_id     BIGINT      NOT NULL,
    start_at    DATETIME(6) NOT NULL,
    end_at      DATETIME(6) NOT NULL,
    total_price INT         NOT NULL,   -- 예약한 순간의 시간당 가격 × 이용 시간 (나중에 방 가격이 바뀌어도 그대로)
    status      VARCHAR(20) NOT NULL,   -- CONFIRMED / CANCELED
    canceled_at DATETIME(6),            -- 취소한 시각. 취소하지 않았으면 NULL
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room (id),
    -- 겹침 검사는 항상 "이 방의 이 시간대"를 찾으므로 (방, 시작 시각) 순서로 색인을 만든다.
    -- 같은 방·같은 시간 중복을 DB가 막는 유니크 제약은 4단계에서 다른 방법들과 비교한다.
    INDEX idx_reservation_room_start (room_id, start_at)
);
