package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import static com.careermatch.careermatch_backend.entity.DomainEnums.*;

@Entity
@Table(name = "job_postings")
public class JobPosting {
    @Id private UUID id;
    @Column(name = "external_job_id") private String externalJobId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private JobSource source;
    @Column(nullable = false) private String title;
    private String company;
    private String location;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Column(name = "expected_experience_months") private Integer expectedExperienceMonths;
    @Column(name = "education_preference") private String educationPreference;
    @Enumerated(EnumType.STRING) @Column(name = "requirements_extraction_confidence") private Confidence requirementsExtractionConfidence;
    @Enumerated(EnumType.STRING) @Column(name = "requirements_status", nullable = false) private RequirementsStatus requirementsStatus;
    @Column(name = "requirements_extracted_at") private Instant requirementsExtractedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    protected JobPosting() {}
    public UUID getId() { return id; }
    public JobSource getSource() { return source; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public Integer getExpectedExperienceMonths() { return expectedExperienceMonths; }
    public String getEducationPreference() { return educationPreference; }
    public Confidence getRequirementsExtractionConfidence() { return requirementsExtractionConfidence; }
}
