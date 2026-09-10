package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.entity.*;
import com.careermatch.careermatch_backend.exception.*;
import com.careermatch.careermatch_backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CandidateProfileService {
    private final CvRepository cvs; private final CandidateProfileRepository profiles;
    private final CandidateProfileSkillRepository skills; private final CandidateExperienceRepository experiences;
    private final CandidateEducationRepository education; private final CandidateProjectRepository projects;
    public CandidateProfileService(CvRepository cvs, CandidateProfileRepository profiles,
            CandidateProfileSkillRepository skills, CandidateExperienceRepository experiences,
            CandidateEducationRepository education, CandidateProjectRepository projects) {
        this.cvs = cvs; this.profiles = profiles; this.skills = skills; this.experiences = experiences;
        this.education = education; this.projects = projects;
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse get(UUID cvId) {
        Cv cv = cvs.findById(cvId).orElseThrow(() -> new NotFoundException("CV not found: " + cvId));
        CandidateProfile profile = profiles.findByCv_Id(cvId).orElseThrow(() ->
                new ApiException(HttpStatus.ACCEPTED, "Candidate profile extraction is still pending"));
        return new CandidateProfileResponse(cv.getId(), profile.getId(), profile.getTotalExperienceMonths(),
                profile.getExtractionConfidence(),
                skills.findByProfile_Id(profile.getId()).stream().map(s -> new SkillEvidence(s.getSkill().getId(),
                        s.getSkill().getCanonicalName(), s.getEvidence())).toList(),
                experiences.findByProfile_Id(profile.getId()).stream().map(e -> new ExperienceItem(e.getId(), e.getJobTitle(),
                        e.getCompany(), e.getStartDate(), e.getEndDate(), e.getDurationMonths(), e.getDescription())).toList(),
                education.findByProfile_Id(profile.getId()).stream().map(e -> new EducationItem(e.getId(), e.getDegree(),
                        e.getFieldOfStudy(), e.getInstitution(), e.getEducationLevel())).toList(),
                projects.findByProfile_Id(profile.getId()).stream().map(p -> new ProjectItem(p.getId(), p.getTitle(), p.getDescription())).toList());
    }
}
