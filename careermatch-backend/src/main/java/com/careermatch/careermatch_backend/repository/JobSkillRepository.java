package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface JobSkillRepository extends JpaRepository<JobSkill, UUID> {
    List<JobSkill> findByJob_IdOrderByRequirementTypeAscImportanceWeightDesc(UUID jobId);
}
