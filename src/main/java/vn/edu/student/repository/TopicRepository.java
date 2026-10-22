package vn.edu.student.repository;
import vn.edu.student.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findAllByOrderByTopicIdDesc();
    List<Topic> findByPublishedTrueAndStatus(String status);
    boolean existsByPeriodIdAndTopicCodeIgnoreCase(Long periodId, String topicCode);
}
