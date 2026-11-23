package com.studyroom.backend.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.common.exception.ConflictException;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.MemberRepository;
import com.studyroom.backend.member.MemberResponse;
import com.studyroom.backend.member.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final String DUPLICATE_EMAIL_MESSAGE = "이미 가입된 이메일이에요.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse signup(SignupRequest request) {
        String email = Member.normalizeEmail(request.email());
        if (memberRepository.existsByEmail(email)) {
            throw new ConflictException(DUPLICATE_EMAIL_MESSAGE);
        }

        Member member = Member.builder()
                .email(email)
                .encodedPassword(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                .build();

        try {
            // 위의 existsByEmail 검사와 저장 사이에 같은 이메일로 가입한 요청이 끼어들 수 있다.
            // 그때는 DB의 유니크 제약이 막아 주므로, 그 에러를 같은 409로 바꿔서 알려 준다.
            return MemberResponse.from(memberRepository.saveAndFlush(member));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(DUPLICATE_EMAIL_MESSAGE);
        }
    }
}
