package com.hhplus.hanghae_clean_arch.biz.lecture.repository;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.ApplicationStatus;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.LectureHistory;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureHistoryRepository extends JpaRepository<LectureHistory, Long> {
    boolean existsByLectureAndStudent(Lecture lecture, Student student);
    List<LectureHistory> findByStudentIdAndStatus(Long studentId, ApplicationStatus status);
}
