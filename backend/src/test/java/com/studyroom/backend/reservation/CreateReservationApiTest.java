package com.studyroom.backend.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
@Transactional // 테스트가 끝나면 저장한 데이터를 롤백
class CreateReservationApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudyCafeRepository studyCafeRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    Clock clock;

    Room room;
    Room otherRoom;
    String token;
    LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        StudyCafe cafe = studyCafeRepository.save(StudyCafe.builder()
                .name("강남점")
                .address("서울 강남구")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(23, 0))
                .build());
        room = roomRepository.save(room(cafe, "A룸"));
        otherRoom = roomRepository.save(room(cafe, "B룸"));

        Member member = memberRepository.save(Member.builder()
                .email("booker@example.com")
                .encodedPassword("{encoded}")
                .name("예약자")
                .role(Role.USER)
                .build());
        token = "Bearer " + jwtProvider.createAccessToken(member.getId(), member.getRole());

        // 실제 시계 기준 내일. 오늘로 하면 테스트를 돌리는 시각에 따라 "이미 시작된 시간"에 걸릴 수 있다.
        tomorrow = LocalDate.now(clock).plusDays(1);
    }

    @Test
    void createReservation() throws Exception {
        reserve(room.getId(), tomorrow, "10:00", 2)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cafeName").value("강남점"))
                .andExpect(jsonPath("$.roomId").value(room.getId()))
                .andExpect(jsonPath("$.roomName").value("A룸"))
                .andExpect(jsonPath("$.startAt").value(tomorrow + "T10:00"))
                .andExpect(jsonPath("$.endAt").value(tomorrow + "T12:00"))
                .andExpect(jsonPath("$.totalPrice").value(16000))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void rejectWithoutLogin() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(room.getId(), tomorrow, "10:00", 2)))
                .andExpect(status().isUnauthorized());
    }

    // 10:00~12:00이 있는데 11:00~12:00을 예약하면 겹친다
    @Test
    void rejectOverlapping() throws Exception {
        reserve(room.getId(), tomorrow, "10:00", 2).andExpect(status().isCreated());

        reserve(room.getId(), tomorrow, "11:00", 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 예약된 시간이에요."));
    }

    // 09:00~13:00이 10:00~12:00을 감싸는 경우도 겹친다
    @Test
    void rejectSurrounding() throws Exception {
        reserve(room.getId(), tomorrow, "10:00", 2).andExpect(status().isCreated());

        reserve(room.getId(), tomorrow, "09:00", 4).andExpect(status().isConflict());
    }

    // 끝 시각은 포함하지 않으므로 10:00~12:00 바로 뒤의 12:00~13:00은 괜찮다
    @Test
    void allowRightAfter() throws Exception {
        reserve(room.getId(), tomorrow, "10:00", 2).andExpect(status().isCreated());

        reserve(room.getId(), tomorrow, "12:00", 1).andExpect(status().isCreated());
    }

    @Test
    void allowSameTimeInOtherRoom() throws Exception {
        reserve(room.getId(), tomorrow, "10:00", 2).andExpect(status().isCreated());

        reserve(otherRoom.getId(), tomorrow, "10:00", 2).andExpect(status().isCreated());
    }

    @Test
    void rejectUnknownRoom() throws Exception {
        reserve(999_999L, tomorrow, "10:00", 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("방을 찾을 수 없습니다. id=999999"));
    }

    @Test
    void rejectBrokenRule() throws Exception {
        reserve(room.getId(), tomorrow, "10:00", 5)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이용 시간은 1~4시간이어야 해요."));
    }

    @Test
    void rejectMissingValue() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId": %d, "date": "%s", "startTime": "10:00"}
                                """.formatted(room.getId(), tomorrow)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이용 시간을 선택해 주세요."));
    }

    private ResultActions reserve(Long roomId, LocalDate date, String startTime, int hours) throws Exception {
        return mockMvc.perform(post("/api/reservations")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(roomId, date, startTime, hours)));
    }

    private static String body(Long roomId, LocalDate date, String startTime, int hours) {
        return """
                {"roomId": %d, "date": "%s", "startTime": "%s", "hours": %d}
                """.formatted(roomId, date, startTime, hours);
    }

    private static Room room(StudyCafe cafe, String name) {
        return Room.builder()
                .studyCafe(cafe)
                .name(name)
                .capacity(4)
                .pricePerHour(8000)
                .build();
    }
}
