package com.careermatch.careermatch_backend.service;
import com.careermatch.careermatch_backend.dto.ApiDtos.MatchResponse;
import java.util.UUID;
public interface MatchAnalysisService { MatchResponse analyze(UUID cvId, UUID jobId); }
