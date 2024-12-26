package com.hhplus.hanghae_clean_arch;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.ApplicationStatus;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.LectureHistory;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureHistoryRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.repository.LectureRepository;
import com.hhplus.hanghae_clean_arch.biz.lecture.service.LectureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureHistoryRepository lectureHistoryRepository;

    @InjectMocks
    private LectureService lectureService;

    @Test
    @DisplayName("학생이 특강을 정상적으로 신청할 수 있다")
    void applyLecture_Success() {
        //given
        Lecture lecture = new Lecture("특강1", "강사1", 30, LocalDateTime.now());
        lecture.setId(1L);
        lecture.setCurrentEnrollment(5);

        Student student = new Student();
        student.setId(1L);
        student.setName("홍길동");

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(lectureHistoryRepository.save(any(LectureHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        lectureService.applyLecture(1L, student);

        //then
        assertEquals(6, lecture.getCurrentEnrollment());
        verify(lectureHistoryRepository, times(1)).save(any(LectureHistory.class));
    }

    @Test
    @DisplayName("동일한 학생은 동일한 강의를 중복 신청할 수 없다")
    void applyLecture_DuplicateApplication() {
        //given
        Lecture lecture = new Lecture("특강1", "강사1", 30, LocalDateTime.now());
        lecture.setId(1L);
        lecture.setCurrentEnrollment(5);

        Student student = new Student();
        student.setId(1L);
        student.setName("홍길동");

        LectureHistory existingHistory = new LectureHistory();
        existingHistory.setLecture(lecture);
        existingHistory.setStudent(student);

        //Mock: 해당 학생이 이미 강의에 신청한 기록이 있는 상황
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(lectureHistoryRepository.existsByLectureAndStudent(lecture, student)).thenReturn(true);

        //when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> lectureService.applyLecture(1L, student));

        assertEquals("이미 신청된 강의입니다.", exception.getMessage());
        verify(lectureHistoryRepository, never()).save(any(LectureHistory.class)); //저장 시도 안 함
    }

    @Test
    @DisplayName("신청 인원이 초과되면 특강 신청이 불가능하다")
    void applyLecture_OverCapacity() {
        //given
        Lecture lecture = new Lecture("특강1", "강사1", 30, LocalDateTime.now());
        lecture.setId(1L);
        lecture.setCurrentEnrollment(30);

        Student student = new Student();
        student.setId(1L);
        student.setName("홍길동");

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));

        //when then
        assertThrows(IllegalStateException.class, () -> lectureService.applyLecture(1L, student));
        verify(lectureHistoryRepository, never()).save(any(LectureHistory.class));
    }

    //동시성 테스트
    @Test
    @DisplayName("여러 사용자가 동시에 신청 시 동시성 문제 확인")
    void applyLecture_ConcurrencyIssue() throws InterruptedException {
        //given
        Lecture lecture = new Lecture("특강1", "강사1", 30, LocalDateTime.now());
        lecture.setId(1L);
        lecture.setCurrentEnrollment(22); //현재 인원

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));

        //스레드 동시 실행을 위한 설정
        int threadCount = 20; //동시 신청
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //성공,실패 카운트
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        //각 스레드에서 실행할 작업 정의
        for (int i = 0; i < threadCount; i++) {
            int studentId = i + 1;
            executorService.execute(() -> {
                try {
                    Student student = new Student();
                    student.setId((long) studentId);
                    student.setName("학생" + studentId);

                    lectureService.applyLecture(1L, student);
                    System.out.println("성공: 학생" + studentId);
                    successCount.incrementAndGet(); //성공 카운트 증가
                } catch (IllegalStateException e) {
                    System.out.println("실패: " + e.getMessage());
                    failureCount.incrementAndGet(); //실패 카운트 증가
                } finally {
                    latch.countDown(); //스레드 작업 완료
                }
            });
        }

        //모든 스레드가 작업을 완료할 때까지 대기
        latch.await();
        executorService.shutdown();

        //결과 확인
        System.out.println("성공한 사람 수: " + successCount.get());
        System.out.println("실패한 사람 수: " + failureCount.get());
        System.out.println("최종 신청 인원: " + lecture.getCurrentEnrollment());

        //then
        assertTrue(lecture.getCurrentEnrollment() <= 30, "정원이 초과되지 않아야 합니다.");
    }

    @Test
    void testGetCompletedLecturesByUserId() {
        //given
        Long userId = 1L;

        Lecture lecture1 = new Lecture("A특강", "김강사", 30, null);
        Lecture lecture2 = new Lecture("B특강", "이강사", 30, null);

        Student student = new Student();
        student.setId(userId);
        student.setName("홍길동");

        LectureHistory history1 = new LectureHistory();
        history1.setLecture(lecture1);
        history1.setStudent(student);

        LectureHistory history2 = new LectureHistory();
        history2.setLecture(lecture2);
        history2.setStudent(student);

        List<LectureHistory> histories = Arrays.asList(history1, history2);

        when(lectureHistoryRepository.findByStudentIdAndStatus(userId, ApplicationStatus.APPLIED))
                .thenReturn(histories); // 특정 유저의 완료된 강의 목록을 반환하도록 설정

        //when
        List<Lecture> completedLectures = lectureService.getCompletedLecturesByUserId(userId);

        //then
        assertNotNull(completedLectures);
        assertEquals(2, completedLectures.size()); // 반환된 특강 목록의 크기 확인
        assertEquals("A특강", completedLectures.get(0).getTitle()); // 첫 번째 특강 제목 확인
        assertEquals("김강사", completedLectures.get(0).getInstructor()); // 첫 번째 강연자 확인
    }

}
