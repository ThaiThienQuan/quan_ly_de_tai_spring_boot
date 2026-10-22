package vn.edu.student.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String type = "SYSTEM";

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    public Long getNotificationId() { return notificationId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public Long getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public String getStatus() { return status; }
    public void setTitle(String value) { title = value; }
    public void setContent(String value) { content = value; }
    public void setType(String value) { type = value; }
    public void setCreatedBy(Long value) { createdBy = value; }
    public void setPublishedAt(LocalDateTime value) { publishedAt = value; }
    public void setStatus(String value) { status = value; }
}
