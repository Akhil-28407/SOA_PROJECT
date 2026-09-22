package com.civicvote.result.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "results", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"election_id", "candidate_id"}, name = "uk_election_candidate")
})
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "election_id", nullable = false)
    private Long electionId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "vote_count", nullable = false)
    private Long voteCount = 0L;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Result() {}

    public Result(Long electionId, Long candidateId) {
        this.electionId = electionId;
        this.candidateId = candidateId;
        this.voteCount = 0L;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResultId() { return id; }
    public void setResultId(Long resultId) { this.id = resultId; }

    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getVoteCount() { return voteCount; }
    public void setVoteCount(Long voteCount) { this.voteCount = voteCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
