package com.careermatch.careermatch_backend.ai;

import java.math.BigDecimal;
import java.util.Optional;

public interface SemanticSimilarityService {
    Optional<BigDecimal> similarity(String candidateEvidence, String jobRequirement);
}
