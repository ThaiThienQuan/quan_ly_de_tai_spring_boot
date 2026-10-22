package vn.edu.student.service;

import vn.edu.student.entity.UserAccount;
import vn.edu.student.repository.LecturerRepository;
import vn.edu.student.repository.StudentRepository;
import vn.edu.student.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
public class UserManagementService {
    private static final Set<String> ALLOWED_ROLES =
        Set.of("ADMIN", "TRUONG_KHOA", "CHU_TICH_HOI_DONG", "GIANG_VIEN", "SINH_VIEN");

    private final UserAccountRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserAccountRepository userRepository, StudentRepository studentRepository,
                                 LecturerRepository lecturerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return userRepository.findAllByOrderByUserIdAsc();
    }

    @Transactional(readOnly = true)
    public UserAccount findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
    }

    @Transactional
    public void updateAccess(Long userId, String role, String status, String actingUsername) {
        UserAccount account = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (role == null || !ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Vai trò tài khoản không hợp lệ.");
        }
        if (!"ACTIVE".equals(status) && !"LOCKED".equals(status)) {
            throw new IllegalArgumentException("Trạng thái tài khoản không hợp lệ.");
        }
        if (account.getUsername().equals(actingUsername)
                && (!"ADMIN".equals(role) || !"ACTIVE".equals(status))) {
            throw new IllegalArgumentException("Không thể tự khóa hoặc tự hạ quyền tài khoản đang đăng nhập.");
        }
        account.setRole(role);
        account.setStatus(status);
        account.setEnabled("ACTIVE".equals(status));
        userRepository.save(account);
    }

    @Transactional
    public void updateAccount(Long userId, String fullName, String email, String phone,
                              String role, String status, String actingUsername) {
        UserAccount account = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        String normalizedEmail = required(email, "Email").toLowerCase();
        if (normalizedEmail.length() > 100 || !normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ.");
        }
        if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(normalizedEmail, userId)) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
        }
        account.setFullName(required(fullName, "Họ và tên"));
        account.setEmail(normalizedEmail);
        account.setPhone(optional(phone, 20, "Số điện thoại"));
        validateAccess(account, role, status, actingUsername);
        account.setRole(role);
        account.setStatus(status);
        account.setEnabled("ACTIVE".equals(status));
        userRepository.save(account);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword, String confirmPassword) {
        UserAccount account = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(account);
    }

    @Transactional
    public void deleteUser(Long userId, String actingUsername) {
        UserAccount account = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (account.getUsername().equals(actingUsername)) {
            throw new IllegalArgumentException("Không thể xóa tài khoản đang đăng nhập.");
        }
        if ("ADMIN".equals(account.getRole()) && "ACTIVE".equals(account.getStatus())
                && userRepository.countByRoleAndStatus("ADMIN", "ACTIVE") <= 1) {
            throw new IllegalArgumentException("Không thể xóa quản trị viên đang hoạt động cuối cùng.");
        }
        studentRepository.findByUserId(userId).ifPresent(studentRepository::delete);
        lecturerRepository.findByUserId(userId).ifPresent(lecturerRepository::delete);
        userRepository.delete(account);
    }

    private void validateAccess(UserAccount account, String role, String status, String actingUsername) {
        if (role == null || !ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Vai trò tài khoản không hợp lệ.");
        }
        if (!"ACTIVE".equals(status) && !"LOCKED".equals(status)) {
            throw new IllegalArgumentException("Trạng thái tài khoản không hợp lệ.");
        }
        if (account.getUsername().equals(actingUsername)
                && (!"ADMIN".equals(role) || !"ACTIVE".equals(status))) {
            throw new IllegalArgumentException("Không thể tự khóa hoặc tự hạ quyền tài khoản đang đăng nhập.");
        }
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        String normalized = value.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException(label + " không được vượt quá 100 ký tự.");
        }
        return normalized;
    }

    private String optional(String value, int maxLength, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(label + " không được vượt quá " + maxLength + " ký tự.");
        }
        return normalized;
    }
}
