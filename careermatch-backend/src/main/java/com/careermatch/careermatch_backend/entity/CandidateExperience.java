package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "candidate_experiences")
public class CandidateExperience {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "profile_id") private CandidateProfile profile;
    @Column(name = "job_title") private String jobTitle;
    private String company;
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Column(name = "duration_months") private Integer durationMonths;
    @Column(columnDefinition = "text") private String description;
    protected CandidateExperience() {}
    public CandidateExperience(UUID id, CandidateProfile profile, String jobTitle, String company,
                               LocalDate startDate, LocalDate endDate, Integer durationMonths, String description) {
        this.id = id; this.profile = profile; this.jobTitle = jobTitle; this.company = company;
        this.startDate = startDate; this.endDate = endDate; this.durationMonths = durationMonths;
        this.description = description;
    }
    public UUID getId() { return id; }
    public String getJobTitle() { return jobTitle; }
    public String getCompany() { return company; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Integer getDurationMonths() { return durationMonths; }
    public String getDescription() { return description; }
}
