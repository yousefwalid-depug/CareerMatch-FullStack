package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.ai.CandidateProfileExtractionService;
import com.careermatch.careermatch_backend.ai.CandidateProfileExtractionService.*;
import com.careermatch.careermatch_backend.entity.*;
import com.careermatch.careermatch_backend.exception.InvalidCvException;
import com.careermatch.careermatch_backend.repository.*;
import org.junit.jupiter.api.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.mock.web.MockMultipartFile;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.careermatch.careermatch_backend.entity.DomainEnums.Confidence.HIGH;

class CvServiceTest {
    @Test void rejectsNonPdfUpload() {
        CvService service = new CvService("target/test-uploads", mock(UserRepository.class), mock(CvRepository.class),
                mock(CandidateProfileRepository.class), mock(SkillRepository.class), mock(SkillAliasRepository.class),
                mock(CandidateProfileSkillRepository.class), mock(CandidateExperienceRepository.class),
                mock(CandidateEducationRepository.class), mock(CandidateProjectRepository.class),
                mock(CandidateProjectSkillRepository.class), mock(CandidateProfileExtractionService.class));
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "hello".getBytes());
        InvalidCvException error = assertThrows(InvalidCvException.class, () -> service.upload(file));
        assertEquals("Only PDF uploads are supported", error.getMessage());
    }

    @Test void persistsRichExtractedProfile() throws Exception {
        Path uploadDirectory = Path.of("target", "test-uploads-rich-" + UUID.randomUUID());
        UserRepository users = mock(UserRepository.class);
        CvRepository cvs = mock(CvRepository.class);
        CandidateProfileRepository profiles = mock(CandidateProfileRepository.class);
        SkillRepository skills = mock(SkillRepository.class);
        SkillAliasRepository aliases = mock(SkillAliasRepository.class);
        CandidateProfileSkillRepository profileSkills = mock(CandidateProfileSkillRepository.class);
        CandidateExperienceRepository experiences = mock(CandidateExperienceRepository.class);
        CandidateEducationRepository education = mock(CandidateEducationRepository.class);
        CandidateProjectRepository projects = mock(CandidateProjectRepository.class);
        CandidateProjectSkillRepository projectSkills = mock(CandidateProjectSkillRepository.class);
        CandidateProfileExtractionService extractor = mock(CandidateProfileExtractionService.class);
        User demo = new User(CvService.DEMO_USER_ID, "demo@example.com", Instant.now());
        Skill java = mock(Skill.class);
        when(java.getId()).thenReturn(UUID.randomUUID());
        when(java.getCanonicalName()).thenReturn("Java");
        when(users.findById(CvService.DEMO_USER_ID)).thenReturn(Optional.of(demo));
        when(skills.findAllByOrderByCanonicalNameAsc()).thenReturn(List.of(java));
        when(aliases.findAll()).thenReturn(List.of());
        when(cvs.save(any(Cv.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.save(any(CandidateProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projects.save(any(CandidateProject.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(extractor.extract(anyString(), anyCollection())).thenReturn(new ExtractedProfile(6, HIGH,
                Set.of("Java"), Map.of("Java", "Java"),
                List.of(new ExtractedExperience("Backend Intern", "Nile Labs", LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 6, 30), 6, "Backend Intern at Nile Labs")),
                List.of(new ExtractedEducation("BSc", "Computer Science", "Cairo University", "BACHELORS",
                        "BSc Computer Science, Cairo University")),
                List.of(new ExtractedProject("Inventory Service", "Inventory Service built with Java", Set.of("Java")))));
        CvService service = new CvService(uploadDirectory.toString(), users, cvs, profiles, skills, aliases,
                profileSkills, experiences, education, projects, projectSkills, extractor);

        try {
            var response = service.upload(new MockMultipartFile("file", "synthetic.pdf", "application/pdf", pdfBytes()));

            assertTrue(response.parsed());
            verify(profileSkills).save(any(CandidateProfileSkill.class));
            verify(experiences).save(any(CandidateExperience.class));
            verify(education).save(any(CandidateEducation.class));
            verify(projects).save(any(CandidateProject.class));
            verify(projectSkills).save(any(CandidateProjectSkill.class));
        } finally {
            if (Files.exists(uploadDirectory)) {
                try (var files = Files.list(uploadDirectory)) {
                    files.forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                    });
                }
                Files.deleteIfExists(uploadDirectory);
            }
        }
    }

    private byte[] pdfBytes() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.showText("Synthetic Java CV with education, internship experience, and Inventory Service project.");
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
