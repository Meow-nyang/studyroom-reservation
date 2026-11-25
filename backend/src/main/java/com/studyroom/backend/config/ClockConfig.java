package com.studyroom.backend.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * "지금 몇 시인지"를 한 곳에서 정한다.
 * 서버 컴퓨터의 시간대(예: AWS는 보통 UTC)와 상관없이 항상 한국 시간으로 계산한다.
 * LocalDateTime.now() 대신 LocalDateTime.now(clock)을 쓴다.
 */
@Configuration
public class ClockConfig {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(SEOUL);
    }
}
