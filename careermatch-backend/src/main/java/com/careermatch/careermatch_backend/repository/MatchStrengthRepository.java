package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.MatchStrength;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MatchStrengthRepository extends JpaRepository<MatchStrength, UUID> {
    List<MatchStrength> findByMatch_Id(UUID matchId);
}
