package com.walid.jobmatcher.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MatchingServiceTest {

    @Autowired
    private MatchingService matchingService;

    @Test
    void calculateMatchScore_WithMatchingSkills_ReturnsCorrectScore() {
        List<String> requiredSkills = Arrays.asList("Java", "Spring", "Hibernate");
        String resumeText = "Experienced Java developer with Spring and Hibernate knowledge";

        int score = matchingService.calculateMatchScore(requiredSkills, resumeText);

        assertEquals(3, score);
    }

    @Test
    void calculateMatchScore_WithPartialMatchingSkills_ReturnsCorrectScore() {
        List<String> requiredSkills = Arrays.asList("Java", "Spring", "Hibernate", "React");
        String resumeText = "Experienced Java developer with Spring knowledge";

        int score = matchingService.calculateMatchScore(requiredSkills, resumeText);

        assertEquals(2, score);
    }

    @Test
    void calculateMatchScore_WithNoMatchingSkills_ReturnsZero() {
        List<String> requiredSkills = Arrays.asList("Java", "Spring");
        String resumeText = "Python developer with Django experience";

        int score = matchingService.calculateMatchScore(requiredSkills, resumeText);

        assertEquals(0, score);
    }

    @Test
    void calculateMatchScore_WithEmptyResume_ReturnsZero() {
        List<String> requiredSkills = Arrays.asList("Java", "Spring");
        String resumeText = "";

        int score = matchingService.calculateMatchScore(requiredSkills, resumeText);

        assertEquals(0, score);
    }

    @Test
    void calculateMatchScore_WithNullResume_ReturnsZero() {
        List<String> requiredSkills = Arrays.asList("Java", "Spring");
        String resumeText = null;

        int score = matchingService.calculateMatchScore(requiredSkills, resumeText);

        assertEquals(0, score);
    }
} 