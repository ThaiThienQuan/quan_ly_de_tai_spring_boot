package vn.edu.student.repository;
import vn.edu.student.entity.RegistrationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findAllByOrderByPeriodIdDesc();
}
