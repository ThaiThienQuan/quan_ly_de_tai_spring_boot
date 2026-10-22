package vn.edu.student.repository;

import vn.edu.student.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByStudentId(Long studentId);
    List<GroupMember> findByGroupId(Long groupId);
    long countByGroupId(Long groupId);
    boolean existsByStudentId(Long studentId);
    boolean existsByGroupIdAndStudentIdAndLeaderTrue(Long groupId, Long studentId);
    boolean existsByGroupIdAndStudentId(Long groupId, Long studentId);
}
