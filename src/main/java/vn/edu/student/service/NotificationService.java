package vn.edu.student.service;

import vn.edu.student.entity.Notification;
import vn.edu.student.entity.SystemRole;
import vn.edu.student.entity.UserAccount;
import vn.edu.student.repository.NotificationRepository;
import vn.edu.student.repository.SystemRoleRepository;
import vn.edu.student.repository.UserAccountRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {
    private static final Set<String> ALLOWED_ROLES =
        Set.of("ADMIN", "TRUONG_KHOA", "CHU_TICH_HOI_DONG", "GIANG_VIEN", "SINH_VIEN");
    private final NotificationRepository notificationRepository;
    private final SystemRoleRepository roleRepository;
    private final UserAccountRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public NotificationService(NotificationRepository notificationRepository, SystemRoleRepository roleRepository,
                               UserAccountRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.notificationRepository = notificationRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<Notification> findForUser(String username, boolean manager) {
        if (manager) return notificationRepository.findAllByOrderByCreatedAtDesc();
        UserAccount user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        return notificationRepository.findPublishedForRole(user.getRole());
    }

    @Transactional
    public Notification create(String username, String title, String content, String type, List<String> targetRoles) {
        UserAccount author = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề và nội dung thông báo không được để trống.");
        }
        if (title.length() > 250 || content.length() > 60000 || (type != null && type.length() > 20)) {
            throw new IllegalArgumentException("Tiêu đề hoặc loại thông báo vượt quá độ dài cho phép.");
        }
        if (type != null && !type.isBlank()
                && !Set.of("SYSTEM", "ACADEMIC", "DEADLINE").contains(type.trim())) {
            throw new IllegalArgumentException("Loại thông báo không hợp lệ.");
        }
        if (targetRoles == null || targetRoles.isEmpty()
                || targetRoles.stream().anyMatch(role -> role == null || !ALLOWED_ROLES.contains(role))) {
            throw new IllegalArgumentException("Phải chọn ít nhất một nhóm người nhận hợp lệ.");
        }
        List<String> uniqueRoles = targetRoles.stream().distinct().toList();
        List<SystemRole> roles = roleRepository.findByNameIn(uniqueRoles);
        if (roles.size() != uniqueRoles.size()) {
            throw new IllegalArgumentException("Không tìm thấy đầy đủ nhóm quyền người nhận.");
        }

        Notification notification = new Notification();
        notification.setTitle(title.trim());
        notification.setContent(content.trim());
        notification.setType(type == null || type.isBlank() ? "SYSTEM" : type.trim());
        notification.setCreatedBy(author.getUserId());
        notification.setStatus("DRAFT");
        notification = notificationRepository.save(notification);
        for (SystemRole role : roles) {
            jdbcTemplate.update("INSERT INTO notification_target_roles (notification_id, role_id) VALUES (?, ?)",
                notification.getNotificationId(), role.getRoleId());
        }
        return notification;
    }

    @Transactional
    public void publish(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo."));
        if (!"DRAFT".equals(notification.getStatus())) {
            throw new IllegalArgumentException("Chỉ thông báo ở trạng thái bản nháp mới được đăng tải.");
        }
        Integer targetCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_target_roles WHERE notification_id = ?", Integer.class, notificationId);
        if (targetCount == null || targetCount == 0) {
            throw new IllegalArgumentException("Thông báo phải có ít nhất một nhóm người nhận.");
        }
        notification.setStatus("PUBLISHED");
        notification.setPublishedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
