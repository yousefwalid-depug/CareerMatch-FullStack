package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;
import static com.careermatch.careermatch_backend.entity.DomainEnums.RequirementType;

@Entity
@Table(name = "job_skills", uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "skill_id", "requirement_type"}))
public class JobSkill {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id") private JobPosting job;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "skill_id") private Skill skill;
    @Enumerated(EnumType.STRING) @Column(name = "requirement_type", nullable = false) private RequirementType requirementType;
    @Column(name = "importance_weight", nullable = false) private int importanceWeight;
    @Column(columnDefinition = "text") private String evidence;

    protected JobSkill() {}
    public UUID getId() { return id; }
    public Skill getSkill() { return skill; }
    public RequirementType getRequirementType() { return requirementType; }
    public int getImportanceWeight() { return importanceWeight; }
    public String getEvidence() { return evidence; }
}
