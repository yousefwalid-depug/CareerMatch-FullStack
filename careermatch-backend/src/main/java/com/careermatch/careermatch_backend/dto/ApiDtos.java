package com.careermatch.careermatch_backend.dto;

import com.careermatch.careermatch_backend.entity.DomainEnums.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ApiDtos {
    private ApiDtos() {}

    public record HealthResponse(String status) {}
    public record JobSummary(UUID jobId, String title, String company, String location, JobSource source) {}
    public record JobListResponse(List<JobSummary> jobs, int page, int pageSize, long totalItems, int totalPages) {}
    public record JobDetailsResponse(UUID jobId, String title, String company, String description,
                                     List<String> requiredSkills, List<String> preferredSkills) {}
    public record CvUploadResponse(UUID cvId, String filename, boolean parsed) {}
    public record SkillEvidence(UUID skillId, String name, String evidence) {}
    public record ExperienceItem(UUID id, String jobTitle, String company, LocalDate startDate,
                                 LocalDate endDate, Integer durationMonths, String description) {}
    public record EducationItem(UUID id, String degree, String fieldOfStudy, String institution, String educationLevel) {}
    public record ProjectItem(UUID id, String title, String description) {}
    public record CandidateProfileResponse(UUID cvId, UUID profileId, Integer totalExperienceMonths,
                                           Confidence extractionConfidence, List<SkillEvidence> skills,
                                           List<ExperienceItem> experiences, List<EducationItem> education,
                                           List<ProjectItem> projects) {}
    public record MatchAnalyzeRequest(@NotNull UUID cvId, @NotNull UUID jobId) {}
    public record ScoreBreakdown(BigDecimal requiredSkills, BigDecimal experience, BigDecimal projects,
                                 BigDecimal education, BigDecimal preferredSkills) {}
    public record SkillMatch(String skill, MatchType matchType, BigDecimal matchValue,
                             BigDecimal similarityScore, String cvEvidence, String jobEvidence) {}
    public record EvidenceItem(String skill, String cvEvidence, String jobEvidence) {}
    public record StrengthItem(String strength, String evidence) {}
    public record ImprovementPlanItem(String gapName, GapCategory gapCategory, RequirementType requiredOrPreferred,
                                      int importanceWeight, String jobEvidence, String cvEvidence,
                                      List<String> relatedExistingStrengths, String recommendedAction, String deliverable,
                                      BigDecimal estimatedEffortMinDays, BigDecimal estimatedEffortMaxDays,
                                      BigDecimal expectedScoreGain, BigDecimal priorityScore,
                                      PriorityLabel priorityLabel, Confidence confidence,
                                      RecommendationStatus status) {}
    public record MatchResponse(UUID matchId, BigDecimal overallMatchScore, ScoreBreakdown scoreBreakdown,
                                List<SkillMatch> matchedRequiredSkills, List<SkillMatch> partialMatches,
                                List<SkillMatch> missingRequiredSkills, List<SkillMatch> matchedPreferredSkills,
                                List<SkillMatch> missingPreferredSkills, List<StrengthItem> strengths,
                                List<EvidenceItem> evidence, List<ImprovementPlanItem> improvementPlan,
                                Confidence extractionConfidence, boolean humanReviewFlag) {}
    public record ErrorResponse(Instant timestamp, int status, String error, String message,
                                String path, Map<String, String> fieldErrors) {}
}
