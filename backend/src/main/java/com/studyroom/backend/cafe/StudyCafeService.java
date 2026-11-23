package com.studyroom.backend.cafe;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.common.exception.NotFoundException;

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

    public StudyCafeResponse findById(Long cafeId) {
        return studyCafeRepository.findById(cafeId)
                .map(StudyCafeResponse::from)
                .orElseThrow(() -> new NotFoundException("지점을 찾을 수 없습니다. id=" + cafeId));
    }
}
