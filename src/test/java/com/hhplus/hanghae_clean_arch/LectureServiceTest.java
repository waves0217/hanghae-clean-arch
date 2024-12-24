package com.hhplus.hanghae_clean_arch;

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

import java.util.Optional;

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
        Lecture lecture = new Lecture();
        lecture.setId(1L);
        lecture.setTitle("tdd 특강");
        lecture.setInstructor("강사 A");
        lecture.setCapacity(30);
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
        // given
        Lecture lecture = new Lecture();
        lecture.setId(1L);
        lecture.setTitle("Spring Boot 특강");
        lecture.setCapacity(10);
        lecture.setCurrentEnrollment(5);

        Student student = new Student();
        student.setId(1L);
        student.setName("홍길동");

        LectureHistory existingHistory = new LectureHistory();
        existingHistory.setLecture(lecture);
        existingHistory.setStudent(student);

        // Mock: 해당 학생이 이미 강의에 신청한 기록이 있는 상황
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(lectureHistoryRepository.existsByLectureAndStudent(lecture, student)).thenReturn(true);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> lectureService.applyLecture(1L, student));

        assertEquals("이미 신청된 강의입니다.", exception.getMessage());
        verify(lectureHistoryRepository, never()).save(any(LectureHistory.class)); // 저장 시도 안 함
    }

    @Test
    @DisplayName("신청 인원이 초과되면 특강 신청이 불가능하다")
    void applyLecture_OverCapacity() {
        //given
        Lecture lecture = new Lecture();
        lecture.setId(1L);
        lecture.setTitle("tdd 특강");
        lecture.setInstructor("강사 A");
        lecture.setCapacity(30);
        lecture.setCurrentEnrollment(30);

        Student student = new Student();
        student.setId(1L);
        student.setName("홍길동");

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));

        //when then
        assertThrows(IllegalStateException.class, () -> lectureService.applyLecture(1L, student));
        verify(lectureHistoryRepository, never()).save(any(LectureHistory.class));
    }


}
