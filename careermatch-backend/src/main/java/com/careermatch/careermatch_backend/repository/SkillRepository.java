package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    List<Skill> findAllByOrderByCanonicalNameAsc();
}
