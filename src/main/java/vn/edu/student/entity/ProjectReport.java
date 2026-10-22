package vn.edu.student.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reports")
public class ProjectReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;

    @Column(name = "report_title", nullable = false, length = 200)
    private String reportTitle;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(nullable = false)
    private int version = 1;

    @Column(length = 500)
    private String description;

    public Long getReportId() { return reportId; }
    public Long getGroupId() { return groupId; }
    public Long getSubmittedBy() { return submittedBy; }
    public String getReportTitle() { return reportTitle; }
    public String getFileUrl() { return fileUrl; }
    public int getVersion() { return version; }
    public String getDescription() { return description; }
    public void setGroupId(Long value) { groupId = value; }
    public void setSubmittedBy(Long value) { submittedBy = value; }
    public void setReportTitle(String value) { reportTitle = value; }
    public void setFileUrl(String value) { fileUrl = value; }
    public void setVersion(int value) { version = value; }
    public void setDescription(String value) { description = value; }
}
