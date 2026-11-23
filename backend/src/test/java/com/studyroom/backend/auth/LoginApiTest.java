package com.studyroom.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.studyroom.backend.auth.jwt.AuthMember;
import com.studyroom.backend.auth.jwt.JwtProvider;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.MemberRepository;
import com.studyroom.backend.member.Role;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .email("login@example.com")
                .encodedPassword(passwordEncoder.encode("abcd1234"))
                .name("로그인")
                .role(Role.USER)
                .build());
    }

    @Test
    void loginSuccessReturnsAccessToken() throws Exception {
        String body = login("login@example.com", "abcd1234")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 받은 토큰을 풀어 보면 로그인한 회원의 id와 권한이 들어 있어야 한다
        String accessToken = JsonPath.read(body, "$.accessToken");
        AuthMember parsed = jwtProvider.parse(accessToken);
        assertThat(parsed.id()).isEqualTo(member.getId());
        assertThat(parsed.role()).isEqualTo(Role.USER);
    }

    @Test
    void loginIgnoresEmailCaseAndSpaces() throws Exception {
        login("  LOGIN@Example.com ", "abcd1234").andExpect(status().isOk());
    }

    @Test
    void wrongPassword() throws Exception {
        login("login@example.com", "wrong1234")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않아요."));
    }

    // 없는 이메일도 비밀번호가 틀렸을 때와 똑같이 응답해야 한다
    @Test
    void unknownEmailSameMessage() throws Exception {
        login("nobody@example.com", "abcd1234")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않아요."));
    }

    private ResultActions login(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
