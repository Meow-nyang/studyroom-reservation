package com.studyroom.backend.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class AvailabilityApiTest {

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
    Clock clock;

    Room room;
    Member member;
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
        member = memberRepository.save(Member.builder()
                .email("booker@example.com")
                .encodedPassword("{encoded}")
                .name("예약자")
                .role(Role.USER)
                .build());
        tomorrow = LocalDate.now(clock).plusDays(1);
    }

    // 로그인하지 않아도 볼 수 있다. 10:00~12:00 예약이 있으면 slots[1](10시)·slots[2](11시)가 막힌다.
    @Test
    void showBookedSlotsWithoutLogin() throws Exception {
        reservationRepository.save(Reservation.create(member, room, tomorrow, LocalTime.of(10, 0), 2,
                LocalDateTime.now(clock)));

        mockMvc.perform(get("/api/rooms/{roomId}/availability", room.getId()).param("date", tomorrow.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(tomorrow.toString()))
                .andExpect(jsonPath("$.slots.length()").value(14))
                .andExpect(jsonPath("$.slots[0].time").value("09:00"))
                .andExpect(jsonPath("$.slots[0].available").value(true))
                .andExpect(jsonPath("$.slots[1].available").value(false))
                .andExpect(jsonPath("$.slots[2].available").value(false))
                .andExpect(jsonPath("$.slots[3].available").value(true))
                // 누가 예약했는지는 보내지 않는다
                .andExpect(jsonPath("$.slots[1].memberId").doesNotExist());
    }

    @Test
    void rejectDateAfterBookableDays() throws Exception {
        LocalDate tooFar = LocalDate.now(clock).plusDays(15);

        mockMvc.perform(get("/api/rooms/{roomId}/availability", room.getId()).param("date", tooFar.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("오늘부터 14일 뒤까지만 조회할 수 있어요."));
    }

    @Test
    void rejectBrokenDateFormat() throws Exception {
        mockMvc.perform(get("/api/rooms/{roomId}/availability", room.getId()).param("date", "11/26"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectUnknownRoom() throws Exception {
        mockMvc.perform(get("/api/rooms/{roomId}/availability", 999_999L).param("date", tomorrow.toString()))
                .andExpect(status().isNotFound());
    }
}
