package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class SystemRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public Long getRoleId() { return roleId; }
    public String getName() { return name; }
}
