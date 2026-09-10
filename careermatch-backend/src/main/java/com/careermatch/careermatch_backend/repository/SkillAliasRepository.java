package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.SkillAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SkillAliasRepository extends JpaRepository<SkillAlias, UUID> {}
