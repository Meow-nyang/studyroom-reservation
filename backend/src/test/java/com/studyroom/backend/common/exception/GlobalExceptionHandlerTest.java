package com.studyroom.backend.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import com.studyroom.backend.auth.jwt.JwtProvider;
import com.studyroom.backend.member.Role;

/**
 * 여러 종류의 에러가 모두 {"message": "..."} 형식과 알맞은 상태 코드로 응답되는지 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtProvider jwtProvider;

    @Test
    void notFoundEntity() throws Exception {
        mockMvc.perform(get("/api/rooms/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("방을 찾을 수 없습니다. id=999999"));
    }

    @Test
    void typeMismatch() throws Exception {
        mockMvc.perform(get("/api/rooms/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 값의 형식이 올바르지 않습니다."));
    }

    // 열어 두지 않은 주소는 로그인이 필요하므로, 로그인한 상태에서 없는 주소를 부르면 404
    @Test
    void unknownUrl() throws Exception {
        mockMvc.perform(get("/api/unknown").header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("요청한 주소를 찾을 수 없습니다."));
    }

    @Test
    void methodNotAllowedKeepsStatus() throws Exception {
        mockMvc.perform(delete("/api/cafes").header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").exists());
    }

    private String userToken() {
        return jwtProvider.createAccessToken(1L, Role.USER);
    }
}
