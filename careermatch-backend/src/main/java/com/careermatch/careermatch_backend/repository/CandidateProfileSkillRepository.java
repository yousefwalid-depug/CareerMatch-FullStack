package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.CandidateProfileSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CandidateProfileSkillRepository extends JpaRepository<CandidateProfileSkill, UUID> {
    List<CandidateProfileSkill> findByProfile_Id(UUID profileId);
}
