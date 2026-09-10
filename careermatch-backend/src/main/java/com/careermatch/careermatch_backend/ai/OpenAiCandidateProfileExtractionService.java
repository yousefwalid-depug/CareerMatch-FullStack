package com.careermatch.careermatch_backend.ai;

import com.careermatch.careermatch_backend.entity.DomainEnums.Confidence;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.*;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
public class OpenAiCandidateProfileExtractionService implements CandidateProfileExtractionService {
    private static final int MAX_ITEMS = 50;
    private static final Pattern PROTECTED_ATTRIBUTE_LINE = Pattern.compile(
            "(?i)^\\s*(age|date of birth|dob|gender|sex|race|ethnicity|religion|disability|marital status|nationality|citizenship|photograph|photo)\\s*[:=-].*$");
    private static final Pattern PROTECTED_ATTRIBUTE_EVIDENCE = Pattern.compile(
            "(?i)\\b(age|date of birth|dob|gender|sex|race|ethnicity|religion|disability|marital status|nationality|citizenship|photograph|photo)\\b\\s*[:=-]");
    private final CandidateExtractionProperties properties;
    private final ObjectMapper mapper;
    private final Transport transport;

    @Autowired
    public OpenAiCandidateProfileExtractionService(CandidateExtractionProperties properties, ObjectMapper mapper) {
        this(properties, mapper, new HttpClientTransport(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds())).build()));
    }

    OpenAiCandidateProfileExtractionService(CandidateExtractionProperties properties, ObjectMapper mapper,
                                              Transport transport) {
        this.properties = properties;
        this.mapper = mapper;
        this.transport = transport;
    }

    @Override
    public ExtractedProfile extract(String cvText, Collection<String> knownSkillTerms) {
        if (cvText == null || cvText.isBlank()) throw new ProviderFailure("CV text is empty");
        if (cvText.length() > properties.getMaxInputCharacters()) throw new ProviderFailure("CV text exceeds provider input limit");
        if (!properties.useOpenAi()) throw new ProviderFailure("OpenAI candidate extraction is not configured");
        String providerText = removeProtectedAttributeLines(cvText);
        if (providerText.isBlank()) throw new ProviderFailure("CV text contains no extractable profile content");

        ProviderFailure lastFailure = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                return execute(providerText, knownSkillTerms);
            } catch (ProviderFailure failure) {
                lastFailure = failure;
            }
        }
        throw Objects.requireNonNull(lastFailure);
    }

    private ExtractedProfile execute(String cvText, Collection<String> knownSkillTerms) {
        try {
            String request = requestBody(cvText, knownSkillTerms);
            ProviderResponse response = transport.post(URI.create(properties.responsesUrl()),
                    Duration.ofSeconds(properties.getTimeoutSeconds()), properties.getApiKey(), request);
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new ProviderFailure("OpenAI returned HTTP " + response.statusCode());
            String structuredText = findStructuredText(mapper.readTree(response.body()));
            ProviderProfile providerProfile = mapper.readValue(structuredText, ProviderProfile.class);
            return normalizeAndValidate(providerProfile, cvText, knownSkillTerms);
        } catch (ProviderFailure failure) {
            throw failure;
        } catch (Exception failure) {
            throw new ProviderFailure("OpenAI candidate extraction failed", failure);
        }
    }

    private String requestBody(String cvText, Collection<String> knownSkillTerms) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        root.put("model", requiredSetting(properties.getModel(), "model"));
        root.put("store", false);
        root.put("temperature", properties.getTemperature());
        root.put("max_output_tokens", properties.getMaxOutputTokens());
        root.put("instructions", CandidateExtractionPrompt.INSTRUCTIONS);
        root.put("input", "KNOWN_SKILL_TERMS:\n" + String.join(", ", knownSkillTerms) + "\n\nCV_TEXT:\n" + cvText);
        ObjectNode format = root.putObject("text").putObject("format");
        format.put("type", "json_schema");
        format.put("name", "candidate_profile");
        format.put("strict", true);
        format.set("schema", CandidateExtractionSchema.build(mapper, knownSkillTerms));
        return mapper.writeValueAsString(root);
    }

    private String requiredSetting(String value, String name) {
        if (value == null || value.isBlank()) throw new ProviderFailure("OpenAI " + name + " is not configured");
        return value.trim();
    }

    private String findStructuredText(JsonNode response) {
        JsonNode output = response.path("output");
        if (!output.isArray()) throw new ProviderFailure("OpenAI response has no output array");
        for (JsonNode item : output) {
            if (!"message".equals(item.path("type").asText())) continue;
            JsonNode content = item.path("content");
            if (!content.isArray()) continue;
            for (JsonNode part : content) {
                if ("output_text".equals(part.path("type").asText()) && part.path("text").isTextual())
                    return part.path("text").textValue();
            }
        }
        throw new ProviderFailure("OpenAI response contains no structured output text");
    }

    private ExtractedProfile normalizeAndValidate(ProviderProfile profile, String source,
                                                    Collection<String> knownSkillTerms) {
        if (profile == null || profile.skills == null || profile.experiences == null ||
                profile.education == null || profile.projects == null)
            throw new ProviderFailure("Candidate profile is incomplete");
        rejectOversized(profile.skills, "skills");
        rejectOversized(profile.experiences, "experiences");
        rejectOversized(profile.education, "education");
        rejectOversized(profile.projects, "projects");
        if (profile.totalExperienceMonths < 0 || profile.totalExperienceMonths > 1200)
            throw new ProviderFailure("Candidate experience is outside supported bounds");

        Confidence confidence;
        try { confidence = Confidence.valueOf(required(profile.extractionConfidence, 20, "confidence")); }
        catch (IllegalArgumentException failure) { throw new ProviderFailure("Candidate confidence is unsupported"); }

        Map<String, String> known = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        knownSkillTerms.stream().filter(Objects::nonNull).map(String::trim).filter(term -> !term.isEmpty())
                .forEach(term -> known.putIfAbsent(term, term));
        LinkedHashSet<String> skillTerms = new LinkedHashSet<>();
        LinkedHashMap<String, String> evidenceBySkill = new LinkedHashMap<>();
        for (ProviderSkill item : profile.skills) {
            String term = supportedSkill(item.term, known);
            String evidence = groundedEvidence(item.evidence, source, "skill evidence");
            skillTerms.add(term);
            evidenceBySkill.putIfAbsent(term, evidence);
        }

        List<ExtractedExperience> experiences = profile.experiences.stream().map(item -> {
            String evidence = groundedEvidence(item.evidence, source, "experience evidence");
            String title = groundedOptional(item.jobTitle, source, 255, "job title");
            String company = groundedOptional(item.company, source, 255, "company");
            if (title == null && company == null) throw new ProviderFailure("Experience has no supported role or company");
            Integer duration = boundedNullable(item.durationMonths, 0, 1200, "experience duration");
            LocalDate start = date(item.startDate, "experience start date");
            LocalDate end = date(item.endDate, "experience end date");
            if (start != null && end != null && end.isBefore(start)) throw new ProviderFailure("Experience dates are inconsistent");
            return new ExtractedExperience(title, company, start, end, duration, evidence);
        }).toList();

        List<ExtractedEducation> education = profile.education.stream().map(item -> {
            groundedEvidence(item.evidence, source, "education evidence");
            String degree = groundedOptional(item.degree, source, 255, "degree");
            String field = groundedOptional(item.fieldOfStudy, source, 255, "field of study");
            String institution = groundedOptional(item.institution, source, 255, "institution");
            // Education level is a normalized category (for example BACHELORS), so its grounded
            // education evidence is authoritative even when that exact category token is absent.
            String level = optional(item.educationLevel, 100, "education level");
            if (degree == null && institution == null && level == null)
                throw new ProviderFailure("Education item has no supported details");
            return new ExtractedEducation(degree, field, institution, level, normalize(item.evidence));
        }).toList();

        List<ExtractedProject> projects = profile.projects.stream().map(item -> {
            String title = groundedRequired(item.title, source, 255, "project title");
            String evidence = groundedEvidence(item.evidence, source, "project evidence");
            if (item.skillTerms == null) throw new ProviderFailure("Project skill terms are missing");
            LinkedHashSet<String> projectTerms = new LinkedHashSet<>();
            item.skillTerms.forEach(term -> projectTerms.add(supportedSkill(term, known)));
            return new ExtractedProject(title, evidence, projectTerms);
        }).toList();

        return new ExtractedProfile(profile.totalExperienceMonths, confidence, skillTerms, evidenceBySkill,
                experiences, education, projects);
    }

    private void rejectOversized(Collection<?> values, String name) {
        if (values.size() > MAX_ITEMS) throw new ProviderFailure("Too many extracted " + name);
    }

    private String supportedSkill(String value, Map<String, String> known) {
        String term = required(value, 100, "skill term");
        String supported = known.get(term);
        if (supported == null) throw new ProviderFailure("Candidate profile contains an unsupported skill term");
        return supported;
    }

    private String groundedEvidence(String value, String source, String name) {
        String evidence = groundedRequired(value, source, 800, name);
        if (PROTECTED_ATTRIBUTE_EVIDENCE.matcher(evidence).find())
            throw new ProviderFailure("Candidate evidence contains a protected attribute");
        return evidence;
    }

    private String removeProtectedAttributeLines(String source) {
        return source.lines().filter(line -> !PROTECTED_ATTRIBUTE_LINE.matcher(line).matches())
                .reduce((left, right) -> left + "\n" + right).orElse("");
    }

    private String groundedRequired(String value, String source, int maxLength, String name) {
        String normalized = required(value, maxLength, name);
        if (!containsNormalized(source, normalized)) throw new ProviderFailure("Candidate " + name + " is not grounded");
        return normalized;
    }

    private String groundedOptional(String value, String source, int maxLength, String name) {
        String normalized = optional(value, maxLength, name);
        if (normalized != null && !containsNormalized(source, normalized))
            throw new ProviderFailure("Candidate " + name + " is not grounded");
        return normalized;
    }

    private boolean containsNormalized(String source, String value) {
        return normalize(source).toLowerCase(Locale.ROOT).contains(normalize(value).toLowerCase(Locale.ROOT));
    }

    private String required(String value, int maxLength, String name) {
        String normalized = optional(value, maxLength, name);
        if (normalized == null) throw new ProviderFailure("Candidate " + name + " is missing");
        return normalized;
    }

    private String optional(String value, int maxLength, String name) {
        if (value == null || value.isBlank()) return null;
        String normalized = normalize(value);
        if (normalized.length() > maxLength) throw new ProviderFailure("Candidate " + name + " is too long");
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private Integer boundedNullable(Integer value, int min, int max, String name) {
        if (value != null && (value < min || value > max)) throw new ProviderFailure("Candidate " + name + " is unsupported");
        return value;
    }

    private LocalDate date(String value, String name) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException failure) { throw new ProviderFailure("Candidate " + name + " is malformed"); }
    }

    interface Transport {
        ProviderResponse post(URI uri, Duration timeout, String apiKey, String jsonBody) throws Exception;
    }

    record ProviderResponse(int statusCode, String body) {}

    private record ProviderProfile(
            @JsonProperty("total_experience_months") int totalExperienceMonths,
            @JsonProperty("extraction_confidence") String extractionConfidence,
            List<ProviderSkill> skills,
            List<ProviderExperience> experiences,
            List<ProviderEducation> education,
            List<ProviderProject> projects) {}
    private record ProviderSkill(String term, String evidence) {}
    private record ProviderExperience(
            @JsonProperty("job_title") String jobTitle,
            String company,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate,
            @JsonProperty("duration_months") Integer durationMonths,
            String evidence) {}
    private record ProviderEducation(
            String degree,
            @JsonProperty("field_of_study") String fieldOfStudy,
            String institution,
            @JsonProperty("education_level") String educationLevel,
            String evidence) {}
    private record ProviderProject(String title, String evidence,
                                   @JsonProperty("skill_terms") List<String> skillTerms) {}

    static final class ProviderFailure extends RuntimeException {
        ProviderFailure(String message) { super(message); }
        ProviderFailure(String message, Throwable cause) { super(message, cause); }
    }

    private record HttpClientTransport(HttpClient client) implements Transport {
        @Override
        public ProviderResponse post(URI uri, Duration timeout, String apiKey, String jsonBody) throws Exception {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return new ProviderResponse(response.statusCode(), response.body());
            } catch (InterruptedException failure) {
                Thread.currentThread().interrupt();
                throw failure;
            }
        }
    }
}
