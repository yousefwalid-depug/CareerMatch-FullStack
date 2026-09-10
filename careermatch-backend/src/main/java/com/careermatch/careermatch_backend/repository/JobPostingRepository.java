package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.JobPosting;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {
    @Query("select j from JobPosting j where (:query = '' or lower(j.title) like lower(concat('%', :query, '%')) or lower(j.description) like lower(concat('%', :query, '%'))) and (:location = '' or lower(j.location) like lower(concat('%', :location, '%'))) order by j.createdAt desc")
    Page<JobPosting> search(@Param("query") String query, @Param("location") String location, Pageable pageable);
}
