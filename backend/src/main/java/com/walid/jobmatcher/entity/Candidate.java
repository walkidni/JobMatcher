package com.walid.jobmatcher.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "candidates")
public class Candidate extends User {

    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL)
    private Resume resume;
}
