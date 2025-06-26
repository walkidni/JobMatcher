package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.entity.JobPost;
import com.walid.jobmatcher.entity.Recruiter;
import com.walid.jobmatcher.repository.JobPostRepository;
import com.walid.jobmatcher.repository.RecruiterRepository;
import com.walid.jobmatcher.repository.JobApplicationRepository;
import com.walid.jobmatcher.entity.JobApplication;
import com.walid.jobmatcher.entity.Candidate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job-posts")
@RequiredArgsConstructor
public class JobPostController {

    private static final Logger logger = LoggerFactory.getLogger(JobPostController.class);
    private final JobPostRepository jobPostRepository;
    private final RecruiterRepository recruiterRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @PostMapping
    public ResponseEntity<?> createJobPost(
            @RequestBody JobPost jobPost,
            Authentication authentication) {
        logger.info("Received request to create job post: {} by user: {}", jobPost, authentication != null ? authentication.getName() : "null");
        try {
            Recruiter recruiter = recruiterRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Recruiter not found"));

            jobPost.setRecruiter(recruiter);
            JobPost savedJobPost = jobPostRepository.save(jobPost);
            logger.info("Job post created successfully: {}", savedJobPost);
            return ResponseEntity.ok(savedJobPost);
        } catch (Exception e) {
            logger.error("Error creating job post: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("An error occurred while creating the job post: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<JobPost>> getAllJobPosts() {
        return ResponseEntity.ok(jobPostRepository.findAll());
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<JobPost>> getJobPostsByRecruiter(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(jobPostRepository.findByRecruiterId(recruiterId));
    }

    @GetMapping("/{jobPostId}/applications")
    public ResponseEntity<List<Map<String, Object>>> getApplicationsForJob(@PathVariable Long jobPostId) {
        List<JobApplication> applications = jobApplicationRepository.findByJobPostId(jobPostId);
        List<Map<String, Object>> result = applications.stream().map(app -> {
            Candidate c = app.getCandidate();
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("applicationId", app.getId());
            map.put("candidateId", c.getId());
            map.put("candidateName", c.getFullName());
            map.put("candidateEmail", c.getEmail());
            map.put("status", app.getStatus());
            map.put("appliedAt", app.getAppliedAt());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long applicationId, @RequestBody Map<String, String> body, Authentication authentication) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        JobPost jobPost = application.getJobPost();
        Recruiter recruiter = recruiterRepository.findByEmail(authentication.getName())
                .orElse(null);
        if (recruiter == null || !jobPost.getRecruiter().getId().equals(recruiter.getId())) {
            return ResponseEntity.status(403).body("Only the recruiter who owns this job post can update application status");
        }
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body("Missing status");
        }
        try {
            JobApplication.Status newStatus = JobApplication.Status.valueOf(statusStr);
            application.setStatus(newStatus);
            jobApplicationRepository.save(application);
            return ResponseEntity.ok("Application status updated");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }
}
