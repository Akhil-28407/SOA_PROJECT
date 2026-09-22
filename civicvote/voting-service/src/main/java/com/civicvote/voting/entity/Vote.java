package com.civicvote.voting.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "election_id"}, name = "uk_user_election")
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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

    public Vote(Long userId, Long electionId, Long candidateId) {
        this.userId = userId;
        this.electionId = electionId;
        this.candidateId = candidateId;
    }

    public Vote(String voterReference, Long electionId, Long candidateId) {
        this.userId = parseUserId(voterReference);
        this.electionId = electionId;
        this.candidateId = candidateId;
    }

    private static Long parseUserId(String ref) {
        if (ref == null) return null;
        if (ref.startsWith("voter-")) {
            try {
                return Long.parseLong(ref.substring(6));
            } catch (NumberFormatException ignored) {}
        }
        try {
            return Long.parseLong(ref);
        } catch (NumberFormatException e) {
            return (long) Math.abs(ref.hashCode());
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVoteId() { return id; }
    public void setVoteId(Long voteId) { this.id = voteId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getVoterReference() { return "voter-" + userId; }
    public void setVoterReference(String voterReference) { this.userId = parseUserId(voterReference); }

    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public LocalDateTime getCastAt() { return castAt; }
    public void setCastAt(LocalDateTime castAt) { this.castAt = castAt; }
}
