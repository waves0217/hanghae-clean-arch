package com.hhplus.hanghae_clean_arch.biz.lecture.repository;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
