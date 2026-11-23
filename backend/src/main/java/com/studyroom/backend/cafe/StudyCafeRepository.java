package com.studyroom.backend.cafe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyCafeRepository extends JpaRepository<StudyCafe, Long> {

    List<StudyCafe> findAllByOrderByIdAsc();
}
