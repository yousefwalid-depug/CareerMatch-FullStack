package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static com.careermatch.careermatch_backend.entity.DomainEnums.*;

@Entity
@Table(name = "improvement_recommendations")
public class ImprovementRecommendation {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "match_id") private MatchResult match;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "match_skill_result_id") private MatchSkillResult matchSkillResult;
    @Column(name = "gap_name", nullable = false) private String gapName;
    @Enumerated(EnumType.STRING) @Column(name = "gap_category", nullable = false) private GapCategory gapCategory;
    @Enumerated(EnumType.STRING) @Column(name = "required_or_preferred", nullable = false) private RequirementType requiredOrPreferred;
    @Column(name = "importance_weight", nullable = false) private int importanceWeight;
    @Column(name = "job_evidence", columnDefinition = "text") private String jobEvidence;
    @Column(name = "cv_evidence", columnDefinition = "text") private String cvEvidence;
    @Column(name = "related_existing_strengths", columnDefinition = "text") private String relatedExistingStrengths;
    @Column(name = "recommended_action", nullable = false, columnDefinition = "text") private String recommendedAction;
    @Column(columnDefinition = "text") private String deliverable;
    @Column(name = "estimated_effort_min_days", precision = 6, scale = 2) private BigDecimal estimatedEffortMinDays;
    @Column(name = "estimated_effort_max_days", precision = 6, scale = 2) private BigDecimal estimatedEffortMaxDays;
    @Column(name = "expected_score_gain", precision = 5, scale = 2, nullable = false) private BigDecimal expectedScoreGain;
    @Column(name = "priority_score", precision = 8, scale = 3, nullable = false) private BigDecimal priorityScore;
    @Enumerated(EnumType.STRING) @Column(name = "priority_label", nullable = false) private PriorityLabel priorityLabel;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Confidence confidence;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RecommendationStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    protected ImprovementRecommendation() {}
    public ImprovementRecommendation(UUID id, MatchResult match, MatchSkillResult skillResult, String gapName,
                                     GapCategory gapCategory, RequirementType type, int weight, String jobEvidence,
                                     String cvEvidence, List<String> relatedExistingStrengths, String action, String deliverable,
                                     BigDecimal minDays, BigDecimal maxDays, BigDecimal gain, BigDecimal priority,
                                     PriorityLabel label, Confidence confidence, Instant now) {
        this.id = id; this.match = match; this.matchSkillResult = skillResult; this.gapName = gapName;
        this.gapCategory = gapCategory; this.requiredOrPreferred = type; this.importanceWeight = weight;
        this.jobEvidence = jobEvidence; this.cvEvidence = cvEvidence;
        this.relatedExistingStrengths = String.join("\n", relatedExistingStrengths);
        this.recommendedAction = action; this.deliverable = deliverable;
        this.estimatedEffortMinDays = minDays; this.estimatedEffortMaxDays = maxDays;
        this.expectedScoreGain = gain; this.priorityScore = priority; this.priorityLabel = label;
        this.confidence = confidence; this.status = RecommendationStatus.NOT_STARTED; this.createdAt = now; this.updatedAt = now;
    }
    public String getGapName() { return gapName; }
    public GapCategory getGapCategory() { return gapCategory; }
    public RequirementType getRequiredOrPreferred() { return requiredOrPreferred; }
    public int getImportanceWeight() { return importanceWeight; }
    public String getJobEvidence() { return jobEvidence; }
    public String getCvEvidence() { return cvEvidence; }
    public List<String> getRelatedExistingStrengths() {
        if (relatedExistingStrengths == null || relatedExistingStrengths.isBlank()) return List.of();
        return Arrays.stream(relatedExistingStrengths.split("\\R")).filter(value -> !value.isBlank()).toList();
    }
    public String getRecommendedAction() { return recommendedAction; }
    public String getDeliverable() { return deliverable; }
    public BigDecimal getEstimatedEffortMinDays() { return estimatedEffortMinDays; }
    public BigDecimal getEstimatedEffortMaxDays() { return estimatedEffortMaxDays; }
    public BigDecimal getExpectedScoreGain() { return expectedScoreGain; }
    public BigDecimal getPriorityScore() { return priorityScore; }
    public PriorityLabel getPriorityLabel() { return priorityLabel; }
    public Confidence getConfidence() { return confidence; }
    public RecommendationStatus getStatus() { return status; }
}
