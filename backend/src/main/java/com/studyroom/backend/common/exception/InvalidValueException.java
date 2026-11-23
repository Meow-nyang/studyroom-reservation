package com.studyroom.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인 규칙에 맞지 않는 값이 들어왔을 때. 예: 닫는 시간이 여는 시간보다 빠름
 */
public class InvalidValueException extends BusinessException {

    public InvalidValueException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
