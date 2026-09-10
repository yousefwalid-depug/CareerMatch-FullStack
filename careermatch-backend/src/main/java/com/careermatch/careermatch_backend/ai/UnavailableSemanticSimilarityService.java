package com.careermatch.careermatch_backend.ai;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UnavailableSemanticSimilarityService implements SemanticSimilarityService {
    @Override public Optional<BigDecimal> similarity(String candidateEvidence, String jobRequirement) {
        return Optional.empty();
    }
}
