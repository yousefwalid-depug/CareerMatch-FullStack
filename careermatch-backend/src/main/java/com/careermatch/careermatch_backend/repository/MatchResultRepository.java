package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {}
