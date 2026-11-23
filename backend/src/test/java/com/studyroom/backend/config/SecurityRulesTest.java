package com.studyroom.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * SecurityConfig의 주소별 접근 규칙을 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void anyoneCanReadCafes() throws Exception {
        mockMvc.perform(get("/api/cafes")).andExpect(status().isOk());
    }

    @Test
    void anyoneCanCheckHealth() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    // 조회는 GET만 열려 있다. 같은 주소라도 다른 방식은 로그인이 필요하다
    @Test
    void writingCafesNeedsLogin() throws Exception {
        mockMvc.perform(post("/api/cafes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요해요."));
    }

    @Test
    void unknownUrlNeedsLogin() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요해요."));
    }

    // 엉터리 토큰을 붙여도 누구나 되는 주소는 정상적으로 볼 수 있어야 한다
    @Test
    void invalidTokenStillReadsPublicApi() throws Exception {
        mockMvc.perform(get("/api/cafes").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokenOnProtectedApi() throws Exception {
        mockMvc.perform(get("/api/unknown").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }
}
