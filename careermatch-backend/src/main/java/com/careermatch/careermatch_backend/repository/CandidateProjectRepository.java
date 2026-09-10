package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.CandidateProject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CandidateProjectRepository extends JpaRepository<CandidateProject, UUID> {
    List<CandidateProject> findByProfile_Id(UUID profileId);
    long countByProfile_Id(UUID profileId);
}
