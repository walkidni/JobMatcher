package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.entity.JobPost;
import com.walid.jobmatcher.entity.Recruiter;
import com.walid.jobmatcher.repository.JobPostRepository;
import com.walid.jobmatcher.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-posts")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostRepository jobPostRepository;
    private final RecruiterRepository recruiterRepository;

    @PostMapping
    public ResponseEntity<?> createJobPost(
            @RequestBody JobPost jobPost,
            Authentication authentication) {
        Recruiter recruiter = recruiterRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Recruiter not found"));

        jobPost.setRecruiter(recruiter);
        JobPost savedJobPost = jobPostRepository.save(jobPost);
        return ResponseEntity.ok(savedJobPost);
    }

    @GetMapping
    public ResponseEntity<List<JobPost>> getAllJobPosts() {
        return ResponseEntity.ok(jobPostRepository.findAll());
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<JobPost>> getJobPostsByRecruiter(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(jobPostRepository.findByRecruiterId(recruiterId));
    }
}
