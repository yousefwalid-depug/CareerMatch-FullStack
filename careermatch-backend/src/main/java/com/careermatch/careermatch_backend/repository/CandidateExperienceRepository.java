package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.CandidateExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CandidateExperienceRepository extends JpaRepository<CandidateExperience, UUID> {
    List<CandidateExperience> findByProfile_Id(UUID profileId);
}
