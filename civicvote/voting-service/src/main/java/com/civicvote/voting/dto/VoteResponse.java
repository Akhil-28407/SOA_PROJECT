package com.civicvote.voting.dto;

import java.time.LocalDateTime;

public class VoteResponse {

    private Long voteId;
    private Long electionId;
    private Long candidateId;
    private LocalDateTime castAt;
    private String message;

    public VoteResponse() {}

    public VoteResponse(Long voteId, Long electionId, Long candidateId, LocalDateTime castAt, String message) {
        this.voteId = voteId;
        this.electionId = electionId;
        this.candidateId = candidateId;
        this.castAt = castAt;
        this.message = message;
    }

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public LocalDateTime getCastAt() { return castAt; }
    public void setCastAt(LocalDateTime castAt) { this.castAt = castAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
