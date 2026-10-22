package vn.edu.student.repository;
import vn.edu.student.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByDepartmentCodeIgnoreCase(String departmentCode);
    boolean existsByDepartmentCodeIgnoreCaseAndDepartmentIdNot(String departmentCode, Long departmentId);
}
