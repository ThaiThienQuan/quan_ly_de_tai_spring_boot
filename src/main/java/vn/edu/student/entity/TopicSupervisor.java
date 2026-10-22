package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "topic_supervisors")
public class TopicSupervisor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_supervisor_id")
    private Long topicSupervisorId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "supervisor_order", nullable = false)
    private int supervisorOrder;

    public Long getTopicSupervisorId() { return topicSupervisorId; }
    public Long getTopicId() { return topicId; }
    public Long getLecturerId() { return lecturerId; }
    public int getSupervisorOrder() { return supervisorOrder; }
    public void setTopicId(Long value) { topicId = value; }
    public void setLecturerId(Long value) { lecturerId = value; }
    public void setSupervisorOrder(int value) { supervisorOrder = value; }
}
