package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_groups")
public class ProjectGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "period_id", nullable = false)
    private Long periodId;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "group_code", nullable = false, length = 30)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    public Long getGroupId() { return groupId; }
    public Long getPeriodId() { return periodId; }
    public Long getTopicId() { return topicId; }
    public String getGroupCode() { return groupCode; }
    public String getGroupName() { return groupName; }
    public String getStatus() { return status; }
    public void setPeriodId(Long value) { periodId = value; }
    public void setTopicId(Long value) { topicId = value; }
    public void setGroupCode(String value) { groupCode = value; }
    public void setGroupName(String value) { groupName = value; }
    public void setStatus(String value) { status = value; }
}
