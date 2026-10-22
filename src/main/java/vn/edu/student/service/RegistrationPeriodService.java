package vn.edu.student.service;

import vn.edu.student.entity.RegistrationPeriod;
import vn.edu.student.repository.RegistrationPeriodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationPeriodService {
    private final RegistrationPeriodRepository repository;
    public RegistrationPeriodService(RegistrationPeriodRepository repository) {
        this.repository = repository;
    }
    public List<RegistrationPeriod> findAll() { return repository.findAllByOrderByPeriodIdDesc(); }

    @Transactional
    public RegistrationPeriod create(RegistrationPeriod period, Long creatorUserId) {
        if (period.getPeriodName() == null || period.getPeriodName().isBlank()) {
            throw new IllegalArgumentException("Tên đợt đăng ký không được để trống.");
        }
        if (period.getPeriodName().length() > 150) {
            throw new IllegalArgumentException("Tên đợt đăng ký không được vượt quá 150 ký tự.");
        }
        if (period.getPeriodType() == null
                || !List.of("MON_HOC", "NCKH", "TLCN", "KLTN").contains(period.getPeriodType())) {
            throw new IllegalArgumentException("Loại đợt đăng ký không hợp lệ.");
        }
        period.setPeriodName(period.getPeriodName().trim());
        if (period.getLecturerStartAt() == null || period.getLecturerEndAt() == null
                || period.getStudentStartAt() == null || period.getStudentEndAt() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thời gian đăng ký.");
        }
        if (!period.getLecturerStartAt().isBefore(period.getLecturerEndAt())) {
            throw new IllegalArgumentException("Thời gian đăng ký của giảng viên không hợp lệ.");
        }
        if (!period.getStudentStartAt().isBefore(period.getStudentEndAt())) {
            throw new IllegalArgumentException("Thời gian đăng ký của sinh viên không hợp lệ.");
        }
        if (!period.getStudentStartAt().isAfter(period.getLecturerEndAt())) {
            throw new IllegalArgumentException("Giai đoạn sinh viên phải bắt đầu sau khi giai đoạn đăng ký đề tài của giảng viên kết thúc.");
        }
        if ("TLCN".equals(period.getPeriodType()) || "KLTN".equals(period.getPeriodType())) {
            if (period.getReviewDeadline() == null || !period.getReviewDeadline().isAfter(period.getStudentEndAt())) {
                throw new IllegalArgumentException("Hạn nộp điểm phản biện phải sau khi kết thúc đăng ký sinh viên.");
            }
        } else {
            period.setReviewDeadline(null);
        }
        if ("KLTN".equals(period.getPeriodType())) {
            if (period.getCouncilReportDate() == null || !period.getCouncilReportDate().isAfter(period.getStudentEndAt())) {
                throw new IllegalArgumentException("Ngày báo cáo hội đồng phải sau khi kết thúc đăng ký sinh viên.");
            }
        } else {
            period.setCouncilReportDate(null);
        }
        period.setCreatedBy(creatorUserId);
        period.setStatus("DRAFT");
        return repository.save(period);
    }

    @Transactional
    public void publish(Long periodId) {
        RegistrationPeriod period = repository.findById(periodId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký."));
        if (!"DRAFT".equals(period.getStatus())) {
            throw new IllegalArgumentException("Chỉ đợt đăng ký ở trạng thái bản nháp mới được công bố.");
        }
        period.setStatus("PUBLISHED");
        repository.save(period);
    }
}
