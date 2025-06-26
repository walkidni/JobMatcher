package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.entity.Candidate;
import com.walid.jobmatcher.entity.Resume;
import com.walid.jobmatcher.repository.CandidateRepository;
import com.walid.jobmatcher.repository.ResumeRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import com.walid.jobmatcher.entity.JobApplication;
import com.walid.jobmatcher.repository.JobApplicationRepository;
import com.walid.jobmatcher.entity.JobPost;
import com.walid.jobmatcher.repository.JobPostRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostRepository jobPostRepository;
    private final Cloudinary cloudinary;

    public CandidateController(CandidateRepository candidateRepository,
                               ResumeRepository resumeRepository,
                               JobApplicationRepository jobApplicationRepository,
                               JobPostRepository jobPostRepository,
                               Cloudinary cloudinary) {
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobPostRepository = jobPostRepository;
        this.cloudinary = cloudinary;
    }


    @PostMapping("/{candidateId}/upload-resume")
    public ResponseEntity<?> uploadResume(@PathVariable Long candidateId,
                                          @RequestParam("file") MultipartFile file) {
        logger.info("Received resume upload request for candidateId: {}. File name: {}", candidateId, file.getOriginalFilename());
        try {
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

            // Delete old resume if it exists
            if (candidate != null) {
                candidate.setResume(null); // Break the association
                candidateRepository.save(candidate);
            }
            resumeRepository.findByCandidateId(candidateId).ifPresent(resumeRepository::delete);
            resumeRepository.flush();

            // Save file to cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "auto",
                "type", "upload"));
            String url = (String) uploadResult.get("secure_url");
            logger.info("Resume uploaded to Cloudinary for candidateId: {}. Cloudinary URL: {}", candidateId, url);

            InputStreamResource resource = new InputStreamResource(file.getInputStream());
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> docs = reader.get();

            StringBuilder sb = new StringBuilder();
            for (Document doc : docs) {
                if (doc.isText() && doc.getText() != null) {
                    sb.append(doc.getText()).append("\n");
                }
            }
            Resume resume = new Resume();
            resume.setOriginalFileName(file.getOriginalFilename());
            resume.setExtractedText(sb.toString());
            resume.setCandidate(candidate);
            resume.setFilePath(url);
            resumeRepository.save(resume);

            logger.info("Resume entity saved for candidateId: {}", candidateId);
            return ResponseEntity.ok("Resume uploaded to Cloudinary and parsed successfully via Spring AI.");
        } catch (Exception e) {
            logger.error("Error uploading resume for candidateId: {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/{candidateId}/resume-text")
    public ResponseEntity<?> getResumeText(@PathVariable Long candidateId) {
        return resumeRepository.findByCandidateId(candidateId)
                .map(resume -> ResponseEntity.ok(resume.getExtractedText()))
                .orElseGet(() -> ResponseEntity.status(404).body("Resume not found for candidate ID: " + candidateId));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentCandidate(Authentication authentication) {
        String email = authentication.getName();
        System.out.println("email: " + email);
        Candidate candidate = candidateRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Candidate not found"));
        return ResponseEntity.ok(Map.of(
            "id", candidate.getId(),
            "email", candidate.getEmail(),
            "name", candidate.getFullName()
        ));
    }

    @GetMapping("/{candidateId}/resume")
    public ResponseEntity<?> getResume(@PathVariable Long candidateId) {
        return resumeRepository.findByCandidateId(candidateId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Resume not found for candidate ID: " + candidateId));
    }

    @DeleteMapping("/{candidateId}/resume")
    public ResponseEntity<?> deleteResume(@PathVariable Long candidateId) {
        logger.info("Received request to delete resume for candidateId: {}", candidateId);
        return resumeRepository.findByCandidateId(candidateId)
                .map(resume -> {
                    Candidate candidate = resume.getCandidate();
                    if (candidate != null) {
                        candidate.setResume(null); // Break the association
                        candidateRepository.save(candidate);
                    }
                    try {
                        cloudinary.uploader().destroy(resume.getFilePath(), ObjectUtils.emptyMap());
                    } catch (Exception e) {
                        logger.error("Error deleting resume file from Cloudinary for candidateId: {}: {}", candidateId, e.getMessage(), e);
                    }
                    resumeRepository.delete(resume);
                    resumeRepository.flush();
                    logger.info("Resume deleted for candidateId: {}", candidateId);
                    return ResponseEntity.ok("Resume deleted successfully.");
                })
                .orElseGet(() -> {
                    logger.warn("Resume not found for candidateId: {}", candidateId);
                    return ResponseEntity.status(404).body("Resume not found for candidate ID: " + candidateId);
                });
    }

    @GetMapping("/{candidateId}/resume/file")
    public ResponseEntity<?> getResumeFile(@PathVariable Long candidateId) {
        logger.info("Received request to get resume file for candidateId: {}", candidateId);
        return resumeRepository.findByCandidateId(candidateId)
                .map(resume -> {
                    String fileUrl = resume.getFilePath();
                    if (fileUrl == null || fileUrl.isEmpty()) {
                        logger.warn("Resume file URL not found for candidateId: {}", candidateId);
                        return ResponseEntity.status(404).body("Resume file not found for candidate ID: " + candidateId);
                    }
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        org.springframework.http.ResponseEntity<byte[]> response = restTemplate.getForEntity(fileUrl, byte[].class);
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(response.getHeaders().getContentType());
                        headers.setContentLength(response.getBody() != null ? response.getBody().length : 0);
                        headers.set("Content-Disposition", "inline; filename=\"" + resume.getOriginalFileName() + "\"");
                        logger.info("Serving resume file for candidateId: {} from Cloudinary", candidateId);
                        return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
                    } catch (Exception e) {
                        logger.error("Error fetching resume file from Cloudinary for candidateId: {}: {}", candidateId, e.getMessage(), e);
                        return ResponseEntity.internalServerError().body("Error fetching resume file: " + e.getMessage());
                    }
                })
                .orElseGet(() -> {
                    logger.warn("Resume not found in DB for candidateId: {}", candidateId);
                    return ResponseEntity.status(404).body("Resume not found for candidate ID: " + candidateId);
                });
    }

    @PostMapping("/{candidateId}/apply/{jobPostId}")
    public ResponseEntity<?> applyToJob(@PathVariable Long candidateId, @PathVariable Long jobPostId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("Job post not found"));
        if (jobApplicationRepository.findByCandidateIdAndJobPostId(candidateId, jobPostId).isPresent()) {
            return ResponseEntity.badRequest().body("Already applied to this job");
        }
        JobApplication application = new JobApplication();
        application.setCandidate(candidate);
        application.setJobPost(jobPost);
        application.setStatus(JobApplication.Status.APPLIED);
        application.setAppliedAt(java.time.LocalDateTime.now());
        jobApplicationRepository.save(application);
        return ResponseEntity.ok("Application submitted successfully");
    }

    @GetMapping("/{candidateId}/applications")
    public ResponseEntity<List<Map<String, Object>>> getApplicationsForCandidate(@PathVariable Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
        List<JobApplication> applications = jobApplicationRepository.findByCandidateId(candidateId);
        List<Map<String, Object>> result = applications.stream().map(app -> {
            JobPost job = app.getJobPost();
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("applicationId", app.getId());
            map.put("jobPostId", job.getId());
            map.put("jobTitle", job.getTitle());
            map.put("jobDescription", job.getDescription());
            map.put("status", app.getStatus());
            map.put("appliedAt", app.getAppliedAt());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }
}