package vn.edu.student.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "topics")
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long topicId;
    @Column(name = "period_id", nullable = false)
    private Long periodId;
    @Column(name = "department_id", nullable = false)
    private Long departmentId;
    @Column(name = "created_by_lecturer_id", nullable = false)
    private Long createdByLecturerId;
    @Column(name = "topic_code", nullable = false, length = 30)
    private String topicCode;
    @Column(name = "topic_name", nullable = false, length = 200)
    private String topicName;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    @Column(nullable = false)
    private boolean published = false;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public Long getTopicId() { return topicId; }
    public Long getPeriodId() { return periodId; }
    public Long getDepartmentId() { return departmentId; }
    public Long getCreatedByLecturerId() { return createdByLecturerId; }
    public String getTopicCode() { return topicCode; }
    public String getTopicName() { return topicName; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public boolean isPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setPeriodId(Long value) { periodId = value; }
    public void setDepartmentId(Long value) { departmentId = value; }
    public void setCreatedByLecturerId(Long value) { createdByLecturerId = value; }
    public void setTopicCode(String value) { topicCode = value; }
    public void setTopicName(String value) { topicName = value; }
    public void setDescription(String value) { description = value; }
    public void setStatus(String value) { status = value; }
    public void setPublished(boolean value) { published = value; }
    public void setApprovedAt(LocalDateTime value) { approvedAt = value; }
}
