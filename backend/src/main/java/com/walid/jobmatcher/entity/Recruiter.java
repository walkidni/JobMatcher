package com.walid.jobmatcher.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "recruiters")
public class Recruiter extends User {

    @OneToMany(mappedBy = "recruiter", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<JobPost> jobPosts;
}
