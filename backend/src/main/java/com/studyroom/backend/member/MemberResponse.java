package com.studyroom.backend.member;

/**
 * 회원 정보 응답. 비밀번호(암호화한 값 포함)는 절대 내보내지 않는다.
 */
public record MemberResponse(
        Long id,
        String email,
        String name,
        Role role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getRole());
    }
}
