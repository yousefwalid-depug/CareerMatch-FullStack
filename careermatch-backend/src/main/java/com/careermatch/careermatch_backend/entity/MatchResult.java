package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static com.careermatch.careermatch_backend.entity.DomainEnums.Confidence;

@Entity
@Table(name = "match_results")
public class MatchResult {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cv_id") private Cv cv;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id") private JobPosting job;
    @Column(name = "overall_match_score", precision = 5, scale = 2, nullable = false) private BigDecimal overallMatchScore;
    @Column(name = "required_skills_score", precision = 5, scale = 2, nullable = false) private BigDecimal requiredSkillsScore;
    @Column(name = "experience_score", precision = 5, scale = 2, nullable = false) private BigDecimal experienceScore;
    @Column(name = "projects_score", precision = 5, scale = 2, nullable = false) private BigDecimal projectsScore;
    @Column(name = "education_score", precision = 5, scale = 2, nullable = false) private BigDecimal educationScore;
    @Column(name = "preferred_skills_score", precision = 5, scale = 2, nullable = false) private BigDecimal preferredSkillsScore;
    @Enumerated(EnumType.STRING) @Column(name = "extraction_confidence", nullable = false) private Confidence extractionConfidence;
    @Column(name = "human_review_flag", nullable = false) private boolean humanReviewFlag;
    @Column(name = "scoring_version", nullable = false) private String scoringVersion;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected MatchResult() {}
    public MatchResult(UUID id, Cv cv, JobPosting job, BigDecimal overall, BigDecimal required, BigDecimal experience,
                       BigDecimal projects, BigDecimal education, BigDecimal preferred, Confidence confidence,
                       boolean humanReviewFlag, String scoringVersion, Instant createdAt) {
        this.id = id; this.cv = cv; this.job = job; this.overallMatchScore = overall;
        this.requiredSkillsScore = required; this.experienceScore = experience; this.projectsScore = projects;
        this.educationScore = education; this.preferredSkillsScore = preferred; this.extractionConfidence = confidence;
        this.humanReviewFlag = humanReviewFlag; this.scoringVersion = scoringVersion; this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public BigDecimal getOverallMatchScore() { return overallMatchScore; }
    public BigDecimal getRequiredSkillsScore() { return requiredSkillsScore; }
    public BigDecimal getExperienceScore() { return experienceScore; }
    public BigDecimal getProjectsScore() { return projectsScore; }
    public BigDecimal getEducationScore() { return educationScore; }
    public BigDecimal getPreferredSkillsScore() { return preferredSkillsScore; }
    public Confidence getExtractionConfidence() { return extractionConfidence; }
    public boolean isHumanReviewFlag() { return humanReviewFlag; }
}
