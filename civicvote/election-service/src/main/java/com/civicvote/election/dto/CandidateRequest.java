package com.civicvote.election.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CandidateRequest {

    @NotBlank(message = "Candidate name is required")
    @Size(max = 100, message = "Candidate name must not exceed 100 characters")
    private String candidateName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public CandidateRequest() {}

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
