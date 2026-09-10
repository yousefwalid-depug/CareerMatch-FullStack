package com.careermatch.careermatch_backend.ai;

import com.careermatch.careermatch_backend.entity.DomainEnums.Confidence;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

@Service
public class MockCandidateProfileExtractionService implements CandidateProfileExtractionService {
    private static final Pattern YEARS = Pattern.compile("(?i)\\b(\\d{1,2})\\s*\\+?\\s*years?\\b");

    @Override
    public ExtractedProfile extract(String cvText, Collection<String> knownSkillTerms) {
        String lower = cvText.toLowerCase(Locale.ROOT);
        Set<String> detected = new LinkedHashSet<>();
        knownSkillTerms.stream().filter(term -> containsTerm(lower, term.toLowerCase(Locale.ROOT))).forEach(detected::add);
        Matcher matcher = YEARS.matcher(cvText);
        int months = matcher.find() ? Integer.parseInt(matcher.group(1)) * 12 : 0;
        return new ExtractedProfile(months, Confidence.LOW, detected);
    }

    private boolean containsTerm(String text, String term) {
        return Pattern.compile("(?<![a-z0-9])" + Pattern.quote(term) + "(?![a-z0-9])").matcher(text).find();
    }
}
