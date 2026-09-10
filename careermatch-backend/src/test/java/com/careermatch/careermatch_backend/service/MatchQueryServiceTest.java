package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.entity.*;
import com.careermatch.careermatch_backend.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static com.careermatch.careermatch_backend.entity.DomainEnums.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchQueryServiceTest {
    @Test void returnsStoredRecommendationEvidenceWithoutRecomputingIt() {
        UUID matchId = UUID.randomUUID();
        MatchResult match = new MatchResult(matchId, null, null, new BigDecimal("13.33"),
                new BigDecimal("33.33"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, Confidence.LOW, true, "day2-v1", Instant.now());
        ImprovementRecommendation stored = new ImprovementRecommendation(UUID.randomUUID(), match, null,
                "Spring Boot", GapCategory.REQUIRED_SKILLS, RequirementType.REQUIRED, 3,
                "Spring Boot REST services", null, List.of("Java"), "Build a REST feature",
                "Working endpoint", new BigDecimal("3"), new BigDecimal("5"), new BigDecimal("13.33"),
                new BigDecimal("9.998"), PriorityLabel.VERY_HIGH, Confidence.LOW, Instant.now());

        MatchResultRepository matches = mock(MatchResultRepository.class);
        MatchSkillResultRepository skillResults = mock(MatchSkillResultRepository.class);
        MatchStrengthRepository strengths = mock(MatchStrengthRepository.class);
        ImprovementRecommendationRepository recommendations = mock(ImprovementRecommendationRepository.class);
        when(matches.findById(matchId)).thenReturn(Optional.of(match));
        when(skillResults.findByMatch_Id(matchId)).thenReturn(List.of());
        when(strengths.findByMatch_Id(matchId)).thenReturn(List.of());
        when(recommendations.findByMatch_IdOrderByPriorityScoreDesc(matchId)).thenReturn(List.of(stored));

        var response = new MatchQueryService(matches, skillResults, strengths, recommendations).get(matchId);
        var item = response.improvementPlan().getFirst();

        assertEquals(GapCategory.REQUIRED_SKILLS, item.gapCategory());
        assertEquals("Spring Boot REST services", item.jobEvidence());
        assertNull(item.cvEvidence());
        assertEquals(List.of("Java"), item.relatedExistingStrengths());
        verify(recommendations).findByMatch_IdOrderByPriorityScoreDesc(matchId);
        verifyNoMoreInteractions(recommendations);
    }
}
