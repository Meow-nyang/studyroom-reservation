package com.studyroom.backend.member;

import java.util.Locale;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.studyroom.backend.common.BaseTimeEntity;
import com.studyroom.backend.common.DomainValidator;
import com.studyroom.backend.common.exception.InvalidValueException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    private static final int NAME_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 원래 비밀번호가 아니라 BCrypt로 암호화한 값
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    private Member(String email, String encodedPassword, String name, Role role) {
        DomainValidator.requireText(email, "이메일");
        DomainValidator.requireText(encodedPassword, "비밀번호");
        DomainValidator.requireText(name, "이름");
        DomainValidator.requireNonNull(role, "권한");

        String trimmedName = name.strip();
        if (trimmedName.length() > NAME_MAX_LENGTH) {
            throw new InvalidValueException("이름은 " + NAME_MAX_LENGTH + "자 이하여야 합니다.");
        }

        this.email = normalizeEmail(email);
        this.password = encodedPassword;
        this.name = trimmedName;
        this.role = role;
    }

    /**
     * 가입과 로그인에서 같은 이메일을 같은 값으로 다루기 위해 앞뒤 공백을 없애고 소문자로 바꾼다.
     * "Admin@x.com"과 "admin@x.com"이 서로 다른 계정이 되지 않게 한다.
     */
    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
