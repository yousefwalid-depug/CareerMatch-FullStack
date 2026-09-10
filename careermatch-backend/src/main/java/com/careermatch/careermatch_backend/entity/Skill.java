package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "skills")
public class Skill {
    @Id private UUID id;
    @Column(name = "canonical_name", nullable = false, unique = true) private String canonicalName;

    protected Skill() {}
    public UUID getId() { return id; }
    public String getCanonicalName() { return canonicalName; }
}
