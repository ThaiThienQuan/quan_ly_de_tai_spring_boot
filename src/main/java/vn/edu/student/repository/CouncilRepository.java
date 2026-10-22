package vn.edu.student.repository;

import vn.edu.student.entity.Council;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouncilRepository extends JpaRepository<Council, Long> {
    List<Council> findAllByOrderByReportDateDesc();
    boolean existsByCouncilCodeIgnoreCase(String councilCode);
}
