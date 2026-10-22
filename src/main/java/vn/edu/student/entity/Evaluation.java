package vn.edu.student.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "evaluations")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Long evaluationId;

    @Column(name = "assignment_id", nullable = false, unique = true)
    private Long assignmentId;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal score;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    public Long getEvaluationId() { return evaluationId; }
    public Long getAssignmentId() { return assignmentId; }
    public BigDecimal getScore() { return score; }
    public String getComment() { return comment; }
    public String getStatus() { return status; }
    public void setAssignmentId(Long value) { assignmentId = value; }
    public void setScore(BigDecimal value) { score = value; }
    public void setComment(String value) { comment = value; }
    public void setStatus(String value) { status = value; }
}
