package com.hhplus.hanghae_clean_arch.biz.lecture.dto;

public class LectureRequestDto {
    private Long lectureId;
    private Long studentId;
    private String studentName;

    public LectureRequestDto() {}

    public LectureRequestDto(Long lectureId, Long studentId, String studentName) {
        this.lectureId = lectureId;
        this.studentId = studentId;
        this.studentName = studentName;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public void setLectureId(Long lectureId) {
        this.lectureId = lectureId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
