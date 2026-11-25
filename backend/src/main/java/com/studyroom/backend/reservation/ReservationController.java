package com.studyroom.backend.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studyroom.backend.auth.jwt.AuthMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 이 주소들은 SecurityConfig의 anyRequest().authenticated()에 걸려서 로그인해야 쓸 수 있다
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody CreateReservationRequest request) {
        return reservationService.create(authMember.id(), request);
    }
}
