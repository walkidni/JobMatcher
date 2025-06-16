package com.walid.jobmatcher.repository;

import com.walid.jobmatcher.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    List<JobPost> findByRecruiterId(Long recruiterId);
}
