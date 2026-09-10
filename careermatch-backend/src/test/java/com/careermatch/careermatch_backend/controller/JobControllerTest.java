package com.careermatch.careermatch_backend.controller;

import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.entity.DomainEnums.JobSource;
import com.careermatch.careermatch_backend.exception.*;
import com.careermatch.careermatch_backend.service.JobService;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JobControllerTest {
    private JobService service; private MockMvc mvc;
    @BeforeEach void setup() {
        service = mock(JobService.class);
        mvc = MockMvcBuilders.standaloneSetup(new JobController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void validListingReturnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.list("java", "Cairo", 1)).thenReturn(new JobListResponse(
                List.of(new JobSummary(id, "Java Developer", "Example", "Cairo", JobSource.LOCAL)), 1, 10, 1, 1));
        mvc.perform(get("/api/jobs").param("query", "java").param("location", "Cairo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.jobs[0].title").value("Java Developer"));
    }
    @Test void unknownJobReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new NotFoundException("Job not found: " + id));
        mvc.perform(get("/api/jobs/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job not found: " + id));
    }

    @Test void serviceFailureReturnsUserFriendly503Response() throws Exception {
        when(service.list(null, null, 1)).thenThrow(
                new ExternalServiceException("Job search is temporarily unavailable. Please try again later."));

        mvc.perform(get("/api/jobs"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message")
                        .value("Job search is temporarily unavailable. Please try again later."))
                .andExpect(jsonPath("$.path").value("/api/jobs"));
    }
}
