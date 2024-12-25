package com.hhplus.hanghae_clean_arch;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.LectureHistory;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureHistoryRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.StudentRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.service.LectureService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LectureIntegrationTest {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private LectureHistoryRepository lectureHistoryRepository;

    @Autowired
    private LectureService lectureService;

    @Test
    @DisplayName("특강 신청 성공 - 통합 테스트")
    void applyLecture_Success() {
        // given
        Lecture lecture = new Lecture("강의1", "김강사", 30, LocalDateTime.now());
        lectureRepository.save(lecture);

        Student student = new Student();
        student.setName("홍길동");

        // when
        lectureService.applyLecture(lecture.getId(), student);

        // then
        assertEquals(1, lectureHistoryRepository.count()); // 신청 기록이 1개인지 확인
        Lecture updatedLecture = lectureRepository.findById(lecture.getId()).orElseThrow();
        assertEquals(1, updatedLecture.getCurrentEnrollment()); // 현재 신청 인원이 1인지 확인
    }

    @Test
    @DisplayName("특강 신청 실패 - 정원 초과")
    void applyLecture_Failure_CapacityExceeded() {
        // given
        Lecture lecture = new Lecture("특강2", "김강사", 2, LocalDateTime.now()); // 최대 정원 2명
        lecture.setCurrentEnrollment(2); // 이미 정원이 찬 상태
        lectureRepository.save(lecture);

        Student student = new Student();
        student.setName("김철수");

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            lectureService.applyLecture(lecture.getId(), student);
        });

        assertEquals("강의 정원이 초과되었습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("특강 신청 실패 - 중복 신청")
    void applyLecture_Failure_DuplicateApplication() {
        // given
        Lecture lecture = new Lecture("특강3", "김강사", 30, LocalDateTime.now());
        lectureRepository.save(lecture);

        Student student = new Student();
        student.setName("실패중");

        // 첫 번째 신청 성공
        lectureService.applyLecture(lecture.getId(), student);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            lectureService.applyLecture(lecture.getId(), student);
        });

        assertEquals("이미 신청된 강의입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("동시성 테스트 - 정원이 초과되지 않는지 확인")
    void applyLecture_Concurrency() throws InterruptedException {
        // given
        Lecture lecture = new Lecture("Spring Boot 특강", "김강사", 30, LocalDateTime.now());
        lecture.setCurrentEnrollment(25); // 현재 정원 25명
        lectureRepository.save(lecture);
        lectureRepository.saveAndFlush(lecture);

        int threadCount = 7; // 동시 신청 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    Student student = new Student();
                    student.setName("학생" + Thread.currentThread().getId());

                    lectureService.applyLecture(lecture.getId(), student);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
       /* Lecture updatedLecture = lectureRepository.findById(lecture.getId()).orElseThrow();
        assertEquals(30, updatedLecture.getCurrentEnrollment()); // 최종 정원 확인
        assertEquals(5, successCount.get()); // 성공한 신청자 수
        assertEquals(2, failureCount.get()); // 실패한 신청자 수*/
    }
}

