package com.example.cgi.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private String name;

    protected Account() {
        // Требуется JPA
    }

    public Long getId() {
        return id;
    }

    public Long getName() { return id; }

    public Instant getCreatedAt() {
        return createdAt;
    }
}