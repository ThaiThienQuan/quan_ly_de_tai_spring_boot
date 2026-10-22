package vn.edu.student.service;

import vn.edu.student.entity.Student;
import vn.edu.student.entity.UserAccount;
import vn.edu.student.repository.StudentRepository;
import vn.edu.student.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final UserAccountRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepository userRepository, StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerStudent(String username, String fullName, String email, String phone,
                                String studentCode, String className, String major,
                                String password, String confirmPassword) {
        String normalizedUsername = required(username, "Tên đăng nhập", 50);
        String normalizedEmail = required(email, "Email", 100).toLowerCase();
        String normalizedStudentCode = required(studentCode, "Mã sinh viên", 20);
        validateEmail(normalizedEmail);
        String normalizedPassword = validatePassword(password, confirmPassword);
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
        if (studentRepository.existsByStudentCodeIgnoreCase(normalizedStudentCode)) {
            throw new IllegalArgumentException("Mã sinh viên đã được sử dụng.");
        }

        UserAccount account = new UserAccount();
        account.setUsername(normalizedUsername);
        account.setPassword(passwordEncoder.encode(normalizedPassword));
        account.setFullName(required(fullName, "Họ và tên", 100));
        account.setEmail(normalizedEmail);
        account.setPhone(optional(phone, 20, "Số điện thoại"));
        account.setRole("SINH_VIEN");
        account.setStatus("ACTIVE");
        account.setEnabled(true);
        UserAccount savedAccount = userRepository.save(account);

        Student student = new Student();
        student.setUserId(savedAccount.getUserId());
        student.setStudentCode(normalizedStudentCode);
        student.setClassName(required(className, "Lớp", 50));
        student.setMajor(required(major, "Chuyên ngành", 100));
        studentRepository.save(student);
    }

    @Transactional
    public void updateProfile(String username, String fullName, String email, String phone) {
        UserAccount account = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        String normalizedEmail = required(email, "Email", 100).toLowerCase();
        validateEmail(normalizedEmail);
        if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(normalizedEmail, account.getUserId())) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
        }
        account.setFullName(required(fullName, "Họ và tên", 100));
        account.setEmail(normalizedEmail);
        account.setPhone(optional(phone, 20, "Số điện thoại"));
        userRepository.save(account);
    }

    @Transactional
    public void changePassword(String username, String currentPassword,
                               String newPassword, String confirmPassword) {
        UserAccount account = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }
        account.setPassword(passwordEncoder.encode(validatePassword(newPassword, confirmPassword)));
        userRepository.save(account);
    }

    private String validatePassword(String password, String confirmPassword) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
        return password;
    }

    private String required(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(label + " không được vượt quá " + maxLength + " ký tự.");
        }
        return normalized;
    }

    private String optional(String value, int maxLength, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return required(value, label, maxLength);
    }

    private void validateEmail(String email) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ.");
        }
    }
}
