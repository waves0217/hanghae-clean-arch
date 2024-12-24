package com.hhplus.hanghae_clean_arch.biz.lecture.controller;

import com.hhplus.hanghae_clean_arch.biz.lecture.application.LectureApplicationFacade;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.dto.LectureRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lecture")
public class LectureController {

    private final LectureApplicationFacade lectureApplicationFacade;

    public LectureController(LectureApplicationFacade lectureApplicationFacade) {
        this.lectureApplicationFacade = lectureApplicationFacade;
    }

    @PostMapping("/apply")
    public ResponseEntity<String> applyLecture(@RequestBody LectureRequestDto requestDto) {
        Student student = new Student();
        student.setId(requestDto.getStudentId());
        student.setName(requestDto.getStudentName());

        lectureApplicationFacade.applyLecture(requestDto.getLectureId(), student);
        return ResponseEntity.ok("신청 완료");
    }

}
