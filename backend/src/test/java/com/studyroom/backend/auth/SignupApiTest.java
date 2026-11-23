package com.studyroom.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SignupApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void signupSuccess() throws Exception {
        signup("  New@Example.com ", "abcd1234", " 홍길동 ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Member saved = memberRepository.findByEmail("new@example.com").orElseThrow();
        // 비밀번호는 원문이 아니라 암호화된 값으로 저장돼야 한다
        assertThat(saved.getPassword()).isNotEqualTo("abcd1234");
        assertThat(passwordEncoder.matches("abcd1234", saved.getPassword())).isTrue();
    }

    @Test
    void duplicateEmailIgnoringCase() throws Exception {
        signup("dup@example.com", "abcd1234", "첫번째").andExpect(status().isCreated());

        signup("DUP@example.com", "abcd1234", "두번째")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일이에요."));
    }

    @Test
    void rejectPasswordWithoutDigit() throws Exception {
        signup("pw@example.com", "abcdefgh", "홍길동")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 영문과 숫자를 포함해 8~20자여야 해요."));
    }

    @Test
    void rejectInvalidEmail() throws Exception {
        signup("not-an-email", "abcd1234", "홍길동")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않아요."));
    }

    @Test
    void rejectTooLongName() throws Exception {
        signup("long@example.com", "abcd1234", "가".repeat(21))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이름은 20자 이하여야 합니다."));
    }

    private ResultActions signup(String email, String password, String name) throws Exception {
        String body = """
                {"email": "%s", "password": "%s", "name": "%s"}
                """.formatted(email, password, name);
        return mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
