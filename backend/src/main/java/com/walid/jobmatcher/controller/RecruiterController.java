package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.entity.Recruiter;
import com.walid.jobmatcher.repository.RecruiterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterController {
    private static final Logger logger = LoggerFactory.getLogger(RecruiterController.class);

    private final RecruiterRepository recruiterRepository;

    public RecruiterController(RecruiterRepository recruiterRepository) {
        this.recruiterRepository = recruiterRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentRecruiter(Authentication authentication) {
        logger.info("Getting current recruiter for user: {}", authentication.getName());
        
        String email = authentication.getName();
        Recruiter recruiter = recruiterRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.error("Recruiter not found for email: {}", email);
                return new RuntimeException("Recruiter not found");
            });

        logger.info("Found recruiter: {}", recruiter.getFullName());
        
        return ResponseEntity.ok(Map.of(
            "id", recruiter.getId(),
            "email", recruiter.getEmail(),
            "fullName", recruiter.getFullName()
            ));
    }
} 