package vn.edu.student.repository;

import vn.edu.student.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByAssignmentId(Long assignmentId);
    List<Evaluation> findByAssignmentIdIn(List<Long> assignmentIds);
}
