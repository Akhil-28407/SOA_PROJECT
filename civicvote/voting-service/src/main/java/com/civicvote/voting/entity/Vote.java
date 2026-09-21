package com.civicvote.voting.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"voter_reference", "election_id"}, name = "uk_voter_election")
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "voter_reference", nullable = false)
    private String voterReference;

    @Column(name = "election_id", nullable = false)
    private Long electionId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "cast_at", nullable = false, updatable = false)
    private LocalDateTime castAt;

    @PrePersist
    protected void onCreate() {
        this.castAt = LocalDateTime.now();
    }

    // Constructors
    public Vote() {}

    public Vote(String voterReference, Long electionId, Long candidateId) {
        this.voterReference = voterReference;
        this.electionId = electionId;
        this.candidateId = candidateId;
    }

    // Getters and Setters
    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public String getVoterReference() { return voterReference; }
    public void setVoterReference(String voterReference) { this.voterReference = voterReference; }

    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public LocalDateTime getCastAt() { return castAt; }
    public void setCastAt(LocalDateTime castAt) { this.castAt = castAt; }
}
