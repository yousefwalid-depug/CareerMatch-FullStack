package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.entity.JobPosting;
import com.careermatch.careermatch_backend.repository.JobPostingRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class LocalJobProvider implements JobProvider {
    private final JobPostingRepository jobs;
    public LocalJobProvider(JobPostingRepository jobs) { this.jobs = jobs; }
    @Override public Page<JobPosting> search(String query, String location, int page, int size) {
        return jobs.search(query == null ? "" : query.trim(), location == null ? "" : location.trim(),
                PageRequest.of(page - 1, size));
    }
}
