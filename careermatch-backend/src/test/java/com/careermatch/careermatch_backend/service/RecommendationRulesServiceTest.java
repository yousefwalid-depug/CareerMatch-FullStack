package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.entity.DomainEnums.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationRulesServiceTest {
    private final RecommendationRulesService rules = new RecommendationRulesService();

    @Test void requiredSkillUsesRequiredSkillsCategory() {
        assertEquals(GapCategory.REQUIRED_SKILLS, rules.category(RequirementType.REQUIRED));
    }

    @Test void preferredSkillUsesPreferredSkillsCategory() {
        assertEquals(GapCategory.PREFERRED_SKILLS, rules.category(RequirementType.PREFERRED));
    }

    @Test void effortLookupUsesDocumentedRangesAndMidpoints() {
        var small = rules.effortFor("JUnit");
        var medium = rules.effortFor("Spring Boot");
        var large = rules.effortFor("AWS");

        assertEquals(new BigDecimal("1"), small.minDays());
        assertEquals(new BigDecimal("2"), small.maxDays());
        assertEquals(new BigDecimal("1.50"), small.midpointDays());
        assertEquals(new BigDecimal("3"), medium.minDays());
        assertEquals(new BigDecimal("5"), medium.maxDays());
        assertEquals(new BigDecimal("4.00"), medium.midpointDays());
        assertEquals(new BigDecimal("7"), large.minDays());
        assertEquals(new BigDecimal("11"), large.maxDays());
        assertEquals(new BigDecimal("9.00"), large.midpointDays());
    }

    @Test void priorityUsesImportanceGainAndEffortMidpoint() {
        var priority = rules.priority(3, new BigDecimal("6"), rules.effortFor("Docker"));
        assertEquals(new BigDecimal("4.500"), priority.score());
        assertEquals(PriorityLabel.VERY_HIGH, priority.label());
    }

    @Test void priorityLabelsMatchDayTwoWorkedExamples() {
        assertEquals(PriorityLabel.MEDIUM,
                rules.priority(1, new BigDecimal("3"), rules.effortFor("CI/CD")).label());
        assertEquals(PriorityLabel.LOW,
                rules.priority(1, new BigDecimal("2"), rules.effortFor("AWS")).label());
    }

    @Test void relatedStrengthsAreLimitedToCandidateProfileSkills() {
        assertEquals(List.of("Java", "REST APIs"),
                rules.relatedStrengths("Docker", List.of("Python", "REST APIs", "Java")));
    }

    @Test void noDefensibleRelatedSkillProducesEmptyList() {
        assertEquals(List.of(), rules.relatedStrengths("Docker", List.of("Python", "JavaScript")));
    }
}
