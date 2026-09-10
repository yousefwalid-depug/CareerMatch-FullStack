package com.careermatch.careermatch_backend.service;

import com.careermatch.careermatch_backend.entity.JobPosting;
import org.springframework.data.domain.Page;

public interface JobProvider {
    Page<JobPosting> search(String query, String location, int page, int size);
}
