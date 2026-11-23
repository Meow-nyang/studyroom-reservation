package com.studyroom.backend.config;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * API 전체의 날짜·시간 JSON 형식을 한 곳에서 정한다. (응답으로 보낼 때와 요청으로 받을 때 모두 적용)
 * 예약이 1시간 단위라서 초 단위는 쓰지 않는다.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer dateTimeFormatCustomizer() {
        return builder -> builder
                // 09:00
                .withConfigOverride(LocalTime.class,
                        override -> override.setFormat(JsonFormat.Value.forPattern("HH:mm")))
                // 2026-11-23T09:00
                .withConfigOverride(LocalDateTime.class,
                        override -> override.setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd'T'HH:mm")));
    }
}
