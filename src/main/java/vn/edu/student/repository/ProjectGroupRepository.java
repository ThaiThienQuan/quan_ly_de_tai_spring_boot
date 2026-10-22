package vn.edu.student.repository;

import vn.edu.student.entity.ProjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Long> {
    List<ProjectGroup> findAllByOrderByGroupIdDesc();
    List<ProjectGroup> findByGroupIdInOrderByGroupIdDesc(List<Long> groupIds);
    boolean existsByGroupCodeIgnoreCase(String groupCode);
    boolean existsByTopicId(Long topicId);
    java.util.Optional<ProjectGroup> findByTopicId(Long topicId);
}
