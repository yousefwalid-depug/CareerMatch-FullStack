package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.entity.*;
import com.careermatch.careermatch_backend.exception.NotFoundException;
import com.careermatch.careermatch_backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.careermatch.careermatch_backend.entity.DomainEnums.RequirementType.*;

@Service
public class JobService {
    private static final int PAGE_SIZE = 10;
    private final JobProvider provider;
    private final JobPostingRepository jobs;
    private final JobSkillRepository jobSkills;
    public JobService(JobProvider provider, JobPostingRepository jobs, JobSkillRepository jobSkills) {
        this.provider = provider; this.jobs = jobs; this.jobSkills = jobSkills;
    }

    @Transactional(readOnly = true)
    public JobListResponse list(String query, String location, int page) {
        Page<JobPosting> result = provider.search(query, location, page, PAGE_SIZE);
        List<JobSummary> items = result.stream().map(j -> new JobSummary(j.getId(), j.getTitle(), j.getCompany(), j.getLocation(), j.getSource())).toList();
        return new JobListResponse(items, page, PAGE_SIZE, result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public JobDetailsResponse get(UUID id) {
        JobPosting job = jobs.findById(id).orElseThrow(() -> new NotFoundException("Job not found: " + id));
        List<JobSkill> requirements = jobSkills.findByJob_IdOrderByRequirementTypeAscImportanceWeightDesc(id);
        List<String> required = requirements.stream().filter(s -> s.getRequirementType() == REQUIRED).map(s -> s.getSkill().getCanonicalName()).toList();
        List<String> preferred = requirements.stream().filter(s -> s.getRequirementType() == PREFERRED).map(s -> s.getSkill().getCanonicalName()).toList();
        return new JobDetailsResponse(job.getId(), job.getTitle(), job.getCompany(), job.getDescription(), required, preferred);
    }
}
