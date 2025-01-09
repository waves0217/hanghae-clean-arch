package com.hhplus.hanghae_clean_arch.biz.lecture.repository;

import com.hhplus.hanghae_clean_arch.biz.lecture.domain.Lecture;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lecture l WHERE l.id = :id")
    Optional<Lecture> findByIdWithLock(@Param("id") Long id);
    /*@Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT l FROM Lecture l WHERE l.id = :lectureId")
    Optional<Lecture> findByIdWithLock(@Param("lectureId") Long lectureId);*/

}
