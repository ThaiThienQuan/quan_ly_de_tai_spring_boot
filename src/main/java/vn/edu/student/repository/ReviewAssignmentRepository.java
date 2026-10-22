package vn.edu.student.repository;

import vn.edu.student.entity.ReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {
    List<ReviewAssignment> findByCouncilIdAndTopicId(Long councilId, Long topicId);
    List<ReviewAssignment> findByCouncilId(Long councilId);
    List<ReviewAssignment> findByLecturerIdOrderByAssignmentIdDesc(Long lecturerId);
    boolean existsByCouncilIdAndTopicIdAndLecturerId(Long councilId, Long topicId, Long lecturerId);
}
