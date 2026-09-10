package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import static com.careermatch.careermatch_backend.entity.DomainEnums.*;

@Entity
@Table(name = "cvs")
public class Cv {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(name = "original_filename", nullable = false) private String originalFilename;
    @Column(name = "file_url") private String fileUrl;
    @Column(name = "raw_text") private String rawText;
    @Enumerated(EnumType.STRING) @Column(name = "text_extraction_status", nullable = false) private CvExtractionStatus textExtractionStatus;
    @Enumerated(EnumType.STRING) @Column(name = "profile_status", nullable = false) private ProfileStatus profileStatus;
    @Column(name = "parsed_at") private Instant parsedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected Cv() {}
    public Cv(UUID id, User user, String originalFilename, String fileUrl, String rawText, Instant now) {
        this.id = id; this.user = user; this.originalFilename = originalFilename; this.fileUrl = fileUrl;
        this.rawText = rawText; this.textExtractionStatus = CvExtractionStatus.EXTRACTED;
        this.profileStatus = ProfileStatus.PENDING; this.parsedAt = now; this.createdAt = now;
    }
    public UUID getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public String getRawText() { return rawText; }
    public ProfileStatus getProfileStatus() { return profileStatus; }
    public void markProfileReady() { this.profileStatus = ProfileStatus.READY; }
}
