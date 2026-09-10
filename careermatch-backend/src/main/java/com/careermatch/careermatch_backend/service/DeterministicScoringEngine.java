package com.careermatch.careermatch_backend.service;

import org.springframework.stereotype.Service;
import java.math.*;
import java.util.*;

@Service
public class DeterministicScoringEngine {
    public static final String VERSION = "day3-v1";
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public Score calculate(Input input) {
        BigDecimal required = skillScore(input.requiredSkillValues());
        BigDecimal preferred = skillScore(input.preferredSkillValues());
        int expectedMonths = input.expectedExperienceMonths() == null ? 12 : input.expectedExperienceMonths();
        BigDecimal experience = expectedMonths <= 0 ? HUNDRED : percentage(
                Math.min(Math.max(input.candidateExperienceMonths(), 0), expectedMonths), expectedMonths);
        BigDecimal projects = projectScore(input.requiredProjectCoverage(), input.qualifyingProjectCount());
        BigDecimal education = clamp(input.educationScore());
        BigDecimal overall = overall(required, experience, projects, education, preferred);
        return new Score(overall, required, experience, projects, education, preferred);
    }

    public BigDecimal expectedGain(Input current, boolean requiredSkill, int skillIndex) {
        Score before = calculate(current);
        List<BigDecimal> required = new ArrayList<>(current.requiredSkillValues());
        List<BigDecimal> preferred = new ArrayList<>(current.preferredSkillValues());
        List<BigDecimal> target = requiredSkill ? required : preferred;
        if (skillIndex < 0 || skillIndex >= target.size()) throw new IllegalArgumentException("Skill index is outside the score input");
        target.set(skillIndex, BigDecimal.ONE);
        Score after = calculate(new Input(required, preferred, current.candidateExperienceMonths(),
                current.expectedExperienceMonths(), current.requiredProjectCoverage(), current.qualifyingProjectCount(),
                current.educationScore()));
        return after.overall().subtract(before.overall()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal skillScore(List<BigDecimal> values) {
        if (values.isEmpty()) return HUNDRED;
        BigDecimal sum = values.stream().map(this::clampMatch).reduce(BigDecimal.ZERO, BigDecimal::add);
        return percentage(sum, BigDecimal.valueOf(values.size()));
    }

    private BigDecimal projectScore(BigDecimal coverage, int projectCount) {
        BigDecimal depth = projectCount <= 0 ? BigDecimal.ZERO : projectCount == 1 ? new BigDecimal("0.93") : BigDecimal.ONE;
        return clamp(coverage.multiply(depth).multiply(HUNDRED));
    }

    private BigDecimal overall(BigDecimal required, BigDecimal experience, BigDecimal projects,
                               BigDecimal education, BigDecimal preferred) {
        return required.multiply(new BigDecimal("0.40"))
                .add(experience.multiply(new BigDecimal("0.20")))
                .add(projects.multiply(new BigDecimal("0.15")))
                .add(education.multiply(new BigDecimal("0.10")))
                .add(preferred.multiply(new BigDecimal("0.15")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(int numerator, int denominator) {
        return percentage(BigDecimal.valueOf(numerator), BigDecimal.valueOf(denominator));
    }
    private BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        return numerator.divide(denominator, 8, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal clamp(BigDecimal score) { return score.max(BigDecimal.ZERO).min(HUNDRED).setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal clampMatch(BigDecimal value) { return value.max(BigDecimal.ZERO).min(BigDecimal.ONE); }

    public record Input(List<BigDecimal> requiredSkillValues, List<BigDecimal> preferredSkillValues,
                        int candidateExperienceMonths, Integer expectedExperienceMonths,
                        BigDecimal requiredProjectCoverage, int qualifyingProjectCount,
                        BigDecimal educationScore) {
        public Input {
            requiredSkillValues = List.copyOf(requiredSkillValues);
            preferredSkillValues = List.copyOf(preferredSkillValues);
        }
    }
    public record Score(BigDecimal overall, BigDecimal requiredSkills, BigDecimal experience,
                        BigDecimal projects, BigDecimal education, BigDecimal preferredSkills) {}
}
