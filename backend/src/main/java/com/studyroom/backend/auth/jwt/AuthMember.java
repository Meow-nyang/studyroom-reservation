package com.studyroom.backend.auth.jwt;

import com.studyroom.backend.member.Role;

/**
 * 토큰에서 꺼낸 "지금 요청한 사람". 컨트롤러에서 @AuthenticationPrincipal로 받는다.
 * DB를 조회하지 않고 토큰 안의 정보만으로 만든다.
 */
public record AuthMember(Long id, Role role) {
}
