package com.careermatch.careermatch_backend.ai;

import com.careermatch.careermatch_backend.entity.DomainEnums.Confidence;
import java.time.LocalDate;
import java.util.*;

public interface CandidateProfileExtractionService {
    ExtractedProfile extract(String cvText, Collection<String> knownSkillTerms);

    record ExtractedProfile(
            int totalExperienceMonths,
            Confidence confidence,
            Set<String> detectedSkillTerms,
            Map<String, String> skillEvidenceByTerm,
            List<ExtractedExperience> experiences,
            List<ExtractedEducation> education,
            List<ExtractedProject> projects) {
        public ExtractedProfile(int totalExperienceMonths, Confidence confidence, Set<String> detectedSkillTerms) {
            this(totalExperienceMonths, confidence, detectedSkillTerms, Map.of(), List.of(), List.of(), List.of());
        }

        public ExtractedProfile {
            if (totalExperienceMonths < 0) throw new IllegalArgumentException("Experience months cannot be negative");
            confidence = Objects.requireNonNull(confidence, "confidence");
            detectedSkillTerms = Set.copyOf(Objects.requireNonNullElse(detectedSkillTerms, Set.of()));
            skillEvidenceByTerm = Map.copyOf(Objects.requireNonNullElse(skillEvidenceByTerm, Map.of()));
            experiences = List.copyOf(Objects.requireNonNullElse(experiences, List.of()));
            education = List.copyOf(Objects.requireNonNullElse(education, List.of()));
            projects = List.copyOf(Objects.requireNonNullElse(projects, List.of()));
        }

        public String evidenceFor(String skillTerm) {
            return skillEvidenceByTerm.getOrDefault(skillTerm, skillTerm);
        }
    }

    record ExtractedExperience(String jobTitle, String company, LocalDate startDate, LocalDate endDate,
                               Integer durationMonths, String evidence) {}
    record ExtractedEducation(String degree, String fieldOfStudy, String institution,
                              String educationLevel, String evidence) {}
    record ExtractedProject(String title, String evidence, Set<String> skillTerms) {
        public ExtractedProject {
            skillTerms = Set.copyOf(Objects.requireNonNullElse(skillTerms, Set.of()));
        }
    }
}
