package vn.edu.student.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.student.entity.*;
import vn.edu.student.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectGroupServiceTest {
    @Mock private UserAccountRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private RegistrationPeriodRepository periodRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private ProjectGroupRepository groupRepository;
    @Mock private GroupMemberRepository memberRepository;
    @Mock private TopicRegistrationRepository registrationRepository;
    @Mock private ProjectReportRepository reportRepository;

    @InjectMocks
    private ProjectGroupService service;

    private Student student;
    private ProjectGroup group;
    private Topic topic;
    private RegistrationPeriod period;

    @BeforeEach
    void setUp() {
        UserAccount account = mock(UserAccount.class);
        when(account.getUserId()).thenReturn(1L);
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(account));

        student = mock(Student.class);
        lenient().when(student.getStudentId()).thenReturn(10L);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));

        group = mock(ProjectGroup.class);
        when(group.getPeriodId()).thenReturn(30L);

        topic = mock(Topic.class);
        when(topic.getPeriodId()).thenReturn(30L);
        when(topic.getStatus()).thenReturn("APPROVED");
        when(topic.isPublished()).thenReturn(true);
        when(topicRepository.findById(40L)).thenReturn(Optional.of(topic));
    }

    @Test
    void joinsGroupWhenItHasRoomAndRegistrationIsOpen() {
        stubGroupAssociation();
        stubOpenRegistrationWindow();
        when(memberRepository.countByGroupId(20L)).thenReturn(2L);
        when(memberRepository.existsByStudentId(10L)).thenReturn(false);

        service.joinGroup("student", 20L, 40L);

        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertEquals(20L, memberCaptor.getValue().getGroupId());
        assertEquals(10L, memberCaptor.getValue().getStudentId());
        assertFalse(memberCaptor.getValue().isLeader());
    }

    @Test
    void rejectsJoiningAFullGroupWithClearMessage() {
        stubGroupAssociation();
        stubOpenRegistrationWindow();
        when(memberRepository.countByGroupId(20L)).thenReturn(3L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.joinGroup("student", 20L, 40L));

        assertEquals("Nhóm đã đủ 3 thành viên.", exception.getMessage());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void rejectsStudentWhoAlreadyBelongsToAGroup() {
        stubGroupAssociation();
        stubOpenRegistrationWindow();
        when(memberRepository.countByGroupId(20L)).thenReturn(2L);
        when(memberRepository.existsByStudentId(10L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.joinGroup("student", 20L, 40L));

        assertEquals("Bạn đã là thành viên của một nhóm khác.", exception.getMessage());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void rejectsJoinOutsideStudentRegistrationWindow() {
        stubGroupAssociation();
        period = mock(RegistrationPeriod.class);
        when(period.getStatus()).thenReturn("PUBLISHED");
        when(period.getStudentStartAt()).thenReturn(LocalDateTime.now().minusDays(2));
        when(period.getStudentEndAt()).thenReturn(LocalDateTime.now().minusDays(1));
        when(periodRepository.findById(30L)).thenReturn(Optional.of(period));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.joinGroup("student", 20L, 40L));

        assertEquals("Chức năng chỉ mở trong thời gian đăng ký của sinh viên.", exception.getMessage());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void rejectsJoiningAGroupThatDidNotRegisterTheSelectedTopic() {
        when(groupRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(group));
        when(registrationRepository.existsByGroupIdAndTopicIdAndStatusIn(
            20L, 40L, java.util.List.of("PENDING", "APPROVED"))).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.joinGroup("student", 20L, 40L));

        assertEquals("Nhóm này không đăng ký đề tài đã chọn.", exception.getMessage());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void listsMatchingGroupsAndMarksFullGroupsUnavailable() {
        when(group.getGroupId()).thenReturn(20L);
        ProjectGroup fullGroup = mock(ProjectGroup.class);
        when(fullGroup.getGroupId()).thenReturn(21L);
        when(fullGroup.getPeriodId()).thenReturn(30L);
        period = mock(RegistrationPeriod.class);
        when(period.getStatus()).thenReturn("PUBLISHED");
        when(period.getStudentStartAt()).thenReturn(LocalDateTime.now().minusDays(1));
        when(period.getStudentEndAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(periodRepository.findById(30L)).thenReturn(Optional.of(period));
        when(memberRepository.existsByStudentId(10L)).thenReturn(false);

        TopicRegistration firstRegistration = new TopicRegistration();
        firstRegistration.setGroupId(20L);
        TopicRegistration secondRegistration = new TopicRegistration();
        secondRegistration.setGroupId(21L);
        when(registrationRepository.findByTopicIdAndStatusIn(40L, List.of("PENDING", "APPROVED")))
            .thenReturn(List.of(firstRegistration, secondRegistration));
        when(groupRepository.findAllById(List.of(20L, 21L))).thenReturn(List.of(group, fullGroup));
        when(memberRepository.findByGroupId(20L)).thenReturn(List.of(member(10L), member(11L)));
        when(memberRepository.findByGroupId(21L)).thenReturn(List.of(member(12L), member(13L), member(14L)));

        List<GroupJoinOption> options = service.findGroupsForTopic("student", 40L);

        assertEquals(2, options.size());
        GroupJoinOption availableGroup = options.stream()
            .filter(option -> option.group().getGroupId().equals(20L)).findFirst().orElseThrow();
        GroupJoinOption full = options.stream()
            .filter(option -> option.group().getGroupId().equals(21L)).findFirst().orElseThrow();
        assertTrue(availableGroup.canJoin());
        assertEquals(2, availableGroup.memberCount());
        assertFalse(full.canJoin());
        assertEquals("Nhóm đã đủ 3 thành viên.", full.unavailableReason());
    }

    private void stubOpenRegistrationWindow() {
        when(group.getStatus()).thenReturn("DRAFT");
        period = mock(RegistrationPeriod.class);
        when(period.getStatus()).thenReturn("PUBLISHED");
        when(period.getStudentStartAt()).thenReturn(LocalDateTime.now().minusDays(1));
        when(period.getStudentEndAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(periodRepository.findById(30L)).thenReturn(Optional.of(period));
    }

    private void stubGroupAssociation() {
        when(groupRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(group));
        when(registrationRepository.existsByGroupIdAndTopicIdAndStatusIn(
            20L, 40L, List.of("PENDING", "APPROVED"))).thenReturn(true);
    }

    private GroupMember member(Long studentId) {
        GroupMember member = new GroupMember();
        member.setStudentId(studentId);
        return member;
    }
}
