package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.entity.DomainEnums.*;
import org.springframework.stereotype.Service;
import java.math.*;
import java.util.*;

@Service
public class RecommendationRulesService {
    private static final Set<String> LARGE_SKILLS = Set.of("AWS");
    private static final Set<String> MEDIUM_SKILLS = Set.of("Spring Boot", "PostgreSQL", "Docker", "CI/CD", "Angular");
    private static final Map<String, Set<String>> RELATED_SKILLS = Map.of(
            "Spring Boot", Set.of("Java", "REST APIs"),
            "PostgreSQL", Set.of("Java", "Spring Boot"),
            "Docker", Set.of("Java", "Spring Boot", "PostgreSQL", "REST APIs", "CI/CD"),
            "AWS", Set.of("Java", "Spring Boot", "Docker", "CI/CD"),
            "CI/CD", Set.of("Docker", "AWS"),
            "Angular", Set.of("JavaScript", "REST APIs"),
            "Java", Set.of("Spring Boot")
    );

    public GapCategory category(RequirementType type) {
        return type == RequirementType.REQUIRED ? GapCategory.REQUIRED_SKILLS : GapCategory.PREFERRED_SKILLS;
    }

    public EffortEstimate effortFor(String gapName) {
        if (LARGE_SKILLS.contains(gapName)) return new EffortEstimate(EffortSize.LARGE, days("7"), days("11"));
        if (MEDIUM_SKILLS.contains(gapName)) return new EffortEstimate(EffortSize.MEDIUM, days("3"), days("5"));
        return new EffortEstimate(EffortSize.SMALL, days("1"), days("2"));
    }

    public List<String> relatedStrengths(String gapName, Collection<String> candidateSkills) {
        Set<String> related = RELATED_SKILLS.getOrDefault(gapName, Set.of());
        return candidateSkills.stream().filter(related::contains).distinct().sorted().toList();
    }

    public Priority priority(int importanceWeight, BigDecimal expectedGain, EffortEstimate effort) {
        BigDecimal score = expectedGain.multiply(BigDecimal.valueOf(importanceWeight))
                .divide(effort.midpointDays(), 3, RoundingMode.HALF_UP);
        PriorityLabel label;
        if (score.compareTo(new BigDecimal("4")) >= 0) label = PriorityLabel.VERY_HIGH;
        else if (score.compareTo(new BigDecimal("2")) >= 0) label = PriorityLabel.HIGH;
        else if (score.compareTo(new BigDecimal("0.5")) >= 0) label = PriorityLabel.MEDIUM;
        else label = PriorityLabel.LOW;
        return new Priority(score, label);
    }

    private BigDecimal days(String value) { return new BigDecimal(value); }

    public enum EffortSize { SMALL, MEDIUM, LARGE }
    public record EffortEstimate(EffortSize size, BigDecimal minDays, BigDecimal maxDays) {
        public BigDecimal midpointDays() {
            return minDays.add(maxDays).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        }
    }
    public record Priority(BigDecimal score, PriorityLabel label) {}
}
