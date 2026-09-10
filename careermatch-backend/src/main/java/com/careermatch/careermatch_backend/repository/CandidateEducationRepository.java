package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.CandidateEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CandidateEducationRepository extends JpaRepository<CandidateEducation, UUID> {
    List<CandidateEducation> findByProfile_Id(UUID profileId);
}
