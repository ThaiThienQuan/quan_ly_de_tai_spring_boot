package vn.edu.student.repository;

import vn.edu.student.entity.TopicRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRegistrationRepository extends JpaRepository<TopicRegistration, Long> {
    boolean existsByGroupIdAndStatusIn(Long groupId, List<String> statuses);
    boolean existsByGroupIdAndTopicIdAndStatusIn(Long groupId, Long topicId, List<String> statuses);
    List<TopicRegistration> findByTopicIdAndStatusIn(Long topicId, List<String> statuses);
    List<TopicRegistration> findByStatusOrderByRegistrationIdDesc(String status);
}
