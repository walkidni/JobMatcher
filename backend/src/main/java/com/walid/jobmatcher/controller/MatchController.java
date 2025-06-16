package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.dto.CandidateMatchDTO;
import com.walid.jobmatcher.entity.JobPost;
import com.walid.jobmatcher.entity.Resume;
import com.walid.jobmatcher.repository.JobPostRepository;
import com.walid.jobmatcher.repository.ResumeRepository;
import com.walid.jobmatcher.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final JobPostRepository jobPostRepository;
    private final ResumeRepository resumeRepository;
    private final MatchingService matchingService;

    @GetMapping("/job/{jobPostId}/candidates")
    public ResponseEntity<List<Map<String, Object>>> matchCandidates(@PathVariable Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("JobPost not found"));

        List<Resume> allResumes = resumeRepository.findAll();

        List<Map<String, Object>> matches = new ArrayList<>();

        for (Resume resume : allResumes) {
            int score = matchingService.calculateMatchScore(jobPost.getRequiredSkills(), resume.getExtractedText());
            if (score > 0) {
                Map<String, Object> candidateMap = new HashMap<>();
                candidateMap.put("id", resume.getCandidate().getId());
                candidateMap.put("email", resume.getCandidate().getEmail());
                candidateMap.put("name", resume.getCandidate().getFullName());
                candidateMap.put("resumeText", resume.getExtractedText());
                candidateMap.put("matchScore", score);
                matches.add(candidateMap);
            }
        }

        matches.sort((a, b) -> Integer.compare((int) b.get("matchScore"), (int) a.get("matchScore")));

        return ResponseEntity.ok(matches);
    }

    @GetMapping("/candidate/{candidateId}/jobs")
    public ResponseEntity<?> matchJobsForCandidate(@PathVariable Long candidateId) {
        Resume resume = resumeRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Resume not found for candidate"));

        String resumeText = resume.getExtractedText();
        List<JobPost> allJobs = jobPostRepository.findAll();

        List<Map<String, Object>> matches = new ArrayList<>();
        for (JobPost job : allJobs) {
            int score = matchingService.calculateMatchScore(job.getRequiredSkills(), resumeText);
            if (score > 0) {
                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("id", job.getId());
                jobMap.put("title", job.getTitle());
                jobMap.put("description", job.getDescription());
                jobMap.put("requiredSkills", job.getRequiredSkills());
                jobMap.put("matchScore", score);
                matches.add(jobMap);
            }
        }
        // Sort by matchScore descending
        matches.sort((a, b) -> Integer.compare((int) b.get("matchScore"), (int) a.get("matchScore")));

        return ResponseEntity.ok(matches);
    }
}
