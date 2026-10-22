package vn.edu.student.service;

import vn.edu.student.entity.*;
import vn.edu.student.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final LecturerRepository lecturerRepository;
    private final TopicRepository topicRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicSupervisorRepository supervisorRepository;
    private final ReviewAssignmentRepository assignmentRepository;
    private final EvaluationRepository evaluationRepository;
    private final ProjectGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userRepository;
    private final ReviewResultRepository resultRepository;

    public ReviewService(CouncilRepository councilRepository, CouncilMemberRepository councilMemberRepository,
                        LecturerRepository lecturerRepository, TopicRepository topicRepository,
                        RegistrationPeriodRepository periodRepository, TopicSupervisorRepository supervisorRepository,
                        ReviewAssignmentRepository assignmentRepository, EvaluationRepository evaluationRepository,
                        ProjectGroupRepository groupRepository, GroupMemberRepository groupMemberRepository,
                        StudentRepository studentRepository, UserAccountRepository userRepository,
                        ReviewResultRepository resultRepository) {
        this.councilRepository = councilRepository;
        this.councilMemberRepository = councilMemberRepository;
        this.lecturerRepository = lecturerRepository;
        this.topicRepository = topicRepository;
        this.periodRepository = periodRepository;
        this.supervisorRepository = supervisorRepository;
        this.assignmentRepository = assignmentRepository;
        this.evaluationRepository = evaluationRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
    }

    @Transactional(readOnly = true)
    public List<Council> findAllCouncils() {
        return councilRepository.findAllByOrderByReportDateDesc();
    }

    @Transactional(readOnly = true)
    public List<ReviewAssignment> findAssignmentsForLecturer(String username) {
        Lecturer lecturer = lecturerFor(username);
        return assignmentRepository.findByLecturerIdOrderByAssignmentIdDesc(lecturer.getLecturerId());
    }

    @Transactional(readOnly = true)
    public List<ReviewAssignment> findAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Transactional
    public Council createCouncil(Long periodId, String code, String name, LocalDateTime reportDate,
                                 Long chairmanId, Long secretaryId, List<Long> otherLecturerIds) {
        RegistrationPeriod period = periodRepository.findById(periodId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký."));
        if (code == null || code.isBlank() || name == null || name.isBlank() || reportDate == null) {
            throw new IllegalArgumentException("Mã, tên và ngày báo cáo hội đồng là bắt buộc.");
        }
        if (code.length() > 30 || name.length() > 150) {
            throw new IllegalArgumentException("Mã hội đồng tối đa 30 ký tự và tên hội đồng tối đa 150 ký tự.");
        }
        if (councilRepository.existsByCouncilCodeIgnoreCase(code.trim())) {
            throw new IllegalArgumentException("Mã hội đồng đã tồn tại.");
        }
        if (chairmanId == null || secretaryId == null || chairmanId.equals(secretaryId)) {
            throw new IllegalArgumentException("Hội đồng phải có chủ tịch và thư ký là hai giảng viên khác nhau.");
        }

        List<Long> lecturerIds = new ArrayList<>();
        lecturerIds.add(chairmanId);
        lecturerIds.add(secretaryId);
        if (otherLecturerIds != null) lecturerIds.addAll(otherLecturerIds.stream().filter(id -> id != null).toList());
        if (lecturerIds.size() < 3 || lecturerIds.size() > 5
                || lecturerIds.stream().distinct().count() != lecturerIds.size()) {
            throw new IllegalArgumentException("Mỗi hội đồng gồm từ 03 đến 05 giảng viên không trùng lặp.");
        }
        for (Long lecturerId : lecturerIds) {
            if (!lecturerRepository.existsById(lecturerId)) {
                throw new IllegalArgumentException("Không tìm thấy hồ sơ giảng viên mã " + lecturerId + ".");
            }
        }

        Council council = new Council();
        council.setPeriodId(periodId);
        council.setCouncilCode(code.trim());
        council.setCouncilName(name.trim());
        council.setReportDate(reportDate);
        council.setStatus("PLANNED");
        council = councilRepository.save(council);
        for (Long lecturerId : lecturerIds) {
            CouncilMember member = new CouncilMember();
            member.setCouncilId(council.getCouncilId());
            member.setLecturerId(lecturerId);
            member.setRole(lecturerId.equals(chairmanId) ? "CHAIRMAN"
                : lecturerId.equals(secretaryId) ? "SECRETARY" : "MEMBER");
            councilMemberRepository.save(member);
        }
        return council;
    }

    @Transactional
    public ReviewAssignment assignReviewer(Long councilId, Long topicId, Long lecturerId) {
        Council council = getCouncil(councilId);
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài."));
        if (!council.getPeriodId().equals(topic.getPeriodId())) {
            throw new IllegalArgumentException("Đề tài và hội đồng phải thuộc cùng một đợt.");
        }
        if (!topic.isPublished() || !"APPROVED".equals(topic.getStatus())) {
            throw new IllegalArgumentException("Chỉ đề tài đã được duyệt và công bố mới được phân công phản biện.");
        }
        if (!councilMemberRepository.existsByCouncilIdAndLecturerId(councilId, lecturerId)) {
            throw new IllegalArgumentException("Giảng viên phản biện phải là thành viên của hội đồng.");
        }
        if (supervisorRepository.existsByTopicIdAndLecturerId(topicId, lecturerId)) {
            throw new IllegalArgumentException("Không được phân công giảng viên hướng dẫn chấm chính đề tài đó.");
        }
        if (assignmentRepository.existsByCouncilIdAndTopicIdAndLecturerId(councilId, topicId, lecturerId)) {
            throw new IllegalArgumentException("Giảng viên này đã được phân công chấm đề tài trong hội đồng.");
        }
        ReviewAssignment assignment = new ReviewAssignment();
        assignment.setCouncilId(councilId);
        assignment.setTopicId(topicId);
        assignment.setLecturerId(lecturerId);
        assignment.setStatus("ASSIGNED");
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Evaluation submitEvaluation(String username, Long assignmentId, BigDecimal score, String comment) {
        Lecturer lecturer = lecturerFor(username);
        ReviewAssignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phân công phản biện."));
        if (!assignment.getLecturerId().equals(lecturer.getLecturerId())) {
            throw new IllegalArgumentException("Bạn không được phân công chấm đề tài này.");
        }
        if (supervisorRepository.existsByTopicIdAndLecturerId(assignment.getTopicId(), lecturer.getLecturerId())) {
            throw new IllegalArgumentException("Giảng viên hướng dẫn không được chấm đề tài của mình.");
        }
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Điểm phải nằm trong khoảng từ 0 đến 10.");
        }
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Nhận xét không được vượt quá 1000 ký tự.");
        }
        Council council = getCouncil(assignment.getCouncilId());
        RegistrationPeriod period = periodRepository.findById(council.getPeriodId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký."));
        if (period.getReviewDeadline() != null && LocalDateTime.now().isAfter(period.getReviewDeadline())) {
            throw new IllegalArgumentException("Đã quá hạn nộp điểm phản biện của đợt.");
        }

        Evaluation evaluation = evaluationRepository.findByAssignmentId(assignmentId).orElseGet(Evaluation::new);
        evaluation.setAssignmentId(assignmentId);
        evaluation.setScore(score.setScale(2, RoundingMode.HALF_UP));
        evaluation.setComment(comment == null ? null : comment.trim());
        evaluation.setStatus("SUBMITTED");
        assignment.setStatus("COMPLETED");
        assignmentRepository.save(assignment);
        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public ReviewResult publishResult(String username, Long councilId, Long topicId) {
        Council council = getCouncil(councilId);
        Lecturer lecturer = lecturerFor(username);
        if (!councilMemberRepository.existsByCouncilIdAndLecturerIdAndRole(councilId, lecturer.getLecturerId(), "CHAIRMAN")) {
            throw new IllegalArgumentException("Chỉ chủ tịch hội đồng mới được tổng hợp và công bố kết quả.");
        }
        List<ReviewAssignment> assignments = assignmentRepository.findByCouncilIdAndTopicId(councilId, topicId);
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("Chưa có phân công phản biện cho đề tài này.");
        }
        List<Long> ids = assignments.stream().map(ReviewAssignment::getAssignmentId).toList();
        List<Evaluation> evaluations = evaluationRepository.findByAssignmentIdIn(ids);
        if (evaluations.size() != assignments.size()
                || evaluations.stream().anyMatch(evaluation -> !"SUBMITTED".equals(evaluation.getStatus()))) {
            throw new IllegalArgumentException("Chưa thể công bố: tất cả thành viên được phân công phải hoàn tất chấm điểm.");
        }
        ProjectGroup group = groupRepository.findByTopicId(topicId)
            .orElseThrow(() -> new IllegalArgumentException("Chưa có nhóm được duyệt đề tài này."));
        BigDecimal average = evaluations.stream().map(Evaluation::getScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);

        ReviewResult result = resultRepository.findByGroupIdAndTopicIdAndCouncilId(group.getGroupId(), topicId, councilId)
            .orElseGet(ReviewResult::new);
        result.setGroupId(group.getGroupId());
        result.setTopicId(topicId);
        result.setCouncilId(councilId);
        result.setFinalScore(average);
        result.setResult(average.compareTo(BigDecimal.valueOf(5)) >= 0 ? "PASS" : "FAIL");
        result.setPublished(true);
        result.setPublishedAt(LocalDateTime.now());
        result = resultRepository.save(result);

        List<ReviewAssignment> councilAssignments = assignmentRepository.findByCouncilId(councilId);
        List<Long> councilAssignmentIds = councilAssignments.stream().map(ReviewAssignment::getAssignmentId).toList();
        if (!councilAssignmentIds.isEmpty()) {
            List<Evaluation> councilEvaluations = evaluationRepository.findByAssignmentIdIn(councilAssignmentIds);
            if (councilEvaluations.size() == councilAssignmentIds.size()
                    && councilEvaluations.stream().allMatch(item -> "SUBMITTED".equals(item.getStatus()))) {
                council.setStatus("COMPLETED");
                councilRepository.save(council);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ReviewResult> findPublishedResultsForStudent(String username) {
        UserAccount account = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        Student student = studentRepository.findByUserId(account.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản này chưa có hồ sơ sinh viên."));
        List<Long> groupIds = groupMemberRepository.findByStudentId(student.getStudentId()).stream()
            .map(GroupMember::getGroupId).toList();
        return groupIds.isEmpty() ? List.of()
            : resultRepository.findByGroupIdInAndPublishedTrueOrderByResultIdDesc(groupIds);
    }

    @Transactional(readOnly = true)
    public List<Evaluation> findEvaluationsForPublishedResult(String username, Long resultId) {
        ReviewResult result = resultRepository.findById(resultId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kết quả."));
        if (!result.isPublished()) {
            throw new IllegalArgumentException("Kết quả chưa được công bố.");
        }
        UserAccount account = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        Student student = studentRepository.findByUserId(account.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản này chưa có hồ sơ sinh viên."));
        if (!groupMemberRepository.existsByGroupIdAndStudentId(result.getGroupId(), student.getStudentId())) {
            throw new IllegalArgumentException("Bạn không thuộc nhóm của kết quả này.");
        }
        List<Long> assignmentIds = assignmentRepository
            .findByCouncilIdAndTopicId(result.getCouncilId(), result.getTopicId()).stream()
            .map(ReviewAssignment::getAssignmentId).toList();
        return assignmentIds.isEmpty() ? List.of() : evaluationRepository.findByAssignmentIdIn(assignmentIds);
    }

    private Lecturer lecturerFor(String username) {
        UserAccount account = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        return lecturerRepository.findByUserId(account.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản này chưa có hồ sơ giảng viên."));
    }

    private Council getCouncil(Long councilId) {
        return councilRepository.findById(councilId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hội đồng."));
    }
}
