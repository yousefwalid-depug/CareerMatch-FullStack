package com.careermatch.careermatch_backend.ai;

import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class MockJobRequirementExtractionService implements JobRequirementExtractionService {
    @Override public ExtractedRequirements extract(String description) {
        return new ExtractedRequirements(Set.of(), Set.of(), true);
    }
}
