package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.CandidateProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CandidateProjectSkillRepository extends JpaRepository<CandidateProjectSkill, UUID> {
    List<CandidateProjectSkill> findByProject_Profile_Id(UUID profileId);
}
