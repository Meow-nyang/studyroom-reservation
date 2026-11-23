package com.studyroom.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tools.jackson.databind.json.JsonMapper;

/**
 * Spring이 실제로 쓰는 JsonMapper가 정한 형식으로 시간을 바꾸는지 확인한다.
 */
@SpringBootTest
class JacksonConfigTest {

    @Autowired
    JsonMapper jsonMapper;

    record Sample(LocalTime time, LocalDateTime dateTime) {
    }

    @Test
    void writeTimeWithoutSeconds() {
        String json = jsonMapper.writeValueAsString(
                new Sample(LocalTime.of(9, 0), LocalDateTime.of(2026, 11, 23, 14, 0)));

        assertThat(json).isEqualTo("{\"time\":\"09:00\",\"dateTime\":\"2026-11-23T14:00\"}");
    }

    @Test
    void readSameFormat() {
        Sample sample = jsonMapper.readValue(
                "{\"time\":\"23:00\",\"dateTime\":\"2026-11-23T15:00\"}", Sample.class);

        assertThat(sample.time()).isEqualTo(LocalTime.of(23, 0));
        assertThat(sample.dateTime()).isEqualTo(LocalDateTime.of(2026, 11, 23, 15, 0));
    }
}
