package vn.edu.student.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.student.entity.*;
import vn.edu.student.repository.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock private CouncilRepository councilRepository;
    @Mock private CouncilMemberRepository councilMemberRepository;
    @Mock private LecturerRepository lecturerRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private RegistrationPeriodRepository periodRepository;
    @Mock private TopicSupervisorRepository supervisorRepository;
    @Mock private ReviewAssignmentRepository assignmentRepository;
    @Mock private EvaluationRepository evaluationRepository;
    @Mock private ProjectGroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserAccountRepository userRepository;
    @Mock private ReviewResultRepository resultRepository;

    @InjectMocks
    private ReviewService service;

    @Test
    void findsCouncilsAndMemberRolesForTheLoggedInLecturer() {
        UserAccount ownAccount = mock(UserAccount.class);
        when(ownAccount.getUserId()).thenReturn(100L);
        when(userRepository.findByUsername("lecturer")).thenReturn(Optional.of(ownAccount));

        Lecturer ownLecturer = lecturer(200L, 100L, "GV001");
        Lecturer chair = lecturer(201L, 101L, "GV002");
        Lecturer secretary = lecturer(202L, 102L, "GV003");
        when(lecturerRepository.findByUserId(100L)).thenReturn(Optional.of(ownLecturer));

        CouncilMember ownMembership = member(10L, 200L, "MEMBER");
        when(councilMemberRepository.findByLecturerId(200L)).thenReturn(List.of(ownMembership));
        Council council = mock(Council.class);
        when(council.getCouncilId()).thenReturn(10L);
        when(council.getPeriodId()).thenReturn(5L);
        when(council.getCouncilCode()).thenReturn("HD01");
        when(councilRepository.findAllById(List.of(10L))).thenReturn(List.of(council));

        when(councilMemberRepository.findByCouncilIdIn(List.of(10L))).thenReturn(List.of(
            member(10L, 201L, "CHAIRMAN"),
            member(10L, 202L, "SECRETARY"),
            ownMembership
        ));
        when(lecturerRepository.findAllById(List.of(201L, 202L, 200L)))
            .thenReturn(List.of(chair, secretary, ownLecturer));

        UserAccount chairAccount = mock(UserAccount.class);
        when(chairAccount.getUserId()).thenReturn(101L);
        when(chairAccount.getFullName()).thenReturn("Nguyen Van A");
        UserAccount secretaryAccount = mock(UserAccount.class);
        when(secretaryAccount.getUserId()).thenReturn(102L);
        when(secretaryAccount.getFullName()).thenReturn("Tran Thi B");
        when(ownAccount.getFullName()).thenReturn("Le Van C");
        when(userRepository.findAllById(any()))
            .thenReturn(List.of(chairAccount, secretaryAccount, ownAccount));

        RegistrationPeriod period = mock(RegistrationPeriod.class);
        when(period.getPeriodId()).thenReturn(5L);
        when(period.getPeriodName()).thenReturn("KLTN HK1");
        when(periodRepository.findAllById(List.of(5L))).thenReturn(List.of(period));

        List<LecturerCouncilView> result = service.findCouncilsForLecturer("lecturer");

        assertEquals(1, result.size());
        LecturerCouncilView councilView = result.getFirst();
        assertEquals("KLTN HK1", councilView.periodName());
        assertEquals("HD01", councilView.council().getCouncilCode());
        assertEquals(3, councilView.members().size());
        assertTrue(councilView.members().stream()
            .anyMatch(member -> member.fullName().equals("Nguyen Van A")
                && member.role().equals("CHAIRMAN")));
        assertTrue(councilView.members().stream()
            .anyMatch(member -> member.fullName().equals("Tran Thi B")
                && member.role().equals("SECRETARY")));
        assertTrue(councilView.members().stream()
            .anyMatch(member -> member.fullName().equals("Le Van C")
                && member.role().equals("MEMBER")));
    }

    @Test
    void returnsEmptyListWhenLecturerHasNoCouncilAssignments() {
        UserAccount account = mock(UserAccount.class);
        when(account.getUserId()).thenReturn(100L);
        when(userRepository.findByUsername("lecturer")).thenReturn(Optional.of(account));
        Lecturer lecturer = mock(Lecturer.class);
        when(lecturer.getLecturerId()).thenReturn(200L);
        when(lecturerRepository.findByUserId(100L)).thenReturn(Optional.of(lecturer));
        when(councilMemberRepository.findByLecturerId(200L)).thenReturn(List.of());

        assertTrue(service.findCouncilsForLecturer("lecturer").isEmpty());
        verify(councilRepository, never()).findAllById(any());
    }

    private Lecturer lecturer(Long id, Long userId, String code) {
        Lecturer lecturer = mock(Lecturer.class);
        when(lecturer.getLecturerId()).thenReturn(id);
        when(lecturer.getUserId()).thenReturn(userId);
        when(lecturer.getLecturerCode()).thenReturn(code);
        return lecturer;
    }

    private CouncilMember member(Long councilId, Long lecturerId, String role) {
        CouncilMember member = new CouncilMember();
        member.setCouncilId(councilId);
        member.setLecturerId(lecturerId);
        member.setRole(role);
        return member;
    }
}
