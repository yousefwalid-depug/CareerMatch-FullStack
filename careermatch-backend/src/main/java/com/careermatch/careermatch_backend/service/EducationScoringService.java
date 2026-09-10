package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.entity.CandidateEducation;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class EducationScoringService {
    public BigDecimal score(String jobPreference, List<CandidateEducation> education) {
        if (jobPreference == null || jobPreference.isBlank()) return new BigDecimal("100");
        return education.isEmpty() ? BigDecimal.ZERO : new BigDecimal("100");
    }
}
