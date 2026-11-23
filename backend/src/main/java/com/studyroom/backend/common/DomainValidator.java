package com.studyroom.backend.common;

import com.studyroom.backend.common.exception.InvalidValueException;

/**
 * 여러 엔티티에서 같이 쓰는 값 검사.
 */
public final class DomainValidator {

    private DomainValidator() {
    }

    // null이거나, 비어 있거나, 공백만 있으면 예외
    public static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException(fieldName + "은(는) 비어 있을 수 없습니다.");
        }
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidValueException(fieldName + "은(는) 필수입니다.");
        }
    }
}
