package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "match_strengths")
public class MatchStrength {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "match_id") private MatchResult match;
    @Column(nullable = false, columnDefinition = "text") private String strength;
    @Column(columnDefinition = "text") private String evidence;
    protected MatchStrength() {}
    public MatchStrength(UUID id, MatchResult match, String strength, String evidence) {
        this.id = id; this.match = match; this.strength = strength; this.evidence = evidence;
    }
    public String getStrength() { return strength; }
    public String getEvidence() { return evidence; }
}
