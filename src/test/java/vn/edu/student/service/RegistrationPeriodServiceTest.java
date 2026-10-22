package vn.edu.student.service;

import vn.edu.student.entity.RegistrationPeriod;
import vn.edu.student.repository.RegistrationPeriodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodServiceTest {
    @Mock
    private RegistrationPeriodRepository repository;

    @InjectMocks
    private RegistrationPeriodService service;

    @Test
    void createsValidGraduationPeriodAsDraft() {
        when(repository.save(any(RegistrationPeriod.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        RegistrationPeriod period = validGraduationPeriod();

        RegistrationPeriod created = service.create(period, 12L);

        assertEquals("DRAFT", created.getStatus());
        assertEquals(12L, created.getCreatedBy());
        assertNotNull(created.getReviewDeadline());
        assertNotNull(created.getCouncilReportDate());
    }

    @Test
    void rejectsStudentPhaseStartingAtLecturerPhaseEnd() {
        RegistrationPeriod period = validGraduationPeriod();
        period.setStudentStartAt(LocalDateTime.of(2026, 10, 5, 0, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.create(period, 12L));

        assertTrue(exception.getMessage().contains("Giai đoạn sinh viên"));
        verify(repository, never()).save(any());
    }

    @Test
    void requiresReviewDeadlineAfterStudentRegistrationEnds() {
        RegistrationPeriod period = validGraduationPeriod();
        period.setReviewDeadline(LocalDateTime.of(2026, 10, 12, 0, 0));

        assertThrows(IllegalArgumentException.class, () -> service.create(period, 12L));
        verify(repository, never()).save(any());
    }

    private RegistrationPeriod validGraduationPeriod() {
        RegistrationPeriod period = new RegistrationPeriod();
        period.setPeriodName("KLTN HK1");
        period.setPeriodType("KLTN");
        period.setLecturerStartAt(LocalDateTime.of(2026, 10, 1, 0, 0));
        period.setLecturerEndAt(LocalDateTime.of(2026, 10, 5, 0, 0));
        period.setStudentStartAt(LocalDateTime.of(2026, 10, 6, 0, 0));
        period.setStudentEndAt(LocalDateTime.of(2026, 10, 12, 0, 0));
        period.setReviewDeadline(LocalDateTime.of(2026, 10, 20, 0, 0));
        period.setCouncilReportDate(LocalDateTime.of(2026, 10, 25, 0, 0));
        return period;
    }
}
