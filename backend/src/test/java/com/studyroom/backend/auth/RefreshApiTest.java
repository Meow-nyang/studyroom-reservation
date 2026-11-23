package com.studyroom.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.studyroom.backend.auth.refresh.RefreshTokenCookie;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.MemberRepository;
import com.studyroom.backend.member.Role;

import jakarta.servlet.http.Cookie;

/**
 * 실제 요청처럼 요청마다 커밋되게 하려고 @Transactional을 붙이지 않는다.
 * 대신 테스트마다 다른 이메일을 써서 서로 영향을 주지 않게 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RefreshApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    String email;

    @BeforeEach
    void setUp() {
        email = "refresh-" + UUID.randomUUID() + "@example.com";
        memberRepository.save(Member.builder()
                .email(email)
                .encodedPassword(passwordEncoder.encode("abcd1234"))
                .name("재발급")
                .role(Role.USER)
                .build());
    }

    @Test
    void loginSetsSafeRefreshCookie() throws Exception {
        String setCookie = login().getHeader(HttpHeaders.SET_COOKIE);

        assertThat(setCookie)
                .startsWith(RefreshTokenCookie.NAME + "=")
                .contains("HttpOnly", "SameSite=Strict", "Path=/api/auth", "Max-Age=604800");
    }

    @Test
    void refreshIssuesNewTokens() throws Exception {
        String oldToken = refreshCookieValue(login());

        MockHttpServletResponse response = refresh(oldToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse();

        // rotation: 새 refresh token은 이전 것과 달라야 한다
        assertThat(refreshCookieValue(response)).isNotEqualTo(oldToken);
    }

    @Test
    void usedRefreshTokenCannotBeReused() throws Exception {
        String oldToken = refreshCookieValue(login());
        refresh(oldToken).andExpect(status().isOk());

        refresh(oldToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
    }

    @Test
    void refreshWithoutCookie() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String token = refreshCookieValue(login());

        String clearCookie = mockMvc.perform(post("/api/auth/logout").cookie(new Cookie(RefreshTokenCookie.NAME, token)))
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(clearCookie).contains("Max-Age=0");

        refresh(token).andExpect(status().isUnauthorized());
    }

    // 같은 토큰으로 재발급 요청 10개가 동시에 와도 성공은 딱 1번이어야 한다 (rotation의 동시성)
    @Test
    void concurrentRefreshSucceedsOnlyOnce() throws Exception {
        String token = refreshCookieValue(login());
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            results.add(executor.submit(() -> {
                ready.countDown();
                start.await(); // 모든 스레드가 준비될 때까지 기다렸다가 한꺼번에 출발
                return refresh(token).andReturn().getResponse().getStatus();
            }));
        }
        ready.await();
        start.countDown();

        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> result : results) {
            statuses.add(result.get(10, TimeUnit.SECONDS));
        }
        executor.shutdown();

        assertThat(statuses).filteredOn(s -> s == 200).hasSize(1);
        assertThat(statuses).filteredOn(s -> s == 401).hasSize(threads - 1);
    }

    // 기기마다 따로 로그인 상태가 유지된다: 한쪽에서 로그아웃해도 다른 쪽은 재발급된다
    @Test
    void otherDeviceStaysLoggedIn() throws Exception {
        String laptop = refreshCookieValue(login());
        String phone = refreshCookieValue(login());

        mockMvc.perform(post("/api/auth/logout").cookie(new Cookie(RefreshTokenCookie.NAME, laptop)));

        refresh(phone).andExpect(status().isOk());
    }

    private MockHttpServletResponse login() throws Exception {
        String body = """
                {"email": "%s", "password": "abcd1234"}
                """.formatted(email);
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    private ResultActions refresh(String token) throws Exception {
        return mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie(RefreshTokenCookie.NAME, token)));
    }

    private static String refreshCookieValue(MockHttpServletResponse response) {
        Cookie cookie = response.getCookie(RefreshTokenCookie.NAME);
        assertThat(cookie).isNotNull();
        return cookie.getValue();
    }
}
