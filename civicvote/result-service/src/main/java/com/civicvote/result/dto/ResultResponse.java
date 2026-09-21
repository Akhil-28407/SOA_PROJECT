package com.civicvote.result.dto;

public class ResultResponse {

    private Long candidateId;
    private String candidateName;
    private Long voteCount;
    private double percentage;

    public ResultResponse() {}

    public ResultResponse(Long candidateId, String candidateName, Long voteCount, double percentage) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public Long getVoteCount() { return voteCount; }
    public void setVoteCount(Long voteCount) { this.voteCount = voteCount; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}
