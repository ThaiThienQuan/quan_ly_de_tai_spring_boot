package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lecturers")
public class Lecturer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecturer_id")
    private Long lecturerId;
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    @Column(name = "lecturer_code", nullable = false, unique = true)
    private String lecturerCode;
    @Column(name = "academic_degree")
    private String academicDegree;
    @Column(name = "department_id", nullable = false)
    private Long departmentId;
    public Long getLecturerId() { return lecturerId; }
    public Long getUserId() { return userId; }
    public String getLecturerCode() { return lecturerCode; }
    public Long getDepartmentId() { return departmentId; }
}
