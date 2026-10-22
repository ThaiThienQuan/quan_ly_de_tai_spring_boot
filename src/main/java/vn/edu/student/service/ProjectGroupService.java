package vn.edu.student.service;

import vn.edu.student.entity.*;
import vn.edu.student.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProjectGroupService {
    private static final int MAX_GROUP_SIZE = 3;
    private static final List<String> ACTIVE_TOPIC_REGISTRATION_STATUSES = List.of("PENDING", "APPROVED");

    private final UserAccountRepository userRepository;
    private final StudentRepository studentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicRepository topicRepository;
    private final ProjectGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final TopicRegistrationRepository registrationRepository;
    private final ProjectReportRepository reportRepository;

    public ProjectGroupService(UserAccountRepository userRepository, StudentRepository studentRepository,
                               RegistrationPeriodRepository periodRepository, TopicRepository topicRepository,
                               ProjectGroupRepository groupRepository, GroupMemberRepository memberRepository,
                               TopicRegistrationRepository registrationRepository,
                               ProjectReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.periodRepository = periodRepository;
        this.topicRepository = topicRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.registrationRepository = registrationRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectGroup> findGroupsForUser(String username) {
        Student student = studentFor(username);
        List<Long> groupIds = memberRepository.findByStudentId(student.getStudentId()).stream()
            .map(GroupMember::getGroupId).toList();
        return groupIds.isEmpty() ? List.of() : groupRepository.findByGroupIdInOrderByGroupIdDesc(groupIds);
    }

    @Transactional(readOnly = true)
    public List<GroupJoinOption> findGroupsForTopic(String username, Long topicId) {
        Student student = studentFor(username);
        Topic topic = getPublishedTopic(topicId);
        boolean registrationOpen = isStudentRegistrationOpen(getPeriod(topic.getPeriodId()));
        boolean alreadyInGroup = memberRepository.existsByStudentId(student.getStudentId());
        List<Long> groupIds = registrationRepository
            .findByTopicIdAndStatusIn(topicId, ACTIVE_TOPIC_REGISTRATION_STATUSES).stream()
            .map(TopicRegistration::getGroupId).distinct().toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }

        return groupRepository.findAllById(groupIds).stream()
            .filter(group -> group.getPeriodId().equals(topic.getPeriodId()))
            .sorted(Comparator.comparing(ProjectGroup::getGroupId).reversed())
            .map(group -> {
                List<GroupMember> members = memberRepository.findByGroupId(group.getGroupId());
                int memberCount = members.size();
                String unavailableReason = memberCount >= MAX_GROUP_SIZE
                    ? "Nhóm đã đủ 3 thành viên."
                    : alreadyInGroup ? "Bạn đã là thành viên của một nhóm khác."
                    : !registrationOpen ? "Đợt đăng ký sinh viên hiện không mở."
                    : null;
                return new GroupJoinOption(group, memberCount, members,
                    unavailableReason == null, unavailableReason);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectGroup> findAllGroups() {
        return groupRepository.findAllByOrderByGroupIdDesc();
    }

    @Transactional(readOnly = true)
    public List<TopicRegistration> findPendingRegistrations() {
        return registrationRepository.findByStatusOrderByRegistrationIdDesc("PENDING");
    }

    @Transactional(readOnly = true)
    public List<ProjectReport> findReportsForGroup(Long groupId, String username) {
        ProjectGroup group = getGroup(groupId);
        Student student = studentFor(username);
        if (!memberRepository.existsByGroupIdAndStudentId(groupId, student.getStudentId())) {
            throw new IllegalArgumentException("Bạn không phải thành viên của nhóm này.");
        }
        return reportRepository.findByGroupIdOrderByReportIdDesc(group.getGroupId());
    }

    @Transactional(readOnly = true)
    public List<GroupMember> findMembers(Long groupId) {
        return memberRepository.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public boolean isLeaderOf(String username, Long groupId) {
        Student student = studentFor(username);
        return memberRepository.existsByGroupIdAndStudentIdAndLeaderTrue(groupId, student.getStudentId());
    }

    @Transactional
    public ProjectGroup createGroup(String username, Long periodId, String groupCode, String groupName,
                                    List<String> memberCodes) {
        Student leader = studentFor(username);
        RegistrationPeriod period = getPeriod(periodId);
        requireStudentRegistrationOpen(period);
        if (groupCode == null || groupCode.isBlank() || groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Mã nhóm và tên nhóm không được để trống.");
        }
        if (groupCode.length() > 30 || groupName.length() > 100) {
            throw new IllegalArgumentException("Mã nhóm tối đa 30 ký tự và tên nhóm tối đa 100 ký tự.");
        }
        if (groupRepository.existsByGroupCodeIgnoreCase(groupCode.trim())) {
            throw new IllegalArgumentException("Mã nhóm đã tồn tại.");
        }

        List<Student> members = new ArrayList<>();
        members.add(leader);
        if (memberCodes != null) {
            for (String code : memberCodes) {
                if (code == null || code.isBlank()) continue;
                Student member = studentRepository.findByStudentCodeIgnoreCase(code.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên có mã " + code.trim() + "."));
                if (members.stream().anyMatch(existing -> existing.getStudentId().equals(member.getStudentId()))) {
                    throw new IllegalArgumentException("Không được thêm trùng sinh viên hoặc thêm chính nhóm trưởng lần nữa.");
                }
                members.add(member);
            }
        }
        if (members.size() > 3) {
            throw new IllegalArgumentException("Mỗi nhóm tối đa 03 thành viên tính cả nhóm trưởng.");
        }
        for (Student member : members) {
            if (memberRepository.existsByStudentId(member.getStudentId())) {
                throw new IllegalArgumentException("Sinh viên " + member.getStudentCode() + " đã thuộc một nhóm.");
            }
        }

        ProjectGroup group = new ProjectGroup();
        group.setPeriodId(periodId);
        group.setGroupCode(groupCode.trim());
        group.setGroupName(groupName.trim());
        group.setStatus("DRAFT");
        group = groupRepository.save(group);
        for (Student member : members) {
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(group.getGroupId());
            groupMember.setStudentId(member.getStudentId());
            groupMember.setLeader(member.getStudentId().equals(leader.getStudentId()));
            memberRepository.save(groupMember);
        }
        return group;
    }

    @Transactional
    public TopicRegistration registerTopic(String username, Long groupId, Long topicId) {
        ProjectGroup group = getGroup(groupId);
        Student leader = studentFor(username);
        requireLeader(groupId, leader.getStudentId());
        RegistrationPeriod period = getPeriod(group.getPeriodId());
        requireStudentRegistrationOpen(period);
        if (!"DRAFT".equals(group.getStatus()) || group.getTopicId() != null) {
            throw new IllegalArgumentException("Nhóm đã được duyệt hoặc đã đăng ký một đề tài.");
        }
        if (registrationRepository.existsByGroupIdAndStatusIn(groupId, List.of("PENDING", "APPROVED"))) {
            throw new IllegalArgumentException("Nhóm đang có đăng ký đề tài chờ xử lý hoặc đã được duyệt.");
        }
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài."));
        if (!topic.isPublished() || !"APPROVED".equals(topic.getStatus()) || !topic.getPeriodId().equals(group.getPeriodId())) {
            throw new IllegalArgumentException("Chỉ được chọn đề tài đã công bố thuộc đúng đợt đăng ký của nhóm.");
        }
        if (groupRepository.existsByTopicId(topicId)) {
            throw new IllegalArgumentException("Đề tài này đã được một nhóm khác đăng ký thành công.");
        }
        TopicRegistration registration = new TopicRegistration();
        registration.setGroupId(groupId);
        registration.setTopicId(topicId);
        registration.setStatus("PENDING");
        return registrationRepository.save(registration);
    }

    @Transactional
    public void joinGroup(String username, Long groupId, Long topicId) {
        Student student = studentFor(username);
        ProjectGroup group = groupRepository.findByIdForUpdate(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm."));
        Topic topic = getPublishedTopic(topicId);
        if (!group.getPeriodId().equals(topic.getPeriodId())
                || !registrationRepository.existsByGroupIdAndTopicIdAndStatusIn(
                    groupId, topicId, ACTIVE_TOPIC_REGISTRATION_STATUSES)) {
            throw new IllegalArgumentException("Nhóm này không đăng ký đề tài đã chọn.");
        }
        requireStudentRegistrationOpen(getPeriod(group.getPeriodId()));
        if (!"DRAFT".equals(group.getStatus()) && !"APPROVED".equals(group.getStatus())) {
            throw new IllegalArgumentException("Nhóm hiện không thể nhận thêm thành viên.");
        }
        if (memberRepository.countByGroupId(groupId) >= MAX_GROUP_SIZE) {
            throw new IllegalArgumentException("Nhóm đã đủ 3 thành viên.");
        }
        if (memberRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("Bạn đã là thành viên của một nhóm khác.");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setStudentId(student.getStudentId());
        member.setLeader(false);
        memberRepository.save(member);
    }

    @Transactional
    public void reviewRegistration(String reviewerUsername, Long registrationId, boolean approve, String reason) {
        TopicRegistration registration = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đăng ký đề tài."));
        if (!"PENDING".equals(registration.getStatus())) {
            throw new IllegalArgumentException("Đăng ký này đã được xử lý.");
        }
        UserAccount reviewer = userRepository.findByUsername(reviewerUsername)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người duyệt."));
        if (!approve && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        }
        if (approve) {
            ProjectGroup group = getGroup(registration.getGroupId());
            if (groupRepository.existsByTopicId(registration.getTopicId())) {
                throw new IllegalArgumentException("Đề tài đã được nhóm khác đăng ký trước đó.");
            }
            registration.setStatus("APPROVED");
            group.setTopicId(registration.getTopicId());
            group.setStatus("APPROVED");
            groupRepository.save(group);
        } else {
            registration.setStatus("REJECTED");
            registration.setRejectionReason(reason.trim());
        }
        registration.setApprovedBy(reviewer.getUserId());
        registration.setApprovedAt(LocalDateTime.now());
        registrationRepository.save(registration);
    }

    @Transactional
    public ProjectReport submitReport(String username, Long groupId, String title, String fileUrl, String description) {
        ProjectGroup group = getGroup(groupId);
        Student student = studentFor(username);
        requireLeader(groupId, student.getStudentId());
        if (!"APPROVED".equals(group.getStatus()) || group.getTopicId() == null) {
            throw new IllegalArgumentException("Chỉ nhóm đã được duyệt đề tài mới được nộp báo cáo.");
        }
        if (title == null || title.isBlank() || fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("Tên báo cáo và đường dẫn tệp không được để trống.");
        }
        if (title.length() > 200 || fileUrl.length() > 500 || (description != null && description.length() > 500)) {
            throw new IllegalArgumentException("Thông tin báo cáo vượt quá độ dài cho phép.");
        }
        validateReportLocation(fileUrl.trim());
        ProjectReport report = new ProjectReport();
        report.setGroupId(groupId);
        report.setSubmittedBy(student.getStudentId());
        report.setReportTitle(title.trim());
        report.setFileUrl(fileUrl.trim());
        report.setDescription(description == null ? null : description.trim());
        report.setVersion(reportRepository.countByGroupId(groupId) + 1);
        return reportRepository.save(report);
    }

    private void requireStudentRegistrationOpen(RegistrationPeriod period) {
        if (!isStudentRegistrationOpen(period)) {
            throw new IllegalArgumentException("Chức năng chỉ mở trong thời gian đăng ký của sinh viên.");
        }
    }

    private boolean isStudentRegistrationOpen(RegistrationPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        return "PUBLISHED".equals(period.getStatus()) && period.getStudentStartAt() != null
            && period.getStudentEndAt() != null && !now.isBefore(period.getStudentStartAt())
            && !now.isAfter(period.getStudentEndAt());
    }

    private Topic getPublishedTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài."));
        if (!topic.isPublished() || !"APPROVED".equals(topic.getStatus())) {
            throw new IllegalArgumentException("Chỉ được tham gia nhóm đăng ký đề tài đã công bố.");
        }
        return topic;
    }

    private Student studentFor(String username) {
        UserAccount account = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        return studentRepository.findByUserId(account.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản này chưa có hồ sơ sinh viên."));
    }

    private void requireLeader(Long groupId, Long studentId) {
        if (!memberRepository.existsByGroupIdAndStudentIdAndLeaderTrue(groupId, studentId)) {
            throw new IllegalArgumentException("Chỉ nhóm trưởng mới được thực hiện thao tác này.");
        }
    }

    private ProjectGroup getGroup(Long groupId) {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm."));
    }

    private RegistrationPeriod getPeriod(Long periodId) {
        return periodRepository.findById(periodId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký."));
    }

    private void validateReportLocation(String fileUrl) {
        if (fileUrl.startsWith("/") && !fileUrl.startsWith("//")) return;
        try {
            URI uri = new URI(fileUrl);
            if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException("Đường dẫn báo cáo phải là đường dẫn nội bộ hoặc URL HTTP/HTTPS.");
            }
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Đường dẫn báo cáo không hợp lệ.");
        }
    }
}
