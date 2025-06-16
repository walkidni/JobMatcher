package com.walid.jobmatcher.controller;

import com.walid.jobmatcher.entity.JobPost;
import com.walid.jobmatcher.entity.Recruiter;
import com.walid.jobmatcher.repository.JobPostRepository;
import com.walid.jobmatcher.repository.RecruiterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JobPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Recruiter testRecruiter;
    private JobPost testJobPost;

    @BeforeEach
    void setUp() {
        jobPostRepository.deleteAll();
        recruiterRepository.deleteAll();

        // Create test recruiter
        testRecruiter = new Recruiter();
        testRecruiter.setEmail("test@recruiter.com");
        testRecruiter.setPassword(passwordEncoder.encode("password"));
        testRecruiter.setFullName("Test Recruiter");
        testRecruiter = recruiterRepository.save(testRecruiter);

        // Create test job post
        testJobPost = new JobPost();
        testJobPost.setTitle("Test Job");
        testJobPost.setDescription("Test Description");
        testJobPost.setRecruiter(testRecruiter);
        testJobPost.setRequiredSkills(Arrays.asList("Java", "Spring"));
        testJobPost = jobPostRepository.save(testJobPost);
    }

    @Test
    @WithMockUser(username = "test@recruiter.com", roles = "RECRUITER")
    void createJobPost_ValidJobPost_ReturnsCreatedJobPost() throws Exception {
        JobPost newJobPost = new JobPost();
        newJobPost.setTitle("New Job");
        newJobPost.setDescription("New Description");
        newJobPost.setRecruiter(testRecruiter);
        newJobPost.setRequiredSkills(Arrays.asList("Python", "Django"));

        mockMvc.perform(post("/api/job-posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newJobPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Job")))
                .andExpect(jsonPath("$.description", is("New Description")));
    }

    @Test
    @WithMockUser(username = "test@candidate.com", roles = {"RECRUITER", "CANDIDATE"})
    void getAllJobPosts_ReturnsListOfJobPosts() throws Exception {
        String response = mockMvc.perform(get("/api/job-posts"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("getAllJobPosts response: " + response);
    }

    @Test
    @WithMockUser(username = "test@candidate.com", roles = {"RECRUITER", "CANDIDATE"})
    void getByRecruiter_ValidRecruiterId_ReturnsJobPosts() throws Exception {
        String response = mockMvc.perform(get("/api/job-posts/recruiter/" + testRecruiter.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("getByRecruiter response: " + response);
    }
} 