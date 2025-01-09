package com.hhplus.hanghae_clean_arch.biz.lecture.service;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.ApplicationStatus;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.LectureHistory;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureHistoryRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LectureService {
    private final LectureRepository lectureRepository;
    private final LectureHistoryRepository lectureHistoryRepository;
    private final StudentRepository studentRepository;

    public LectureService(LectureRepository lectureRepository, LectureHistoryRepository lectureHistoryRepository, StudentRepository studentRepository) {
        this.lectureRepository = lectureRepository;
        this.lectureHistoryRepository = lectureHistoryRepository;
        this.studentRepository = studentRepository;
    }

    @Retryable(
            value = CannotAcquireLockException.class,
            maxAttempts = 3, // 최대 3회 재시도
            backoff = @Backoff(delay = 200) // 200ms 딜레이
    )
    @Transactional
    public void applyLecture(Long lectureId, Student student) {
        //강의 조회
        //Lecture lecture = lectureRepository.findById(lectureId)
        Lecture lecture = lectureRepository.findByIdWithLock(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의를 찾을 수 없습니다."));

        if (student.getId() == null) {
            student = studentRepository.save(student);
        }
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


    // 특강 신청 완료 목록 조회
    public List<Lecture> getCompletedLecturesByUserId(Long userId) {
        // 특정 유저가 신청한 완료된 특강 목록 조회
        List<LectureHistory> lectureHistories = lectureHistoryRepository.findByStudentIdAndStatus(userId, ApplicationStatus.APPLIED);

        // 결과에서 Lecture만 추출하여 반환
        return lectureHistories.stream()
                .map(LectureHistory::getLecture) // LectureHistory에서 Lecture 객체만 추출
                .collect(Collectors.toList());
    }


}
