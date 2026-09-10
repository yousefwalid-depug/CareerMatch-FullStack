package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "candidate_profile_skills", uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "skill_id"}))
public class CandidateProfileSkill {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "profile_id") private CandidateProfile profile;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "skill_id") private Skill skill;
    @Column(columnDefinition = "text") private String evidence;

    protected CandidateProfileSkill() {}
    public CandidateProfileSkill(UUID id, CandidateProfile profile, Skill skill, String evidence) {
        this.id = id; this.profile = profile; this.skill = skill; this.evidence = evidence;
    }
    public UUID getId() { return id; }
    public Skill getSkill() { return skill; }
    public String getEvidence() { return evidence; }
}
