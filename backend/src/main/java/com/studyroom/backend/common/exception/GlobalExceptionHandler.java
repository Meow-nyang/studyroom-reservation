package com.studyroom.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * 컨트롤러에서 던져진 예외를 잡아서 {"message": "..."} 형식의 에러 응답으로 바꾼다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 일부러 던진 예외: 예외가 들고 있는 상태 코드 그대로 응답
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        return respond(e.getStatus(), e.getMessage());
    }

    // 요청 본문의 @Valid 검사 실패. 첫 번째로 걸린 항목의 메시지를 보여 준다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("요청 값이 올바르지 않습니다.");
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    // 요청 본문이 JSON이 아니거나 비어 있음
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
        return respond(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다.");
    }

    // 주소의 값 타입이 틀림. 예: /api/rooms/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return respond(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다.");
    }

    // 없는 주소. 예: /api/abc
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        return respond(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다.");
    }

    // 그 밖의 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        // 지원하지 않는 HTTP 메서드(405)처럼 Spring이 이미 상태 코드를 정해 둔 예외는 그 코드를 유지
        if (e instanceof org.springframework.web.ErrorResponse springError) {
            return respond(springError.getStatusCode(), "요청을 처리할 수 없습니다.");
        }
        // 정말 예상하지 못한 예외: 원인은 로그에만 남기고 사용자에게는 일반적인 문구만 보냄
        log.error("처리하지 못한 예외", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatusCode status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}
