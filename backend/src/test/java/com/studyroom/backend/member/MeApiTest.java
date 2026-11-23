package com.studyroom.backend.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.auth.jwt.JwtProvider;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Test
    void meWithoutToken() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요해요."));
    }

    @Test
    void meWithToken() throws Exception {
        Member member = save("me@example.com", Role.USER);

        mockMvc.perform(get("/api/members/me").header(HttpHeaders.AUTHORIZATION, bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.name").value("나"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void userCannotUseAdminApi() throws Exception {
        Member user = save("plain@example.com", Role.USER);

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("이 기능을 쓸 권한이 없어요."));
    }

    @Test
    void adminCanUseAdminApi() throws Exception {
        Member admin = save("boss@example.com", Role.ADMIN);

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk());
    }

    private Member save(String email, Role role) {
        return memberRepository.save(Member.builder()
                .email(email)
                .encodedPassword("{encoded}")
                .name("나")
                .role(role)
                .build());
    }

    private String bearer(Member member) {
        return "Bearer " + jwtProvider.createAccessToken(member.getId(), member.getRole());
    }
}
