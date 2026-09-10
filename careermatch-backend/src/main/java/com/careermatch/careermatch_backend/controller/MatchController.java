package com.careermatch.careermatch_backend.controller;
import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/match")
public class MatchController {
    private final MatchAnalysisService analysis; private final MatchQueryService query;
    public MatchController(MatchAnalysisService analysis, MatchQueryService query) { this.analysis = analysis; this.query = query; }
    @PostMapping("/analyze") public MatchResponse analyze(@Valid @RequestBody MatchAnalyzeRequest request) {
        return analysis.analyze(request.cvId(), request.jobId());
    }
    @GetMapping("/{matchId}") public MatchResponse get(@PathVariable UUID matchId) { return query.get(matchId); }
}
