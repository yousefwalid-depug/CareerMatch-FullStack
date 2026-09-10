package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "candidate_project_skills", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "skill_id"}))
public class CandidateProjectSkill {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id") private CandidateProject project;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "skill_id") private Skill skill;
    @Column(columnDefinition = "text") private String evidence;
    protected CandidateProjectSkill() {}
    public CandidateProjectSkill(UUID id, CandidateProject project, Skill skill, String evidence) {
        this.id = id; this.project = project; this.skill = skill; this.evidence = evidence;
    }
    public CandidateProject getProject() { return project; }
    public Skill getSkill() { return skill; }
}
