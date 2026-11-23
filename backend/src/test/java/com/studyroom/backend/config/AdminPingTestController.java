package com.studyroom.backend.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트 전용. 관리자 API는 6단계에서 만들기 때문에, /api/admin/** 권한 규칙을 확인할 주소를 테스트에서만 하나 둔다.
 * src/test에 있어서 실제 서버에는 포함되지 않는다.
 */
@RestController
class AdminPingTestController {

    @GetMapping("/api/admin/ping")
    String ping() {
        return "pong";
    }
}
