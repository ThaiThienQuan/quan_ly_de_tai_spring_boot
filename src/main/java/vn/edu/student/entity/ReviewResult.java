package vn.edu.student.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_results")
public class ReviewResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "council_id", nullable = false)
    private Long councilId;

    @Column(name = "final_score", nullable = false, precision = 4, scale = 2)
    private BigDecimal finalScore;

    @Column(nullable = false, length = 20)
    private String result;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public Long getResultId() { return resultId; }
    public Long getGroupId() { return groupId; }
    public Long getTopicId() { return topicId; }
    public Long getCouncilId() { return councilId; }
    public BigDecimal getFinalScore() { return finalScore; }
    public String getResult() { return result; }
    public boolean isPublished() { return published; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setGroupId(Long value) { groupId = value; }
    public void setTopicId(Long value) { topicId = value; }
    public void setCouncilId(Long value) { councilId = value; }
    public void setFinalScore(BigDecimal value) { finalScore = value; }
    public void setResult(String value) { result = value; }
    public void setPublished(boolean value) { published = value; }
    public void setPublishedAt(LocalDateTime value) { publishedAt = value; }
}
