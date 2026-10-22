package vn.edu.student.repository;

import vn.edu.student.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();

    @Query(value = """
        SELECT n.* FROM notifications n
        JOIN notification_target_roles target ON target.notification_id = n.notification_id
        JOIN roles r ON r.role_id = target.role_id
        WHERE r.name = :roleName AND n.status = 'PUBLISHED'
        ORDER BY n.published_at DESC, n.notification_id DESC
        """, nativeQuery = true)
    List<Notification> findPublishedForRole(@Param("roleName") String roleName);
}
