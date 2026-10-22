package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "review_assignments")
public class ReviewAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @Column(name = "council_id", nullable = false)
    private Long councilId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(nullable = false, length = 20)
    private String status = "ASSIGNED";

    public Long getAssignmentId() { return assignmentId; }
    public Long getCouncilId() { return councilId; }
    public Long getTopicId() { return topicId; }
    public Long getLecturerId() { return lecturerId; }
    public String getStatus() { return status; }
    public void setCouncilId(Long value) { councilId = value; }
    public void setTopicId(Long value) { topicId = value; }
    public void setLecturerId(Long value) { lecturerId = value; }
    public void setStatus(String value) { status = value; }
}
