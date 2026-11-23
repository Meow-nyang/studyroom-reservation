package com.studyroom.backend.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 요청마다 한 번 실행되는 필터.
 * Authorization: Bearer <토큰> 헤더가 있으면 검증해서, 통과하면 "이 요청은 누구의 것"이라고 Spring Security에 알려 준다.
 *
 * 토큰이 없거나 틀려도 여기서 막지 않고 그냥 다음으로 넘긴다.
 * 로그인이 필요한 주소인지는 SecurityConfig의 규칙이 판단하고, 필요한데 인증이 없으면 그쪽에서 401을 보낸다.
 * 그래서 지점 조회처럼 누구나 되는 주소는 토큰이 틀려도 정상적으로 볼 수 있다.
 *
 * @Component를 붙이지 않는다. 붙이면 Spring Boot가 일반 서블릿 필터로도 한 번 더 등록해서 두 번 실행된다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            try {
                AuthMember member = jwtProvider.parse(header.substring(BEARER_PREFIX.length()));
                var authentication = new UsernamePasswordAuthenticationToken(
                        member, null, List.of(new SimpleGrantedAuthority("ROLE_" + member.role().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                // 인증 정보 없이 다음으로 넘긴다 (위 설명 참고)
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
