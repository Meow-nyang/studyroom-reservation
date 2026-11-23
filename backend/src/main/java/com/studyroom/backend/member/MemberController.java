package com.studyroom.backend.member;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studyroom.backend.auth.jwt.AuthMember;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // JwtAuthenticationFilter가 토큰에서 꺼내 둔 AuthMember를 그대로 받는다
    @GetMapping("/me")
    public MemberResponse me(@AuthenticationPrincipal AuthMember authMember) {
        return memberService.findMe(authMember.id());
    }
}
