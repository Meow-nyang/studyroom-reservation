package com.studyroom.backend.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 테스트를 시작할 때마다 테스트 DB의 테이블을 모두 지우고 Flyway SQL을 처음부터 다시 실행한다.
 * 이전 실행에서 남은 데이터가 다음 실행에 영향을 주지 않게 한다. (예전 ddl-auto: create와 같은 효과)
 */
@Configuration
@Profile("test")
class TestFlywayConfig {

    @Bean
    FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
