package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false, length = 255)
    private String password;
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    private String phone;
    @Column(nullable = false, length = 20)
    private String role;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(nullable = false)
    private boolean enabled;

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public boolean isEnabled() { return enabled; }
    public String getPhone() { return phone; }
    public void setUsername(String value) { username = value; }
    public void setPassword(String value) { password = value; }
    public void setFullName(String value) { fullName = value; }
    public void setEmail(String value) { email = value; }
    public void setPhone(String value) { phone = value; }
    public void setRole(String value) { role = value; }
    public void setStatus(String value) { status = value; }
    public void setEnabled(boolean value) { enabled = value; }
}
