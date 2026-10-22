package vn.edu.student.repository;

import vn.edu.student.entity.ProjectReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectReportRepository extends JpaRepository<ProjectReport, Long> {
    int countByGroupId(Long groupId);
    List<ProjectReport> findByGroupIdOrderByReportIdDesc(Long groupId);
}
