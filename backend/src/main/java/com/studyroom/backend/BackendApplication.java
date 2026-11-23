package com.studyroom.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// Spring Security가 기본으로 만드는 임시 계정(user / 무작위 비밀번호)은 쓰지 않는다. 회원은 DB와 JWT로만 인증한다.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan // @ConfigurationProperties가 붙은 설정 클래스(JwtProperties 등)를 찾아서 등록
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
