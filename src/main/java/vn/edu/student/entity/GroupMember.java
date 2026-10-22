package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_members")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_member_id")
    private Long groupMemberId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "is_leader", nullable = false)
    private boolean leader;

    public Long getGroupMemberId() { return groupMemberId; }
    public Long getGroupId() { return groupId; }
    public Long getStudentId() { return studentId; }
    public boolean isLeader() { return leader; }
    public void setGroupId(Long value) { groupId = value; }
    public void setStudentId(Long value) { studentId = value; }
    public void setLeader(boolean value) { leader = value; }
}
