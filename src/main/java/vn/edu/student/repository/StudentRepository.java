package vn.edu.student.repository;

import vn.edu.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByStudentCodeIgnoreCase(String studentCode);
    boolean existsByStudentCodeIgnoreCase(String studentCode);
}
