package com.hhplus.hanghae_clean_arch;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.ApplicationStatus;
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
import java.util.Arrays;
import java.util.List;
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
    private StudentRepository studentRepository;

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
        //assertEquals(1, lectureHistoryRepository.count()); // 신청 기록이 1개인지 확인
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
        Lecture updatedLecture = lectureRepository.findById(lecture.getId()).orElseThrow();
        assertEquals(30, updatedLecture.getCurrentEnrollment()); // 최종 정원 확인
        assertEquals(5, successCount.get()); // 성공한 신청자 수
        assertEquals(2, failureCount.get()); // 실패한 신청자 수
    }

    @Test
    @DisplayName("특정 유저의 완료된 강의 목록 조회 - 통합 테스트")
    void getCompletedLecturesByUserIdTest() {
        // given
        Student student = new Student();
        student.setName("오오오");
        studentRepository.save(student);
        Long userId = student.getId();

        // 강의 2개 생성
        Lecture lecture1 = new Lecture("A특강", "김강사", 30, LocalDateTime.now());
        Lecture lecture2 = new Lecture("B특강", "김강사", 30, LocalDateTime.now());
        lectureRepository.saveAll(Arrays.asList(lecture1, lecture2));

        // 신청 기록 생성 (학생과 강의 매핑)
        LectureHistory history1 = new LectureHistory();
        history1.setLecture(lecture1);
        history1.setStudent(student);
        history1.setAppliedAt(LocalDateTime.now());
        history1.setStatus(ApplicationStatus.APPLIED);
        lectureHistoryRepository.save(history1);

        LectureHistory history2 = new LectureHistory();
        history2.setLecture(lecture2);
        history2.setStudent(student);
        history2.setAppliedAt(LocalDateTime.now());
        history2.setStatus(ApplicationStatus.APPLIED);
        lectureHistoryRepository.save(history2);

        // when
        List<Lecture> completedLectures = lectureService.getCompletedLecturesByUserId(userId);

        // then
        assertNotNull(completedLectures);
        assertEquals(2, completedLectures.size()); // 완료된 강의 목록 수가 2개인지 확인
        assertTrue(completedLectures.stream().anyMatch(lecture -> "A특강".equals(lecture.getTitle())));
        assertTrue(completedLectures.stream().anyMatch(lecture -> "B특강".equals(lecture.getTitle())));
    }

}

