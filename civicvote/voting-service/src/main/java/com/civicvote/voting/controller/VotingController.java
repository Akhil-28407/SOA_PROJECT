package com.civicvote.voting.controller;

import com.civicvote.voting.dto.VoteRequest;
import com.civicvote.voting.dto.VoteResponse;
import com.civicvote.voting.service.VotingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/votes")
public class VotingController {

    private final VotingService votingService;

    public VotingController(VotingService votingService) {
        this.votingService = votingService;
    }

    /**
     * Cast a vote.
     * The voter identity comes from the X-User-Id header (set by gateway from JWT).
     * The client NEVER submits userId — it is extracted from the authenticated JWT.
     */
    @PostMapping
    public ResponseEntity<VoteResponse> castVote(
            @Valid @RequestBody VoteRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Voter reference is a hashed/internal reference derived from the authenticated user
        String voterReference = "voter-" + userId;

        VoteResponse response = votingService.castVote(request, voterReference);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Check if the authenticated user has already voted in a specific election.
     */
    @GetMapping("/status/{electionId}")
    public ResponseEntity<Map<String, Object>> getVoteStatus(
            @PathVariable Long electionId,
            @RequestHeader("X-User-Id") String userId) {

        String voterReference = "voter-" + userId;
        boolean hasVoted = votingService.hasVoted(voterReference, electionId);

        return ResponseEntity.ok(Map.of(
                "electionId", electionId,
                "hasVoted", hasVoted
        ));
    }
}
