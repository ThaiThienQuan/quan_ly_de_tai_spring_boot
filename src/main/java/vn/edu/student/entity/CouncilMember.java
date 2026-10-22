package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "council_members")
public class CouncilMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_member_id")
    private Long councilMemberId;

    @Column(name = "council_id", nullable = false)
    private Long councilId;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(nullable = false, length = 20)
    private String role;

    public Long getCouncilMemberId() { return councilMemberId; }
    public Long getCouncilId() { return councilId; }
    public Long getLecturerId() { return lecturerId; }
    public String getRole() { return role; }
    public void setCouncilId(Long value) { councilId = value; }
    public void setLecturerId(Long value) { lecturerId = value; }
    public void setRole(String value) { role = value; }
}
