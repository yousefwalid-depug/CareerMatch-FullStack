package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.ai.CandidateProfileExtractionService;
import com.careermatch.careermatch_backend.dto.ApiDtos.CvUploadResponse;
import com.careermatch.careermatch_backend.entity.*;
import com.careermatch.careermatch_backend.exception.*;
import com.careermatch.careermatch_backend.repository.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@Service
public class CvService {
    public static final UUID DEMO_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private final Path uploadDirectory;
    private final UserRepository users;
    private final CvRepository cvs;
    private final CandidateProfileRepository profiles;
    private final SkillRepository skills;
    private final SkillAliasRepository aliases;
    private final CandidateProfileSkillRepository profileSkills;
    private final CandidateExperienceRepository experiences;
    private final CandidateEducationRepository education;
    private final CandidateProjectRepository projects;
    private final CandidateProjectSkillRepository projectSkills;
    private final CandidateProfileExtractionService extractor;

    public CvService(@Value("${careermatch.upload-directory}") String uploadDirectory, UserRepository users,
                     CvRepository cvs, CandidateProfileRepository profiles, SkillRepository skills,
                     SkillAliasRepository aliases, CandidateProfileSkillRepository profileSkills,
                     CandidateExperienceRepository experiences, CandidateEducationRepository education,
                     CandidateProjectRepository projects, CandidateProjectSkillRepository projectSkills,
                     CandidateProfileExtractionService extractor) {
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.users = users; this.cvs = cvs; this.profiles = profiles; this.skills = skills;
        this.aliases = aliases; this.profileSkills = profileSkills; this.experiences = experiences;
        this.education = education; this.projects = projects; this.projectSkills = projectSkills;
        this.extractor = extractor;
    }

    @Transactional
    public CvUploadResponse upload(MultipartFile file) {
        validate(file);
        byte[] bytes;
        try { bytes = file.getBytes(); } catch (IOException ex) { throw new InvalidCvException("The uploaded PDF could not be read"); }
        String text = extractText(bytes);
        if (text.replaceAll("\\s+", "").length() < 40) throw new InvalidCvException("The PDF contains too little readable text");

        User demo = users.findById(DEMO_USER_ID).orElseThrow(() -> new IllegalStateException("Demo user seed is missing"));
        UUID cvId = UUID.randomUUID();
        Path stored = store(cvId, bytes);
        Instant now = Instant.now();
        Cv cv = cvs.save(new Cv(cvId, demo, file.getOriginalFilename(), stored.toString(), text, now));

        Map<String, Skill> terms = new LinkedHashMap<>();
        skills.findAllByOrderByCanonicalNameAsc().forEach(skill -> terms.put(skill.getCanonicalName(), skill));
        aliases.findAll().forEach(alias -> terms.put(alias.getAlias(), alias.getSkill()));
        var extracted = extractor.extract(text, terms.keySet());
        CandidateProfile profile = profiles.save(new CandidateProfile(UUID.randomUUID(), cv,
                extracted.totalExperienceMonths(), extracted.confidence(), now));
        Set<UUID> seen = new HashSet<>();
        extracted.detectedSkillTerms().forEach(term -> {
            Skill skill = resolveSkill(terms, term);
            if (skill != null && seen.add(skill.getId()))
                profileSkills.save(new CandidateProfileSkill(UUID.randomUUID(), profile, skill,
                        extracted.evidenceFor(term)));
        });
        extracted.experiences().forEach(item -> experiences.save(new CandidateExperience(UUID.randomUUID(), profile,
                item.jobTitle(), item.company(), item.startDate(), item.endDate(), item.durationMonths(), item.evidence())));
        extracted.education().forEach(item -> education.save(new CandidateEducation(UUID.randomUUID(), profile,
                item.degree(), item.fieldOfStudy(), item.institution(), item.educationLevel())));
        extracted.projects().forEach(item -> {
            CandidateProject project = projects.save(new CandidateProject(UUID.randomUUID(), profile,
                    item.title(), item.evidence()));
            Set<UUID> projectSkillIds = new HashSet<>();
            item.skillTerms().forEach(term -> {
                Skill skill = resolveSkill(terms, term);
                if (skill != null && projectSkillIds.add(skill.getId()))
                    projectSkills.save(new CandidateProjectSkill(UUID.randomUUID(), project, skill, item.evidence()));
            });
        });
        cv.markProfileReady();
        cvs.save(cv);
        return new CvUploadResponse(cv.getId(), cv.getOriginalFilename(), true);
    }

    private Skill resolveSkill(Map<String, Skill> terms, String extractedTerm) {
        Skill exact = terms.get(extractedTerm);
        if (exact != null) return exact;
        return terms.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(extractedTerm))
                .map(Map.Entry::getValue).findFirst().orElse(null);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new InvalidCvException("A non-empty PDF file is required");
        if (file.getSize() > MAX_SIZE) throw new InvalidCvException("PDF must not exceed 5 MB");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (!name.endsWith(".pdf") || !"application/pdf".equalsIgnoreCase(file.getContentType()))
            throw new InvalidCvException("Only PDF uploads are supported");
    }

    private String extractText(byte[] bytes) {
        try (var document = Loader.loadPDF(bytes)) { return new PDFTextStripper().getText(document).trim(); }
        catch (IOException ex) { throw new InvalidCvException("The uploaded file is not a readable PDF"); }
    }

    private Path store(UUID id, byte[] bytes) {
        try {
            Files.createDirectories(uploadDirectory);
            Path destination = uploadDirectory.resolve(id + ".pdf").normalize();
            if (!destination.startsWith(uploadDirectory)) throw new InvalidCvException("Invalid upload path");
            return Files.write(destination, bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) { throw new ApiException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "The PDF could not be stored"); }
    }
}
