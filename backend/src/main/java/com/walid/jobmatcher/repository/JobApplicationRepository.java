package com.walid.jobmatcher.repository;

import com.walid.jobmatcher.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Optional<JobApplication> findByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);
    List<JobApplication> findByCandidateId(Long candidateId);
    List<JobApplication> findByJobPostId(Long jobPostId);
} 