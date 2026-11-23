package com.studyroom.backend.cafe;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cafes")
@RequiredArgsConstructor
public class StudyCafeController {

    private final StudyCafeService studyCafeService;

    @GetMapping
    public List<StudyCafeResponse> findAll() {
        return studyCafeService.findAll();
    }

    @GetMapping("/{cafeId}")
    public StudyCafeResponse findById(@PathVariable Long cafeId) {
        return studyCafeService.findById(cafeId);
    }
}
