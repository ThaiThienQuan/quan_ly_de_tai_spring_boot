package vn.edu.student.repository;

import vn.edu.student.entity.CouncilMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, Long> {
    List<CouncilMember> findByCouncilId(Long councilId);
    List<CouncilMember> findByLecturerId(Long lecturerId);
    List<CouncilMember> findByCouncilIdIn(List<Long> councilIds);
    long countByCouncilId(Long councilId);
    boolean existsByCouncilIdAndLecturerId(Long councilId, Long lecturerId);
    boolean existsByCouncilIdAndLecturerIdAndRole(Long councilId, Long lecturerId, String role);
    Optional<CouncilMember> findByCouncilIdAndRole(Long councilId, String role);
}
