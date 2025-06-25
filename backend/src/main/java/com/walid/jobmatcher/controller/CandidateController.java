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

    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostRepository jobPostRepository;

    public CandidateController(CandidateRepository candidateRepository,
                               ResumeRepository resumeRepository,
                               JobApplicationRepository jobApplicationRepository,
                               JobPostRepository jobPostRepository) {
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobPostRepository = jobPostRepository;
    }


    @PostMapping("/{candidateId}/upload-resume")
    public ResponseEntity<?> uploadResume(@PathVariable Long candidateId,
                                          @RequestParam("file") MultipartFile file) {
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

            // Save file to disk
            String uploadDir = "uploads/resumes/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, uniqueFileName);
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(file.getBytes());
            }

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
            resume.setFilePath(filePath.toString());
            resumeRepository.save(resume);

            return ResponseEntity.ok("Resume uploaded and parsed successfully via Spring AI.");
        } catch (Exception e) {
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
        return resumeRepository.findByCandidateId(candidateId)
                .map(resume -> {
                    Candidate candidate = resume.getCandidate();
                    if (candidate != null) {
                        candidate.setResume(null); // Break the association
                        candidateRepository.save(candidate);
                    }
                    // Delete the file from the filesystem
                    String filePath = resume.getFilePath();
                    if (filePath != null && !filePath.isEmpty()) {
                        Path path = Paths.get(filePath);
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Log the error, but continue to delete the DB record
                            System.err.println("Failed to delete resume file: " + e.getMessage());
                        }
                    }
                    resumeRepository.delete(resume);
                    resumeRepository.flush();
                    return ResponseEntity.ok("Resume deleted successfully.");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Resume not found for candidate ID: " + candidateId));
    }

    @GetMapping("/{candidateId}/resume/file")
    public ResponseEntity<?> getResumeFile(@PathVariable Long candidateId) {
        return resumeRepository.findByCandidateId(candidateId)
                .map(resume -> {
                    String filePath = resume.getFilePath();
                    if (filePath == null || filePath.isEmpty()) {
                        return ResponseEntity.status(404).body("Resume file not found for candidate ID: " + candidateId);
                    }
                    Path path = Paths.get(filePath);
                    if (!Files.exists(path)) {
                        return ResponseEntity.status(404).body("Resume file not found for candidate ID: " + candidateId);
                    }
                    try {
                        byte[] fileBytes = Files.readAllBytes(path);
                        return ResponseEntity.ok()
                                .header("Content-Disposition", "inline; filename=\"" + resume.getOriginalFileName() + "\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(fileBytes);
                    } catch (IOException e) {
                        return ResponseEntity.internalServerError().body("Error reading resume file: " + e.getMessage());
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Resume not found for candidate ID: " + candidateId));
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