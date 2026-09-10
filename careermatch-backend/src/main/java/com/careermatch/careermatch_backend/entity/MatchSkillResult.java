package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import static com.careermatch.careermatch_backend.entity.DomainEnums.MatchType;

@Entity
@Table(name = "match_skill_results", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "job_skill_id"}))
public class MatchSkillResult {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "match_id") private MatchResult match;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_skill_id") private JobSkill jobSkill;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "candidate_profile_skill_id") private CandidateProfileSkill candidateProfileSkill;
    @Enumerated(EnumType.STRING) @Column(name = "match_type", nullable = false) private MatchType matchType;
    @Column(name = "match_value", precision = 3, scale = 2, nullable = false) private BigDecimal matchValue;
    @Column(name = "similarity_score", precision = 4, scale = 3) private BigDecimal similarityScore;
    @Column(name = "cv_evidence", columnDefinition = "text") private String cvEvidence;
    @Column(name = "job_evidence", columnDefinition = "text") private String jobEvidence;

    protected MatchSkillResult() {}
    public MatchSkillResult(UUID id, MatchResult match, JobSkill jobSkill, CandidateProfileSkill candidateSkill,
                            MatchType type, BigDecimal value, BigDecimal similarity, String cvEvidence, String jobEvidence) {
        this.id = id; this.match = match; this.jobSkill = jobSkill; this.candidateProfileSkill = candidateSkill;
        this.matchType = type; this.matchValue = value; this.similarityScore = similarity;
        this.cvEvidence = cvEvidence; this.jobEvidence = jobEvidence;
    }
    public UUID getId() { return id; }
    public JobSkill getJobSkill() { return jobSkill; }
    public MatchType getMatchType() { return matchType; }
    public BigDecimal getMatchValue() { return matchValue; }
    public BigDecimal getSimilarityScore() { return similarityScore; }
    public String getCvEvidence() { return cvEvidence; }
    public String getJobEvidence() { return jobEvidence; }
}
