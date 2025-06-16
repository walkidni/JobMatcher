package com.walid.jobmatcher.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MatchingService {

    public int calculateMatchScore(List<String> requiredSkills, String resumeText) {
        if (resumeText == null || resumeText.isBlank()) return 0;
        int score = 0;
        for (String skill : requiredSkills) {
            if (resumeText.toLowerCase().contains(skill.toLowerCase())) {
                score++;
            }
        }
        return score;
    }
}
