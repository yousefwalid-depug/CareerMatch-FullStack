package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.ai.ImprovementWordingService;
import com.careermatch.careermatch_backend.dto.ApiDtos.MatchResponse;
import com.careermatch.careermatch_backend.entity.*;
import com.careermatch.careermatch_backend.exception.*;
import com.careermatch.careermatch_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import static com.careermatch.careermatch_backend.entity.DomainEnums.*;

@Service
public class DefaultMatchAnalysisService implements MatchAnalysisService {
    private final CvRepository cvs; private final JobPostingRepository jobs;
    private final CandidateProfileRepository profiles; private final CandidateProfileSkillRepository profileSkills;
    private final CandidateEducationRepository education; private final CandidateProjectSkillRepository projectSkills;
    private final JobSkillRepository jobSkills; private final MatchResultRepository matches;
    private final MatchSkillResultRepository skillResults; private final MatchStrengthRepository strengths;
    private final ImprovementRecommendationRepository recommendations; private final DeterministicScoringEngine scoring;
    private final EducationScoringService educationScoring; private final ImprovementWordingService wording;
    private final RecommendationRulesService recommendationRules; private final MatchQueryService query;

    public DefaultMatchAnalysisService(CvRepository cvs, JobPostingRepository jobs, CandidateProfileRepository profiles,
            CandidateProfileSkillRepository profileSkills, CandidateEducationRepository education,
            CandidateProjectSkillRepository projectSkills, JobSkillRepository jobSkills, MatchResultRepository matches,
            MatchSkillResultRepository skillResults, MatchStrengthRepository strengths,
            ImprovementRecommendationRepository recommendations, DeterministicScoringEngine scoring,
            EducationScoringService educationScoring, ImprovementWordingService wording,
            RecommendationRulesService recommendationRules, MatchQueryService query) {
        this.cvs = cvs; this.jobs = jobs; this.profiles = profiles; this.profileSkills = profileSkills;
        this.education = education; this.projectSkills = projectSkills; this.jobSkills = jobSkills; this.matches = matches;
        this.skillResults = skillResults; this.strengths = strengths; this.recommendations = recommendations;
        this.scoring = scoring; this.educationScoring = educationScoring; this.wording = wording;
        this.recommendationRules = recommendationRules; this.query = query;
    }

    @Override @Transactional
    public MatchResponse analyze(UUID cvId, UUID jobId) {
        Cv cv = cvs.findById(cvId).orElseThrow(() -> new NotFoundException("CV not found: " + cvId));
        JobPosting job = jobs.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found: " + jobId));
        CandidateProfile profile = profiles.findByCv_Id(cvId).orElseThrow(() ->
                new ApiException(org.springframework.http.HttpStatus.CONFLICT, "The CV profile is not ready for matching"));

        List<CandidateProfileSkill> candidateSkills = profileSkills.findByProfile_Id(profile.getId());
        Map<UUID, CandidateProfileSkill> candidateBySkill = candidateSkills.stream()
                .collect(Collectors.toMap(s -> s.getSkill().getId(), s -> s));
        Set<String> candidateSkillNames = candidateSkills.stream()
                .map(s -> s.getSkill().getCanonicalName()).collect(Collectors.toSet());
        List<JobSkill> requirements = jobSkills.findByJob_IdOrderByRequirementTypeAscImportanceWeightDesc(jobId);
        List<JobSkill> required = requirements.stream().filter(s -> s.getRequirementType() == RequirementType.REQUIRED).toList();
        List<JobSkill> preferred = requirements.stream().filter(s -> s.getRequirementType() == RequirementType.PREFERRED).toList();
        List<BigDecimal> requiredValues = values(required, candidateBySkill);
        List<BigDecimal> preferredValues = values(preferred, candidateBySkill);

        Set<UUID> requiredIds = required.stream().map(s -> s.getSkill().getId()).collect(Collectors.toSet());
        List<CandidateProjectSkill> projectSkillRows = projectSkills.findByProject_Profile_Id(profile.getId());
        long covered = projectSkillRows.stream().map(s -> s.getSkill().getId()).filter(requiredIds::contains).distinct().count();
        long qualifyingProjects = projectSkillRows.stream().filter(s -> requiredIds.contains(s.getSkill().getId()))
                .map(s -> s.getProject().getId()).distinct().count();
        BigDecimal coverage = requiredIds.isEmpty() ? BigDecimal.ONE : BigDecimal.valueOf(covered)
                .divide(BigDecimal.valueOf(requiredIds.size()), 8, RoundingMode.HALF_UP);
        BigDecimal educationScore = educationScoring.score(job.getEducationPreference(), education.findByProfile_Id(profile.getId()));
        var input = new DeterministicScoringEngine.Input(requiredValues, preferredValues,
                Optional.ofNullable(profile.getTotalExperienceMonths()).orElse(0), job.getExpectedExperienceMonths(),
                coverage, Math.toIntExact(qualifyingProjects), educationScore);
        var score = scoring.calculate(input);
        Confidence confidence = profile.getExtractionConfidence() == Confidence.HIGH &&
                job.getRequirementsExtractionConfidence() == Confidence.HIGH ? Confidence.HIGH : Confidence.LOW;
        Instant now = Instant.now();
        MatchResult match = matches.save(new MatchResult(UUID.randomUUID(), cv, job, score.overall(), score.requiredSkills(),
                score.experience(), score.projects(), score.education(), score.preferredSkills(), confidence,
                confidence == Confidence.LOW, DeterministicScoringEngine.VERSION, now));

        persistSkillRows(match, required, candidateBySkill, candidateSkillNames, input, true, confidence, now);
        persistSkillRows(match, preferred, candidateBySkill, candidateSkillNames, input, false, confidence, now);
        matches.flush();
        return query.get(match.getId());
    }

    private List<BigDecimal> values(List<JobSkill> requirements, Map<UUID, CandidateProfileSkill> candidateBySkill) {
        return requirements.stream().map(s -> candidateBySkill.containsKey(s.getSkill().getId()) ? BigDecimal.ONE : BigDecimal.ZERO).toList();
    }

    private void persistSkillRows(MatchResult match, List<JobSkill> requirements,
                                  Map<UUID, CandidateProfileSkill> candidateBySkill,
                                  Set<String> candidateSkillNames, DeterministicScoringEngine.Input input,
                                  boolean required, Confidence confidence, Instant now) {
        for (int i = 0; i < requirements.size(); i++) {
            JobSkill jobSkill = requirements.get(i);
            CandidateProfileSkill candidate = candidateBySkill.get(jobSkill.getSkill().getId());
            MatchType type = candidate == null ? MatchType.MISSING : MatchType.FULL;
            MatchSkillResult row = skillResults.save(new MatchSkillResult(UUID.randomUUID(), match, jobSkill, candidate,
                    type, candidate == null ? BigDecimal.ZERO : BigDecimal.ONE, null,
                    candidate == null ? null : candidate.getEvidence(), jobSkill.getEvidence()));
            if (type == MatchType.FULL) {
                strengths.save(new MatchStrength(UUID.randomUUID(), match,
                        "Demonstrated " + jobSkill.getSkill().getCanonicalName(), candidate.getEvidence()));
            } else {
                BigDecimal gain = scoring.expectedGain(input, required, i);
                String gapName = jobSkill.getSkill().getCanonicalName();
                var effort = recommendationRules.effortFor(gapName);
                var priority = recommendationRules.priority(jobSkill.getImportanceWeight(), gain, effort);
                var copy = wording.create(gapName, required);
                recommendations.save(new ImprovementRecommendation(UUID.randomUUID(), match, row,
                        gapName, recommendationRules.category(jobSkill.getRequirementType()),
                        jobSkill.getRequirementType(), jobSkill.getImportanceWeight(), jobSkill.getEvidence(),
                        candidate == null ? null : candidate.getEvidence(),
                        recommendationRules.relatedStrengths(gapName, candidateSkillNames), copy.action(), copy.deliverable(),
                        effort.minDays(), effort.maxDays(), gain, priority.score(), priority.label(), confidence, now));
            }
        }
    }
}
