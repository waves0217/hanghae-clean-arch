package com.hhplus.hanghae_clean_arch.biz.lecture.repository;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

}
