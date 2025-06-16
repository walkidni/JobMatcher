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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;

    public CandidateController(CandidateRepository candidateRepository,
                               ResumeRepository resumeRepository) {
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
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
                    resumeRepository.delete(resume);
                    resumeRepository.flush();
                    return ResponseEntity.ok("Resume deleted successfully.");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Resume not found for candidate ID: " + candidateId));
    }
}