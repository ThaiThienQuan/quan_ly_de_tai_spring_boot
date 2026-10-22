package vn.edu.student.repository;

import vn.edu.student.entity.TopicSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicSupervisorRepository extends JpaRepository<TopicSupervisor, Long> {
    boolean existsByTopicIdAndLecturerId(Long topicId, Long lecturerId);
    long countByTopicId(Long topicId);
    boolean existsByTopicIdAndSupervisorOrder(Long topicId, int supervisorOrder);
}
