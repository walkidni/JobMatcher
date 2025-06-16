package com.walid.jobmatcher.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class CandidateMatchDTO {
    private Long candidateId;
    private String candidateEmail;
    private int matchScore;
}
