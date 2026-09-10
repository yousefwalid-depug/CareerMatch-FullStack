package com.careermatch.careermatch_backend.controller;
import com.careermatch.careermatch_backend.dto.ApiDtos.*;
import com.careermatch.careermatch_backend.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
@RestController @RequestMapping("/api/cv")
public class CvController {
    private final CvService cvs; private final CandidateProfileService profiles;
    public CvController(CvService cvs, CandidateProfileService profiles) { this.cvs = cvs; this.profiles = profiles; }
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cvs.upload(file));
    }
    @GetMapping("/{cvId}/profile") public CandidateProfileResponse profile(@PathVariable UUID cvId) { return profiles.get(cvId); }
}
