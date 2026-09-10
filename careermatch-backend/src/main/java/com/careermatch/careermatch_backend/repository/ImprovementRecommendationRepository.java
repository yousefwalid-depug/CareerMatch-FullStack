package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.ImprovementRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ImprovementRecommendationRepository extends JpaRepository<ImprovementRecommendation, UUID> {
    List<ImprovementRecommendation> findByMatch_IdOrderByPriorityScoreDesc(UUID matchId);
}
