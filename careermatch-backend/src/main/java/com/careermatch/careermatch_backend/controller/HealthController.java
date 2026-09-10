package com.careermatch.careermatch_backend.controller;
import com.careermatch.careermatch_backend.dto.ApiDtos.HealthResponse;
import org.springframework.web.bind.annotation.*;
@RestController
public class HealthController {
    @GetMapping("/health") public HealthResponse health() { return new HealthResponse("ok"); }
}
