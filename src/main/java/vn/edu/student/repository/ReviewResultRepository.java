package vn.edu.student.repository;

import vn.edu.student.entity.ReviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewResultRepository extends JpaRepository<ReviewResult, Long> {
    Optional<ReviewResult> findByGroupIdAndTopicIdAndCouncilId(Long groupId, Long topicId, Long councilId);
    List<ReviewResult> findByGroupIdInAndPublishedTrueOrderByResultIdDesc(List<Long> groupIds);
}
