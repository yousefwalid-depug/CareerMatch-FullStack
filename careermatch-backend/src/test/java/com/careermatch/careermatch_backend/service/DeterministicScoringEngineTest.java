package com.careermatch.careermatch_backend.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeterministicScoringEngineTest {
    private final DeterministicScoringEngine engine = new DeterministicScoringEngine();

    @Test void calculatesSpecifiedWeightedScore() {
        var input = new DeterministicScoringEngine.Input(
                List.of(BigDecimal.ONE, new BigDecimal("0.5"), BigDecimal.ZERO),
                List.of(BigDecimal.ONE, BigDecimal.ZERO), 6, 12,
                new BigDecimal("0.66666667"), 1, new BigDecimal("100"));
        var score = engine.calculate(input);
        assertEquals(new BigDecimal("50.00"), score.requiredSkills());
        assertEquals(new BigDecimal("50.00"), score.experience());
        assertEquals(new BigDecimal("62.00"), score.projects());
        assertEquals(new BigDecimal("50.00"), score.preferredSkills());
        assertEquals(new BigDecimal("56.80"), score.overall());
    }

    @Test void recalculatesExpectedGainByClosingOneGap() {
        var input = new DeterministicScoringEngine.Input(List.of(BigDecimal.ONE, BigDecimal.ZERO),
                List.of(), 12, 12, BigDecimal.ZERO, 0, new BigDecimal("100"));
        assertEquals(new BigDecimal("20.00"), engine.expectedGain(input, true, 1));
    }
}
