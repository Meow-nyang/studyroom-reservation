package com.studyroom.backend.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.studyroom.backend.auth.jwt.JwtAuthenticationFilter;
import com.studyroom.backend.auth.jwt.JwtProvider;
import com.studyroom.backend.common.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProvider jwtProvider, JsonMapper jsonMapper)
            throws Exception {
        http
                // 세션·로그인 폼·기본 인증을 쓰지 않는다. 인증은 매 요청의 JWT로만 한다.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 주소별 접근 규칙. 위에서부터 차례로 맞춰 보고 처음 맞는 규칙을 쓴다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cafes/**", "/api/rooms/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                // 인증이 없으면 401, 권한이 모자라면 403. 다른 에러와 같은 {"message"} 형식으로 응답한다.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, e) ->
                                writeError(response, jsonMapper, HttpStatus.UNAUTHORIZED, "로그인이 필요해요."))
                        .accessDeniedHandler((request, response, e) ->
                                writeError(response, jsonMapper, HttpStatus.FORBIDDEN, "이 기능을 쓸 권한이 없어요.")))

                // 기본 로그인 필터 자리보다 앞에서 JWT를 확인한다
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 필터 단계의 에러는 @RestControllerAdvice까지 가지 않으므로 여기서 직접 JSON을 쓴다.
    private static void writeError(HttpServletResponse response, JsonMapper jsonMapper, HttpStatus status,
            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getWriter(), new ErrorResponse(message));
    }
}
