package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.MatchSkillResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MatchSkillResultRepository extends JpaRepository<MatchSkillResult, UUID> {
    List<MatchSkillResult> findByMatch_Id(UUID matchId);
}
