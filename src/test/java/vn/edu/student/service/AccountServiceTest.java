package vn.edu.student.service;

import vn.edu.student.entity.Student;
import vn.edu.student.entity.UserAccount;
import vn.edu.student.repository.StudentRepository;
import vn.edu.student.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private UserAccountRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AccountService service;

    @Test
    void registersStudentAccountAndProfile() {
        when(userRepository.save(any(UserAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");

        service.registerStudent("sv-new", "Sinh Vien Moi", "sv-new@example.edu", "0901234567",
            "SV2026999", "CNTT-K20A", "Computer Science", "Password123", "Password123");

        ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(accountCaptor.capture());
        assertEquals("SINH_VIEN", accountCaptor.getValue().getRole());
        assertEquals("ACTIVE", accountCaptor.getValue().getStatus());
        assertEquals("encoded-password", accountCaptor.getValue().getPassword());

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        assertEquals("SV2026999", studentCaptor.getValue().getStudentCode());
        assertEquals("CNTT-K20A", studentCaptor.getValue().getClassName());
        assertEquals("Computer Science", studentCaptor.getValue().getMajor());
    }

    @Test
    void rejectsMismatchedRegistrationPasswordsBeforeWriting() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.registerStudent("sv-new", "Sinh Vien Moi", "sv-new@example.edu", null,
                "SV2026999", "CNTT-K20A", "Computer Science", "Password123", "Password456"));

        assertEquals("Mật khẩu xác nhận không khớp.", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
    }
}
