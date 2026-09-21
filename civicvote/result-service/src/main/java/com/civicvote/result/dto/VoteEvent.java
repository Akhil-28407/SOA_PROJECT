package com.civicvote.result.dto;

public class VoteEvent {

    private Long electionId;
    private Long candidateId;

    public VoteEvent() {}

    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
}
