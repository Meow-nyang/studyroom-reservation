-- 1~2단계에서 JPA(ddl-auto)가 만들던 테이블을 그대로 옮긴 첫 번째 버전.
-- 이미 실행된 파일은 고치지 않는다. 바꿀 게 생기면 V2, V3... 새 파일을 추가한다.

CREATE TABLE study_cafe (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)  NOT NULL,
    address    VARCHAR(200) NOT NULL,
    phone      VARCHAR(20),
    open_time  TIME         NOT NULL,
    close_time TIME         NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE room (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    study_cafe_id  BIGINT       NOT NULL,
    name           VARCHAR(50)  NOT NULL,
    capacity       INT          NOT NULL,
    price_per_hour INT          NOT NULL,
    description    VARCHAR(500),
    status         VARCHAR(20)  NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_study_cafe FOREIGN KEY (study_cafe_id) REFERENCES study_cafe (id)
);

CREATE TABLE member (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    name       VARCHAR(20)  NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_email UNIQUE (email)
);

CREATE TABLE refresh_token (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_member FOREIGN KEY (member_id) REFERENCES member (id)
);
