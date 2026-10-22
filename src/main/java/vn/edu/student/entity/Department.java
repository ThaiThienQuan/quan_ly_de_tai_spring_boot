package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;
    @Column(name = "department_code", nullable = false, unique = true)
    private String departmentCode;
    @Column(name = "department_name", nullable = false)
    private String departmentName;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private String status;
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
}
