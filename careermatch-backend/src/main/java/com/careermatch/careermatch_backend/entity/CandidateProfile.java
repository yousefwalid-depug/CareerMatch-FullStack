package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import static com.careermatch.careermatch_backend.entity.DomainEnums.Confidence;

@Entity
@Table(name = "candidate_profiles")
public class CandidateProfile {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cv_id", unique = true) private Cv cv;
    @Column(name = "total_experience_months") private Integer totalExperienceMonths;
    @Enumerated(EnumType.STRING) @Column(name = "extraction_confidence", nullable = false) private Confidence extractionConfidence;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    protected CandidateProfile() {}
    public CandidateProfile(UUID id, Cv cv, Integer months, Confidence confidence, Instant now) {
        this.id = id; this.cv = cv; this.totalExperienceMonths = months; this.extractionConfidence = confidence;
        this.createdAt = now; this.updatedAt = now;
    }
    public UUID getId() { return id; }
    public UUID getCvId() { return cv.getId(); }
    public Integer getTotalExperienceMonths() { return totalExperienceMonths; }
    public Confidence getExtractionConfidence() { return extractionConfidence; }
}
