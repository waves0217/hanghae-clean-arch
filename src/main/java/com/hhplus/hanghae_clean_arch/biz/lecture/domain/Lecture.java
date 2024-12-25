package com.hhplus.hanghae_clean_arch.biz.lecture.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String instructor;
    private int capacity;
    private int currentEnrollment;
    private LocalDateTime date;

    public Lecture(String title, String instructor, int capacity, LocalDateTime date) {
        this.title = title;
        this.instructor = instructor;
        this.capacity = capacity;
        this.currentEnrollment = 0; // 초기값 설정
        this.date = date;
    }

    @Version
    private int version;
}
