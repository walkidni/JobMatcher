package com.walid.jobmatcher.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@MappedSuperclass // Not a table itself
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String fullName;
}
