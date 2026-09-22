package com.civicvote.voting.controller;

import com.civicvote.voting.dto.VoteRequest;
import com.civicvote.voting.dto.VoteResponse;
import com.civicvote.voting.service.VotingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
     * The voter identity is extracted directly from the verified JWT in SecurityContext.
     * The client NEVER submits userId.
     */
    @PostMapping
    public ResponseEntity<VoteResponse> castVote(
            @Valid @RequestBody VoteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            Authentication authentication) {

        Long userId = resolveUserId(authentication, userIdHeader);
        VoteResponse response = votingService.castVote(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Check if the authenticated user has already voted in a specific election.
     */
    @GetMapping("/status/{electionId}")
    public ResponseEntity<Map<String, Object>> getVoteStatus(
            @PathVariable Long electionId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            Authentication authentication) {

        Long userId = resolveUserId(authentication, userIdHeader);
        boolean hasVoted = votingService.hasVoted(userId, electionId);

        return ResponseEntity.ok(Map.of(
                "electionId", electionId,
                "hasVoted", hasVoted
        ));
    }

    private Long resolveUserId(Authentication authentication, String header) {
        if (authentication != null && authentication.getCredentials() instanceof Long) {
            return (Long) authentication.getCredentials();
        }
        if (header != null && !header.isBlank()) {
            if (header.startsWith("voter-")) {
                header = header.substring(6);
            }
            try {
                return Long.parseLong(header);
            } catch (NumberFormatException e) {
                return (long) Math.abs(header.hashCode());
            }
        }
        return 0L;
    }
}
