package com.hhplus.hanghae_clean_arch.biz.lecture.application;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.service.LectureService;
import org.springframework.stereotype.Component;

@Component
public class LectureApplicationFacade {

    private final LectureService lectureService;

    public LectureApplicationFacade(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    public void applyLecture(Long lectureId, Student student) {
        lectureService.applyLecture(lectureId, student);
    }
}
