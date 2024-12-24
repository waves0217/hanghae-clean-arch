package com.hhplus.hanghae_clean_arch.biz.lecture.service;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.ApplicationStatus;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.LectureHistory;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureHistoryRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LectureService {
    private final LectureRepository lectureRepository;
    private final LectureHistoryRepository lectureHistoryRepository;

    public LectureService(LectureRepository lectureRepository, LectureHistoryRepository lectureHistoryRepository) {
        this.lectureRepository = lectureRepository;
        this.lectureHistoryRepository = lectureHistoryRepository;
    }

    public void applyLecture(Long lectureId, Student student) {
        //강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의를 찾을 수 없습니다."));

        //중복 신청 확인
        if (lectureHistoryRepository.existsByLectureAndStudent(lecture, student)) {
            throw new IllegalStateException("이미 신청된 강의입니다.");
        }
        
        //정원 초과 확인
        if (lecture.getCurrentEnrollment() >= lecture.getCapacity()) {
            throw new IllegalStateException("강의 정원이 초과되었습니다.");
        }

        //신청 기록 생성 및 저장
        LectureHistory history = new LectureHistory();
        history.setLecture(lecture);
        history.setStudent(student);
        history.setAppliedAt(LocalDateTime.now());
        history.setStatus(ApplicationStatus.APPLIED);
        lectureHistoryRepository.save(history);

        //현재 신청 인원 증가
        lecture.setCurrentEnrollment(lecture.getCurrentEnrollment() + 1);
        lectureRepository.save(lecture);
    }
}
