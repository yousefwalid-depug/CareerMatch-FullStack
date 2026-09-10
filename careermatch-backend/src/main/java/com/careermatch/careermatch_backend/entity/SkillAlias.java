package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "skill_aliases")
public class SkillAlias {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "skill_id") private Skill skill;
    @Column(nullable = false, unique = true) private String alias;
    protected SkillAlias() {}
    public Skill getSkill() { return skill; }
    public String getAlias() { return alias; }
}
