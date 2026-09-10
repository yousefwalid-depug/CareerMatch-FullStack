package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface CvRepository extends JpaRepository<Cv, UUID> {}
