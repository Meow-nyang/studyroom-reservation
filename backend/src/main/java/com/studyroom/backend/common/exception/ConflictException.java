package com.studyroom.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 이미 있는 것과 충돌할 때. 예: 이미 가입된 이메일, (3단계) 이미 예약된 시간
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
