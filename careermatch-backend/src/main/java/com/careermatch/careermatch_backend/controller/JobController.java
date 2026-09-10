package com.careermatch.careermatch_backend.controller;
import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.service.JobService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/jobs") @Validated
public class JobController {
    private final JobService service;
    public JobController(JobService service) { this.service = service; }
    @GetMapping public JobListResponse list(@RequestParam(required = false) String query,
            @RequestParam(required = false) String location, @RequestParam(defaultValue = "1") @Min(1) int page) {
        return service.list(query, location, page);
    }
    @GetMapping("/{jobId}") public JobDetailsResponse get(@PathVariable UUID jobId) { return service.get(jobId); }
}
