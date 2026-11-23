package com.studyroom.backend.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청. 비밀번호 형식은 검사하지 않는다. 저장된 값과 같은지만 본다.
 */
public record LoginRequest(

        @NotBlank(message = "이메일을 입력해 주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password) {
}
