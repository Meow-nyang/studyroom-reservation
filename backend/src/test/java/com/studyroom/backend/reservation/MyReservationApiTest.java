package com.studyroom.backend.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.auth.jwt.JwtProvider;
import com.studyroom.backend.cafe.StudyCafe;
import com.studyroom.backend.cafe.StudyCafeRepository;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.MemberRepository;
import com.studyroom.backend.member.Role;
import com.studyroom.backend.room.Room;
import com.studyroom.backend.room.RoomRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyReservationApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudyCafeRepository studyCafeRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    Clock clock;

    Room room;
    Member me;
    Member other;
    LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        StudyCafe cafe = studyCafeRepository.save(StudyCafe.builder()
                .name("강남점")
                .address("서울 강남구")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(23, 0))
                .build());
        room = roomRepository.save(Room.builder()
                .studyCafe(cafe)
                .name("A룸")
                .capacity(4)
                .pricePerHour(8000)
                .build());
        me = memberRepository.save(member("me@example.com"));
        other = memberRepository.save(member("other@example.com"));
        tomorrow = LocalDate.now(clock).plusDays(1);
    }

    // 내 예약만, 시작 시각이 늦은 것부터 나온다
    @Test
    void findMineNewestFirst() throws Exception {
        Reservation early = reserve(me, tomorrow, 10);
        Reservation late = reserve(me, tomorrow.plusDays(1), 10);
        reserve(other, tomorrow, 15);

        mockMvc.perform(get("/api/reservations/me").header(HttpHeaders.AUTHORIZATION, bearer(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(late.getId()))
                .andExpect(jsonPath("$[1].id").value(early.getId()))
                .andExpect(jsonPath("$[0].cafeName").value("강남점"))
                .andExpect(jsonPath("$[0].roomName").value("A룸"));
    }

    @Test
    void findMineWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/reservations/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelMine() throws Exception {
        Reservation reservation = reserve(me, tomorrow, 10);

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    // 남의 예약은 "없는 예약"처럼 404
    @Test
    void cannotCancelOthers() throws Exception {
        Reservation othersReservation = reserve(other, tomorrow, 10);

        mockMvc.perform(patch("/api/reservations/{id}/cancel", othersReservation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(me)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을 찾을 수 없습니다. id=" + othersReservation.getId()));
    }

    @Test
    void rejectCancelTwice() throws Exception {
        Reservation reservation = reserve(me, tomorrow, 10);
        cancel(reservation, me);

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(me)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 취소된 예약이에요."));
    }

    // 취소된 예약은 겹침 검사에서 빠진다: 같은 시간을 다시 예약할 수 있고, 시간표에서도 열린다
    @Test
    void canceledTimeBecomesAvailable() throws Exception {
        Reservation reservation = reserve(me, tomorrow, 10);
        cancel(reservation, me);

        mockMvc.perform(get("/api/rooms/{roomId}/availability", room.getId()).param("date", tomorrow.toString()))
                .andExpect(jsonPath("$.slots[1].time").value("10:00"))
                .andExpect(jsonPath("$.slots[1].available").value(true));

        mockMvc.perform(post("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId": %d, "date": "%s", "startTime": "10:00", "hours": 1}
                                """.formatted(room.getId(), tomorrow)))
                .andExpect(status().isCreated());
    }

    private Reservation reserve(Member member, LocalDate date, int hour) {
        return reservationRepository.save(Reservation.create(member, room, date, LocalTime.of(hour, 0), 1,
                LocalDateTime.now(clock)));
    }

    private void cancel(Reservation reservation, Member member) throws Exception {
        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(member)))
                .andExpect(status().isOk());
    }

    private String bearer(Member member) {
        return "Bearer " + jwtProvider.createAccessToken(member.getId(), member.getRole());
    }

    private static Member member(String email) {
        return Member.builder()
                .email(email)
                .encodedPassword("{encoded}")
                .name("회원")
                .role(Role.USER)
                .build();
    }
}
