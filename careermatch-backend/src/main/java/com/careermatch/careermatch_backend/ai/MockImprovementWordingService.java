package com.careermatch.careermatch_backend.ai;

import org.springframework.stereotype.Service;

@Service
public class MockImprovementWordingService implements ImprovementWordingService {
    @Override public Wording create(String skillName, boolean required) {
        String scope = required ? "job-ready" : "working";
        return new Wording("Build " + scope + " capability in " + skillName + " through a focused practical exercise.",
                "Publish a small project or case study demonstrating " + skillName + " with a README and tests.", true);
    }
}
