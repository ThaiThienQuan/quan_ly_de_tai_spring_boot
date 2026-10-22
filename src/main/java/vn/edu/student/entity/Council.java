package vn.edu.student.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "councils")
public class Council {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_id")
    private Long councilId;

    @Column(name = "period_id", nullable = false)
    private Long periodId;

    @Column(name = "council_code", nullable = false, unique = true, length = 30)
    private String councilCode;

    @Column(name = "council_name", nullable = false, length = 150)
    private String councilName;

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    @Column(nullable = false, length = 20)
    private String status = "PLANNED";

    public Long getCouncilId() { return councilId; }
    public Long getPeriodId() { return periodId; }
    public String getCouncilCode() { return councilCode; }
    public String getCouncilName() { return councilName; }
    public LocalDateTime getReportDate() { return reportDate; }
    public String getStatus() { return status; }
    public void setPeriodId(Long value) { periodId = value; }
    public void setCouncilCode(String value) { councilCode = value; }
    public void setCouncilName(String value) { councilName = value; }
    public void setReportDate(LocalDateTime value) { reportDate = value; }
    public void setStatus(String value) { status = value; }
}
