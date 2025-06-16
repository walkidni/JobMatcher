package com.walid.jobmatcher.repository;

import com.walid.jobmatcher.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    Optional<Recruiter> findByEmail(String email);

}
