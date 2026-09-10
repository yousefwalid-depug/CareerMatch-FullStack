package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.entity.*;
import com.careermatch.careermatch_backend.exception.NotFoundException;
import com.careermatch.careermatch_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.careermatch.careermatch_backend.entity.DomainEnums.*;

@Service
public class MatchQueryService {
    private final MatchResultRepository matches; private final MatchSkillResultRepository skillResults;
    private final MatchStrengthRepository strengths; private final ImprovementRecommendationRepository recommendations;
    public MatchQueryService(MatchResultRepository matches, MatchSkillResultRepository skillResults,
            MatchStrengthRepository strengths, ImprovementRecommendationRepository recommendations) {
        this.matches = matches; this.skillResults = skillResults; this.strengths = strengths; this.recommendations = recommendations;
    }

    @Transactional(readOnly = true)
    public MatchResponse get(UUID id) {
        MatchResult match = matches.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        List<MatchSkillResult> rows = skillResults.findByMatch_Id(id);
        List<SkillMatch> requiredFull = map(rows, RequirementType.REQUIRED, MatchType.FULL);
        List<SkillMatch> preferredFull = map(rows, RequirementType.PREFERRED, MatchType.FULL);
        List<SkillMatch> requiredMissing = map(rows, RequirementType.REQUIRED, MatchType.MISSING);
        List<SkillMatch> preferredMissing = map(rows, RequirementType.PREFERRED, MatchType.MISSING);
        List<SkillMatch> partial = rows.stream().filter(r -> r.getMatchType() == MatchType.PARTIAL).map(this::toSkillMatch).toList();
        List<StrengthItem> strengthItems = strengths.findByMatch_Id(id).stream()
                .map(s -> new StrengthItem(s.getStrength(), s.getEvidence())).toList();
        List<EvidenceItem> evidence = rows.stream().filter(r -> r.getCvEvidence() != null)
                .map(r -> new EvidenceItem(r.getJobSkill().getSkill().getCanonicalName(), r.getCvEvidence(), r.getJobEvidence())).toList();
        List<ImprovementPlanItem> plan = recommendations.findByMatch_IdOrderByPriorityScoreDesc(id).stream()
                .map(r -> new ImprovementPlanItem(r.getGapName(), r.getGapCategory(), r.getRequiredOrPreferred(),
                        r.getImportanceWeight(), r.getJobEvidence(), r.getCvEvidence(), r.getRelatedExistingStrengths(),
                        r.getRecommendedAction(), r.getDeliverable(),
                        r.getEstimatedEffortMinDays(), r.getEstimatedEffortMaxDays(), r.getExpectedScoreGain(),
                        r.getPriorityScore(), r.getPriorityLabel(), r.getConfidence(), r.getStatus())).toList();
        return new MatchResponse(match.getId(), match.getOverallMatchScore(),
                new ScoreBreakdown(match.getRequiredSkillsScore(), match.getExperienceScore(), match.getProjectsScore(),
                        match.getEducationScore(), match.getPreferredSkillsScore()), requiredFull, partial, requiredMissing,
                preferredFull, preferredMissing, strengthItems, evidence, plan, match.getExtractionConfidence(),
                match.isHumanReviewFlag());
    }

    private List<SkillMatch> map(List<MatchSkillResult> rows, RequirementType type, MatchType matchType) {
        return rows.stream().filter(r -> r.getJobSkill().getRequirementType() == type && r.getMatchType() == matchType)
                .map(this::toSkillMatch).toList();
    }
    private SkillMatch toSkillMatch(MatchSkillResult row) {
        return new SkillMatch(row.getJobSkill().getSkill().getCanonicalName(), row.getMatchType(), row.getMatchValue(),
                row.getSimilarityScore(), row.getCvEvidence(), row.getJobEvidence());
    }
}
