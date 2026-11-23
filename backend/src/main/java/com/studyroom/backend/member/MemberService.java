package com.studyroom.backend.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse findMe(Long memberId) {
        // 토큰은 유효한데 회원이 없는 경우(삭제됨 등)는 다시 로그인하게 한다
        return memberRepository.findById(memberId)
                .map(MemberResponse::from)
                .orElseThrow(() -> new UnauthorizedException("다시 로그인해 주세요."));
    }
}
