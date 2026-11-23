package com.studyroom.backend.auth.refresh;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 재발급할 때 회원의 id와 권한이 꼭 필요하므로, 토큰을 조회하면서 회원도 함께 가져온다(JOIN)
    @EntityGraph(attributePaths = "member")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * DELETE 쿼리 한 번으로 지우고, 실제로 지운 줄 수를 돌려준다.
     * 같은 토큰으로 재발급 요청이 동시에 두 번 오면, DB가 한쪽만 1줄을 지우게 해 주고 다른 쪽은 0줄이 된다.
     * 그래서 "0이면 이미 누가 썼다"로 판단할 수 있다.
     */
    @Modifying
    @Query("delete from RefreshToken r where r.id = :id")
    int deleteByIdAndCount(@Param("id") Long id);

    @Modifying
    @Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
    int deleteByTokenHash(@Param("tokenHash") String tokenHash);
}
