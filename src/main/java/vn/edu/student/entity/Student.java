package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "student_code", nullable = false, unique = true, length = 20)
    private String studentCode;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(nullable = false, length = 100)
    private String major;

    public Long getStudentId() { return studentId; }
    public Long getUserId() { return userId; }
    public String getStudentCode() { return studentCode; }
    public String getClassName() { return className; }
    public String getMajor() { return major; }
    public void setUserId(Long value) { userId = value; }
    public void setStudentCode(String value) { studentCode = value; }
    public void setClassName(String value) { className = value; }
    public void setMajor(String value) { major = value; }
}
