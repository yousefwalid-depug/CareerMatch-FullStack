package com.careermatch.careermatch_backend.ai;

import java.util.Set;

public interface JobRequirementExtractionService {
    ExtractedRequirements extract(String description);
    record ExtractedRequirements(Set<String> requiredSkills, Set<String> preferredSkills, boolean mock) {}
}
