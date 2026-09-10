package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {
    Optional<CandidateProfile> findByCv_Id(UUID cvId);
}
