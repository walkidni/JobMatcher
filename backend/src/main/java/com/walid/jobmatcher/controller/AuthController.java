package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.entity.Candidate;
import com.walid.jobmatcher.entity.Recruiter;
import com.walid.jobmatcher.repository.CandidateRepository;
import com.walid.jobmatcher.repository.RecruiterRepository;
import com.walid.jobmatcher.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ===========================
    // Register Candidate
    // ===========================
    @PostMapping("/register-candidate")
    public ResponseEntity<?> registerCandidate(@RequestBody Candidate candidate) {
        if (candidateRepository.findByEmail(candidate.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Candidate already exists");
        }
        candidate.setPassword(passwordEncoder.encode(candidate.getPassword()));
        candidateRepository.save(candidate);
        
        // Generate token after successful registration
        String token = jwtUtil.generateToken(candidate.getEmail(), "CANDIDATE", candidate.getFullName());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // ===========================
    // Register Recruiter
    // ===========================
    @PostMapping("/register-recruiter")
    public ResponseEntity<?> registerRecruiter(@RequestBody Recruiter recruiter) {
        if (recruiterRepository.findByEmail(recruiter.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Recruiter already exists");
        }
        recruiter.setPassword(passwordEncoder.encode(recruiter.getPassword()));
        recruiterRepository.save(recruiter);
        
        // Generate token after successful registration
        String token = jwtUtil.generateToken(recruiter.getEmail(), "RECRUITER", recruiter.getFullName());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // ===========================
    // Login for All Users
    // ===========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Determine user type and generate appropriate token
        String role;
        String name;
        if (candidateRepository.findByEmail(request.email()).isPresent()) {
            role = "CANDIDATE";
            name = candidateRepository.findByEmail(request.email()).get().getFullName();
        } else {
            role = "RECRUITER";
            name = recruiterRepository.findByEmail(request.email()).get().getFullName();
        }
        String token = jwtUtil.generateToken(request.email(), role, name);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // DTOs
    record LoginRequest(String email, String password) {}
    record LoginResponse(String token) {}
}
