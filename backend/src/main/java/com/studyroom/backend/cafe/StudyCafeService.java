package com.studyroom.backend.cafe;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyCafeService {

    private final StudyCafeRepository studyCafeRepository;

    public List<StudyCafeResponse> findAll() {
        return studyCafeRepository.findAll().stream()
                .map(StudyCafeResponse::from)
                .toList();
    }
}
