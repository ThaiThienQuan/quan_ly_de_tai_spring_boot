package vn.edu.student.repository;

import vn.edu.student.entity.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {
    List<SystemRole> findByNameIn(List<String> names);
}
