package com.careermatch.careermatch_backend.ai;

import org.slf4j.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Collection;

@Primary
@Service
public class ResilientCandidateProfileExtractionService implements CandidateProfileExtractionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResilientCandidateProfileExtractionService.class);
    private final CandidateExtractionProperties properties;
    private final OpenAiCandidateProfileExtractionService openAi;
    private final MockCandidateProfileExtractionService fallback;

    public ResilientCandidateProfileExtractionService(CandidateExtractionProperties properties,
            OpenAiCandidateProfileExtractionService openAi, MockCandidateProfileExtractionService fallback) {
        this.properties = properties;
        this.openAi = openAi;
        this.fallback = fallback;
    }

    @Override
    public ExtractedProfile extract(String cvText, Collection<String> knownSkillTerms) {
        if (!properties.useOpenAi() || cvText == null || cvText.isBlank())
            return fallback.extract(cvText == null ? "" : cvText, knownSkillTerms);
        try {
            return openAi.extract(cvText, knownSkillTerms);
        } catch (RuntimeException failure) {
            LOGGER.warn("Candidate profile AI extraction failed after retry; using deterministic fallback ({}: {})",
                    failure.getClass().getSimpleName(), failure.getMessage());
            return fallback.extract(cvText, knownSkillTerms);
        }
    }
}
