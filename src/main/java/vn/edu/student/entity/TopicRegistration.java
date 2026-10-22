package vn.edu.student.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "topic_registrations")
public class TopicRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public Long getRegistrationId() { return registrationId; }
    public Long getGroupId() { return groupId; }
    public Long getTopicId() { return topicId; }
    public String getStatus() { return status; }
    public void setGroupId(Long value) { groupId = value; }
    public void setTopicId(Long value) { topicId = value; }
    public void setStatus(String value) { status = value; }
    public void setApprovedBy(Long value) { approvedBy = value; }
    public void setApprovedAt(LocalDateTime value) { approvedAt = value; }
    public void setRejectionReason(String value) { rejectionReason = value; }
}
