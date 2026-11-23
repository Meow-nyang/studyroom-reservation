package com.studyroom.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청. 형식 검사는 여기서(@Valid), 이름 길이처럼 공백을 정리한 뒤 봐야 하는 규칙은 엔티티에서 한다.
 */
public record SignupRequest(

        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 올바르지 않아요.")
        @Size(max = 100, message = "이메일은 100자 이하여야 해요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$",
                message = "비밀번호는 영문과 숫자를 포함해 8~20자여야 해요.")
        String password,

        @NotBlank(message = "이름을 입력해 주세요.")
        String name) {

    // 형식 검사(@Email)보다 먼저 실행된다. 앞뒤 공백 때문에 올바른 이메일이 거절되지 않게 미리 없앤다.
    public SignupRequest {
        email = email == null ? null : email.strip();
    }
}
