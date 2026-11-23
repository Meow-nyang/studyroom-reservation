package com.studyroom.backend.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 우리 서비스의 규칙에 따라 일부러 던지는 예외의 부모.
 * 응답할 HTTP 상태 코드를 직접 들고 있어서, 핸들러는 이 예외 하나만 처리하면 된다.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;

    protected BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
