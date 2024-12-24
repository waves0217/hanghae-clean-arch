package com.hhplus.hanghae_clean_arch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplus.hanghae_clean_arch.biz.lecture.application.LectureApplicationFacade;
import com.hhplus.hanghae_clean_arch.biz.lecture.controller.LectureController;
import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import com.hhplus.hanghae_clean_arch.biz.lecture.dto.LectureRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@WebMvcTest(LectureController.class)
class LectureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LectureApplicationFacade lectureApplicationFacade;

    @Test
    @DisplayName("특강 신청 성공 - 200 OK")
    void applyLecture_Success() throws Exception {
        // given
        LectureRequestDto requestDto = new LectureRequestDto(1L, 1L, "홍길동");
        String requestJson = new ObjectMapper().writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/lecture/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("신청 완료"));
    }

    @Test
    @DisplayName("특강 신청 실패 - 정원 초과 - 400 Bad Request")
    void applyLecture_Failure_CapacityExceeded() throws Exception {
        // given
        LectureRequestDto requestDto = new LectureRequestDto(1L, 1L, "홍길동");
        String requestJson = new ObjectMapper().writeValueAsString(requestDto);

        doThrow(new IllegalStateException("강의 정원이 초과되었습니다."))
                .when(lectureApplicationFacade)
                .applyLecture(anyLong(), any(Student.class));

        // when & then
        mockMvc.perform(post("/lecture/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("강의 정원이 초과되었습니다."));
    }

    @Test
    @DisplayName("특강 신청 실패 - 강의가 존재하지 않음 - 400 Bad Request")
    void applyLecture_Failure_LectureNotFound() throws Exception {
        // given
        LectureRequestDto requestDto = new LectureRequestDto(1L, 1L, "홍길동");
        String requestJson = new ObjectMapper().writeValueAsString(requestDto);

        doThrow(new IllegalArgumentException("해당 강의를 찾을 수 없습니다."))
                .when(lectureApplicationFacade)
                .applyLecture(anyLong(), any(Student.class));

        // when & then
        mockMvc.perform(post("/lecture/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("해당 강의를 찾을 수 없습니다."));
    }
}
