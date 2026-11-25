package com.studyroom.backend.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 엔티티의 createdAt·updatedAt을 자동으로 채운다.
 * 채우는 시각도 ClockConfig의 한국 시간 기준을 쓴다.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider(Clock clock) {
        return () -> Optional.of(LocalDateTime.now(clock));
    }
}
