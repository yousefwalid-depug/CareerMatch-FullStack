package com.careermatch.careermatch_backend.controller;

import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.entity.DomainEnums.*;
import com.careermatch.careermatch_backend.exception.GlobalExceptionHandler;
import com.careermatch.careermatch_backend.service.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MatchControllerTest {
    private MatchAnalysisService analysis; private MatchQueryService query; private MockMvc mvc;
    @BeforeEach void setup() {
        analysis = mock(MatchAnalysisService.class); query = mock(MatchQueryService.class);
        mvc = MockMvcBuilders.standaloneSetup(new MatchController(analysis, query))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void rejectsMissingIds() throws Exception {
        mvc.perform(post("/api/match/analyze").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.cvId").exists())
                .andExpect(jsonPath("$.fieldErrors.jobId").exists());
        verifyNoInteractions(analysis);
    }
    @Test void persistedGetDoesNotRunAnalysis() throws Exception {
        UUID id = UUID.randomUUID();
        ImprovementPlanItem recommendation = new ImprovementPlanItem("Spring Boot", GapCategory.REQUIRED_SKILLS,
                RequirementType.REQUIRED, 3, "Spring Boot REST services", null, List.of("Java"),
                "Build a REST feature", "Working endpoint", new BigDecimal("3"), new BigDecimal("5"),
                new BigDecimal("13.34"), new BigDecimal("10.005"), PriorityLabel.VERY_HIGH,
                Confidence.LOW, RecommendationStatus.NOT_STARTED);
        MatchResponse response = new MatchResponse(id, BigDecimal.TEN,
                new ScoreBreakdown(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(recommendation),
                Confidence.LOW, true);
        when(query.get(id)).thenReturn(response);
        mvc.perform(get("/api/match/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(id.toString()))
                .andExpect(jsonPath("$.improvementPlan[0].gapCategory").value("REQUIRED_SKILLS"))
                .andExpect(jsonPath("$.improvementPlan[0].jobEvidence").value("Spring Boot REST services"))
                .andExpect(jsonPath("$.improvementPlan[0].cvEvidence").doesNotExist())
                .andExpect(jsonPath("$.improvementPlan[0].relatedExistingStrengths[0]").value("Java"));
        verify(query).get(id); verifyNoInteractions(analysis);
    }
}
