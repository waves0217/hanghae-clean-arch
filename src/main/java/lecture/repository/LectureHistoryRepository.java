package lecture.repository;

import lecture.domain.Lecture;
import lecture.domain.LectureHistory;
import lecture.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureHistoryRepository extends JpaRepository<LectureHistory, Long> {
    boolean existsByLectureAndStudent(Lecture lecture, Student student);
}
