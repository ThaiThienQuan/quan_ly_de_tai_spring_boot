package vn.edu.student.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_periods")
public class RegistrationPeriod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "period_id")
    private Long periodId;
    @Column(name = "period_name", nullable = false, length = 150)
    private String periodName;
    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;
    @Column(name = "lecturer_start_at", nullable = false)
    private LocalDateTime lecturerStartAt;
    @Column(name = "lecturer_end_at", nullable = false)
    private LocalDateTime lecturerEndAt;
    @Column(name = "student_start_at", nullable = false)
    private LocalDateTime studentStartAt;
    @Column(name = "student_end_at", nullable = false)
    private LocalDateTime studentEndAt;
    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;
    @Column(name = "council_report_date")
    private LocalDateTime councilReportDate;
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public Long getPeriodId() { return periodId; }
    public String getPeriodName() { return periodName; }
    public String getPeriodType() { return periodType; }
    public LocalDateTime getLecturerStartAt() { return lecturerStartAt; }
    public LocalDateTime getLecturerEndAt() { return lecturerEndAt; }
    public LocalDateTime getStudentStartAt() { return studentStartAt; }
    public LocalDateTime getStudentEndAt() { return studentEndAt; }
    public LocalDateTime getReviewDeadline() { return reviewDeadline; }
    public LocalDateTime getCouncilReportDate() { return councilReportDate; }
    public String getStatus() { return status; }
    public Long getCreatedBy() { return createdBy; }

    public void setPeriodName(String value) { periodName = value; }
    public void setPeriodType(String value) { periodType = value; }
    public void setLecturerStartAt(LocalDateTime value) { lecturerStartAt = value; }
    public void setLecturerEndAt(LocalDateTime value) { lecturerEndAt = value; }
    public void setStudentStartAt(LocalDateTime value) { studentStartAt = value; }
    public void setStudentEndAt(LocalDateTime value) { studentEndAt = value; }
    public void setReviewDeadline(LocalDateTime value) { reviewDeadline = value; }
    public void setCouncilReportDate(LocalDateTime value) { councilReportDate = value; }
    public void setStatus(String value) { status = value; }
    public void setCreatedBy(Long value) { createdBy = value; }
}
