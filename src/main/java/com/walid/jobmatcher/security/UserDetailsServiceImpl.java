package com.walid.jobmatcher.security;

import com.walid.jobmatcher.entity.Candidate;
import com.walid.jobmatcher.entity.Recruiter;
import com.walid.jobmatcher.repository.CandidateRepository;
import com.walid.jobmatcher.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("=== Loading User Details ===");
        logger.info("Attempting to load user with email: {}", email);

        // Check if it's a candidate
        logger.info("Checking candidate repository for email: {}", email);
        Candidate candidate = candidateRepository.findByEmail(email).orElse(null);
        if (candidate != null) {
            logger.info("Found candidate with email: {}", email);
            UserDetails userDetails = User.withUsername(candidate.getEmail())
                    .password(candidate.getPassword())
                    .roles("CANDIDATE")
                    .build();
            logger.info("Created UserDetails for candidate: {}", userDetails);
            logger.info("User authorities: {}", userDetails.getAuthorities());
            return userDetails;
        }

        // Check if it's a recruiter
        logger.info("Checking recruiter repository for email: {}", email);
        Recruiter recruiter = recruiterRepository.findByEmail(email).orElse(null);
        if (recruiter != null) {
            logger.info("Found recruiter with email: {}", email);
            UserDetails userDetails = User.withUsername(recruiter.getEmail())
                    .password(recruiter.getPassword())
                    .roles("RECRUITER")
                    .build();
            logger.info("Created UserDetails for recruiter: {}", userDetails);
            logger.info("User authorities: {}", userDetails.getAuthorities());
            return userDetails;
        }

        logger.error("User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}

