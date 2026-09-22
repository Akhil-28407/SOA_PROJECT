package com.civicvote.election.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors
    public Candidate() {}

    public Candidate(String name, String description, Election election) {
        this.name = name;
        this.description = description;
        this.election = election;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCandidateId() { return id; }
    public void setCandidateId(Long candidateId) { this.id = candidateId; }

    public Election getElection() { return election; }
    public void setElection(Election election) { this.election = election; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCandidateName() { return name; }
    public void setCandidateName(String candidateName) { this.name = candidateName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
