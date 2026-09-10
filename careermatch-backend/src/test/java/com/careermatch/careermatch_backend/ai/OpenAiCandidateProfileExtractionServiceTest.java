package com.careermatch.careermatch_backend.ai;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.careermatch.careermatch_backend.entity.DomainEnums.Confidence.HIGH;
import static com.careermatch.careermatch_backend.entity.DomainEnums.Confidence.LOW;
import static org.junit.jupiter.api.Assertions.*;

class OpenAiCandidateProfileExtractionServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsesAndGroundsStructuredCandidateProfile() throws Exception {
        String source = """
                Nationality: Egyptian
                BSc Computer Science, Cairo University
                Backend Intern at Nile Labs from 2025-01-01 to 2025-06-30. Built Java REST APIs.
                CareerMatch Portal - Java REST APIs.
                """;
        String profile = """
                {
                  "total_experience_months": 6,
                  "extraction_confidence": "HIGH",
                  "skills": [
                    {"term":"Java","evidence":"Java"},
                    {"term":"REST APIs","evidence":"REST APIs"}
                  ],
                  "experiences": [{
                    "job_title":"Backend Intern","company":"Nile Labs",
                    "start_date":"2025-01-01","end_date":"2025-06-30","duration_months":6,
                    "evidence":"Backend Intern at Nile Labs from 2025-01-01 to 2025-06-30"
                  }],
                  "education": [{
                    "degree":"BSc Computer Science","field_of_study":"Computer Science",
                    "institution":"Cairo University","education_level":"BACHELORS",
                    "evidence":"BSc Computer Science, Cairo University"
                  }],
                  "projects": [{
                    "title":"CareerMatch Portal","evidence":"CareerMatch Portal - Java REST APIs",
                    "skill_terms":["Java","REST APIs"]
                  }]
                }
                """;
        String[] sent = new String[1];
        var service = service((uri, timeout, key, body) -> {
            sent[0] = body;
            return new OpenAiCandidateProfileExtractionService.ProviderResponse(200, response(profile));
        });

        var result = service.extract(source, List.of("Java", "REST APIs", "Python"));

        assertEquals(6, result.totalExperienceMonths());
        assertEquals(HIGH, result.confidence());
        assertEquals(Set.of("Java", "REST APIs"), result.detectedSkillTerms());
        assertEquals("Java", result.evidenceFor("Java"));
        assertEquals("Backend Intern", result.experiences().getFirst().jobTitle());
        assertEquals("Cairo University", result.education().getFirst().institution());
        assertEquals("BACHELORS", result.education().getFirst().educationLevel());
        assertEquals("CareerMatch Portal", result.projects().getFirst().title());
        assertTrue(sent[0].contains("\"store\":false"));
        assertTrue(sent[0].contains("\"type\":\"json_schema\""));
        assertTrue(sent[0].contains("\"strict\":true"));
        assertFalse(sent[0].toLowerCase().contains("date_of_birth"));
        assertFalse(sent[0].contains("Egyptian"));
    }

    @Test
    void rejectsUngroundedProviderEvidenceAfterOneRetry() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        String profile = minimalProfile("Java", "Kubernetes");
        var service = service((uri, timeout, key, body) -> {
            attempts.incrementAndGet();
            return new OpenAiCandidateProfileExtractionService.ProviderResponse(200, response(profile));
        });

        assertThrows(OpenAiCandidateProfileExtractionService.ProviderFailure.class,
                () -> service.extract("Java developer", List.of("Java")));
        assertEquals(2, attempts.get());
    }

    @Test
    void rejectsMalformedStructuredResponseAfterOneRetry() {
        AtomicInteger attempts = new AtomicInteger();
        var service = service((uri, timeout, key, body) -> {
            attempts.incrementAndGet();
            return new OpenAiCandidateProfileExtractionService.ProviderResponse(200, "{\"output\":[]}");
        });

        assertThrows(OpenAiCandidateProfileExtractionService.ProviderFailure.class,
                () -> service.extract("Java developer", List.of("Java")));
        assertEquals(2, attempts.get());
    }

    @Test
    void rejectsUnsupportedConfidence() throws Exception {
        String profile = minimalProfile("Java", "Java").replace("\"LOW\"", "\"MEDIUM\"");
        var service = service((uri, timeout, key, body) ->
                new OpenAiCandidateProfileExtractionService.ProviderResponse(200, response(profile)));

        assertThrows(OpenAiCandidateProfileExtractionService.ProviderFailure.class,
                () -> service.extract("Java developer", List.of("Java")));
    }

    @Test
    void rejectsProtectedAttributeEvidence() throws Exception {
        String profile = minimalProfile("Java", "Gender: female");
        var service = service((uri, timeout, key, body) ->
                new OpenAiCandidateProfileExtractionService.ProviderResponse(200, response(profile)));

        assertThrows(OpenAiCandidateProfileExtractionService.ProviderFailure.class,
                () -> service.extract("Java developer\nGender: female", List.of("Java")));
    }

    @Test
    void providerFailureFallsBackWithoutLeakingTheFailure() {
        AtomicInteger attempts = new AtomicInteger();
        CandidateExtractionProperties properties = properties();
        var openAi = new OpenAiCandidateProfileExtractionService(properties, mapper, (uri, timeout, key, body) -> {
            attempts.incrementAndGet();
            throw new java.net.http.HttpTimeoutException("timed out");
        });
        var resilient = new ResilientCandidateProfileExtractionService(properties, openAi,
                new MockCandidateProfileExtractionService());

        var result = resilient.extract("Java developer with enough resume content for extraction.", List.of("Java"));

        assertEquals(LOW, result.confidence());
        assertEquals(Set.of("Java"), result.detectedSkillTerms());
        assertEquals(2, attempts.get());
    }

    @Test
    void missingConfigurationUsesDeterministicFallback() {
        CandidateExtractionProperties properties = new CandidateExtractionProperties();
        var openAi = new OpenAiCandidateProfileExtractionService(properties, mapper,
                (uri, timeout, key, body) -> fail("Provider must not be called without configuration"));
        var resilient = new ResilientCandidateProfileExtractionService(properties, openAi,
                new MockCandidateProfileExtractionService());

        var result = resilient.extract("Built services with Java.", List.of("Java"));

        assertEquals(LOW, result.confidence());
        assertEquals(Set.of("Java"), result.detectedSkillTerms());
    }

    @Test
    void directProviderRejectsEmptyInputWithoutCallingTransport() {
        var service = service((uri, timeout, key, body) ->
                fail("Provider must not be called for empty input"));

        assertThrows(OpenAiCandidateProfileExtractionService.ProviderFailure.class,
                () -> service.extract("   ", List.of("Java")));
    }

    @Test
    void resilientServiceReturnsEmptyLowConfidenceProfileForEmptyInput() {
        CandidateExtractionProperties properties = properties();
        var openAi = new OpenAiCandidateProfileExtractionService(properties, mapper,
                (uri, timeout, key, body) -> fail("Provider must not be called for empty input"));
        var resilient = new ResilientCandidateProfileExtractionService(properties, openAi,
                new MockCandidateProfileExtractionService());

        var result = resilient.extract("   ", List.of("Java"));

        assertEquals(0, result.totalExperienceMonths());
        assertEquals(LOW, result.confidence());
        assertTrue(result.detectedSkillTerms().isEmpty());
        assertTrue(result.experiences().isEmpty());
        assertTrue(result.education().isEmpty());
        assertTrue(result.projects().isEmpty());
    }

    private OpenAiCandidateProfileExtractionService service(
            OpenAiCandidateProfileExtractionService.Transport transport) {
        return new OpenAiCandidateProfileExtractionService(properties(), mapper, transport);
    }

    private CandidateExtractionProperties properties() {
        CandidateExtractionProperties properties = new CandidateExtractionProperties();
        properties.setApiKey("test-key");
        properties.setProvider("openai");
        properties.setModel("gpt-4.1-mini");
        return properties;
    }

    private String minimalProfile(String skill, String evidence) {
        return """
                {
                  "total_experience_months":0,
                  "extraction_confidence":"LOW",
                  "skills":[{"term":"%s","evidence":"%s"}],
                  "experiences":[],"education":[],"projects":[]
                }
                """.formatted(skill, evidence);
    }

    private String response(String profile) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode message = root.putArray("output").addObject();
        message.put("type", "message");
        ObjectNode content = message.putArray("content").addObject();
        content.put("type", "output_text");
        content.put("text", profile);
        return mapper.writeValueAsString(root);
    }
}
