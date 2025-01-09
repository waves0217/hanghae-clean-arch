package com.hhplus.hanghae_clean_arch.biz.lecture.application;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.service.LectureService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LectureApplicationFacade {

    private final LectureService lectureService;

    public LectureApplicationFacade(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    public void applyLecture(Long lectureId, Student student) {
        lectureService.applyLecture(lectureId, student);
    }

    public List<Lecture> getCompletedLecturesByUserId(Long userId) { return lectureService.getCompletedLecturesByUserId(userId); }
}
