package vn.edu.student.service;

import vn.edu.student.entity.Topic;
import vn.edu.student.entity.RegistrationPeriod;
import vn.edu.student.entity.TopicSupervisor;
import vn.edu.student.repository.RegistrationPeriodRepository;
import vn.edu.student.repository.LecturerRepository;
import vn.edu.student.repository.TopicSupervisorRepository;
import vn.edu.student.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopicService {
    private final TopicRepository topicRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicSupervisorRepository supervisorRepository;
    private final LecturerRepository lecturerRepository;
    public TopicService(TopicRepository topicRepository, RegistrationPeriodRepository periodRepository,
                        TopicSupervisorRepository supervisorRepository, LecturerRepository lecturerRepository) {
        this.topicRepository = topicRepository;
        this.periodRepository = periodRepository;
        this.supervisorRepository = supervisorRepository;
        this.lecturerRepository = lecturerRepository;
    }
    public List<Topic> findAll() { return topicRepository.findAllByOrderByTopicIdDesc(); }
    public List<Topic> findPublished() { return topicRepository.findByPublishedTrueAndStatus("APPROVED"); }

    @Transactional
    public Topic create(Topic topic, Long coSupervisorId) {
        if (topic.getTopicCode() == null || topic.getTopicCode().isBlank()
                || topic.getTopicName() == null || topic.getTopicName().isBlank()) {
            throw new IllegalArgumentException("Mã đề tài và tên đề tài không được để trống.");
        }
        if (topic.getTopicCode().length() > 30 || topic.getTopicName().length() > 200) {
            throw new IllegalArgumentException("Mã đề tài tối đa 30 ký tự và tên đề tài tối đa 200 ký tự.");
        }
        if (topic.getPeriodId() == null || topic.getDepartmentId() == null
                || topic.getCreatedByLecturerId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn đợt đăng ký và bộ môn.");
        }
        RegistrationPeriod period = periodRepository.findById(topic.getPeriodId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký."));
        LocalDateTime now = LocalDateTime.now();
        if (!"PUBLISHED".equals(period.getStatus())
                || now.isBefore(period.getLecturerStartAt()) || now.isAfter(period.getLecturerEndAt())) {
            throw new IllegalArgumentException("Chỉ được đăng ký đề tài trong thời gian đăng ký của giảng viên.");
        }
        if (topicRepository.existsByPeriodIdAndTopicCodeIgnoreCase(topic.getPeriodId(), topic.getTopicCode().trim())) {
            throw new IllegalArgumentException("Mã đề tài đã tồn tại trong đợt đăng ký này.");
        }
        if (!lecturerRepository.existsById(topic.getCreatedByLecturerId())) {
            throw new IllegalArgumentException("Không tìm thấy hồ sơ giảng viên hướng dẫn.");
        }
        if (coSupervisorId != null && !lecturerRepository.existsById(coSupervisorId)) {
            throw new IllegalArgumentException("Không tìm thấy hồ sơ giảng viên hướng dẫn thứ hai.");
        }
        if (coSupervisorId != null && coSupervisorId.equals(topic.getCreatedByLecturerId())) {
            throw new IllegalArgumentException("Hai giảng viên hướng dẫn phải khác nhau.");
        }
        topic.setTopicCode(topic.getTopicCode().trim());
        topic.setTopicName(topic.getTopicName().trim());
        topic.setStatus("PENDING");
        topic.setPublished(false);
        Topic savedTopic = topicRepository.save(topic);
        saveSupervisor(savedTopic.getTopicId(), topic.getCreatedByLecturerId(), 1);
        if (coSupervisorId != null) saveSupervisor(savedTopic.getTopicId(), coSupervisorId, 2);
        return savedTopic;
    }

    @Transactional
    public void approve(Long topicId) {
        Topic topic = getTopic(topicId);
        if (!"PENDING".equals(topic.getStatus())) {
            throw new IllegalArgumentException("Chỉ đề tài đang chờ duyệt mới được phê duyệt.");
        }
        topic.setStatus("APPROVED");
        topic.setApprovedAt(LocalDateTime.now());
        topicRepository.save(topic);
    }

    @Transactional
    public void publish(Long topicId) {
        Topic topic = getTopic(topicId);
        if (!"APPROVED".equals(topic.getStatus())) {
            throw new IllegalArgumentException("Chỉ đề tài đã được duyệt mới được công bố.");
        }
        long supervisorCount = supervisorRepository.countByTopicId(topicId);
        if (supervisorCount < 1 || supervisorCount > 2) {
            throw new IllegalArgumentException("Đề tài phải có từ một đến hai giảng viên hướng dẫn trước khi công bố.");
        }
        RegistrationPeriod period = periodRepository.findById(topic.getPeriodId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký của đề tài."));
        if (!"PUBLISHED".equals(period.getStatus())) {
            throw new IllegalArgumentException("Đợt đăng ký phải đang được công bố mới có thể công bố đề tài.");
        }
        topic.setPublished(true);
        topicRepository.save(topic);
    }

    private Topic getTopic(Long topicId) {
        return topicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài."));
    }

    private void saveSupervisor(Long topicId, Long lecturerId, int order) {
        TopicSupervisor supervisor = new TopicSupervisor();
        supervisor.setTopicId(topicId);
        supervisor.setLecturerId(lecturerId);
        supervisor.setSupervisorOrder(order);
        supervisorRepository.save(supervisor);
    }
}
