package vn.edu.student.repository;

import vn.edu.student.entity.ProjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select projectGroup from ProjectGroup projectGroup where projectGroup.groupId = :groupId")
    Optional<ProjectGroup> findByIdForUpdate(@Param("groupId") Long groupId);
    List<ProjectGroup> findAllByOrderByGroupIdDesc();
    List<ProjectGroup> findByGroupIdInOrderByGroupIdDesc(List<Long> groupIds);
    boolean existsByGroupCodeIgnoreCase(String groupCode);
    boolean existsByTopicId(Long topicId);
    java.util.Optional<ProjectGroup> findByTopicId(Long topicId);
}
