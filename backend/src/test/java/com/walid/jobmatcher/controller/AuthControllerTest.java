package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.entity.Candidate;
import com.walid.jobmatcher.entity.Recruiter;
import com.walid.jobmatcher.repository.CandidateRepository;
import com.walid.jobmatcher.repository.RecruiterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        candidateRepository.deleteAll();
        recruiterRepository.deleteAll();
    }

    @Test
    void registerCandidate_ValidData_ReturnsSuccess() throws Exception {
        Candidate candidate = new Candidate();
        candidate.setEmail("test@candidate.com");
        candidate.setPassword("password123");
        candidate.setFullName("Test Candidate");

        mockMvc.perform(post("/auth/register-candidate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Candidate registered"));
    }

    @Test
    void registerRecruiter_ValidData_ReturnsSuccess() throws Exception {
        Recruiter recruiter = new Recruiter();
        recruiter.setEmail("test@recruiter.com");
        recruiter.setPassword("password123");
        recruiter.setFullName("Test Recruiter");

        mockMvc.perform(post("/auth/register-recruiter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recruiter)))
                .andExpect(status().isOk())
                .andExpect(content().string("Recruiter registered"));
    }

    @Test
    void registerCandidate_DuplicateEmail_ReturnsBadRequest() throws Exception {
        // First registration
        Candidate candidate = new Candidate();
        candidate.setEmail("test@candidate.com");
        candidate.setPassword("password123");
        candidate.setFullName("Test Candidate");
        candidateRepository.save(candidate);

        // Try to register again with same email
        mockMvc.perform(post("/auth/register-candidate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidate)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Candidate already exists"));
    }

    @Test
    void login_ValidCredentials_ReturnsToken() throws Exception {
        // Register a candidate first
        Candidate candidate = new Candidate();
        candidate.setEmail("test@candidate.com");
        candidate.setPassword(passwordEncoder.encode("password123"));
        candidate.setFullName("Test Candidate");
        candidateRepository.save(candidate);

        // Try to login
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@candidate.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@candidate.com\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }
} 