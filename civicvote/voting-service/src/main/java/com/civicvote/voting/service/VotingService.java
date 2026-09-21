package com.civicvote.voting.service;

import com.civicvote.voting.client.ElectionServiceClient;
import com.civicvote.voting.client.ResultServiceClient;
import com.civicvote.voting.dto.VoteRequest;
import com.civicvote.voting.dto.VoteResponse;
import com.civicvote.voting.entity.Vote;
import com.civicvote.voting.exception.ElectionNotActiveException;
import com.civicvote.voting.exception.InvalidVoteException;
import com.civicvote.voting.exception.UserAlreadyVotedException;
import com.civicvote.voting.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VotingService {

    private final VoteRepository voteRepository;
    private final ElectionServiceClient electionServiceClient;
    private final ResultServiceClient resultServiceClient;

    public VotingService(VoteRepository voteRepository,
                         ElectionServiceClient electionServiceClient,
                         ResultServiceClient resultServiceClient) {
        this.voteRepository = voteRepository;
        this.electionServiceClient = electionServiceClient;
        this.resultServiceClient = resultServiceClient;
    }

    /**
     * Complete vote validation and processing flow:
     * 1. Validate JWT (done at gateway)
     * 2. Extract authenticated user (from headers)
     * 3. Check election exists
     * 4. Check election is ACTIVE
     * 5. Check candidate exists
     * 6. Check candidate belongs to election
     * 7. Check whether voter already voted
     * 8. Save vote
     * 9. Notify Result Service (anonymous - only electionId + candidateId)
     * 10. Return success
     */
    @Transactional
    public VoteResponse castVote(VoteRequest request, String voterReference) {
        Long electionId = request.getElectionId();
        Long candidateId = request.getCandidateId();

        // Step 3 & 4: Validate election exists and is ACTIVE
        try {
            Map<String, Object> statusMap = electionServiceClient.getElectionStatus(electionId);
            String status = (String) statusMap.get("status");
            if (!"ACTIVE".equals(status)) {
                throw new ElectionNotActiveException(
                        "Election is not active. Current status: " + status);
            }
        } catch (ElectionNotActiveException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidVoteException("Election not found or unavailable: " + electionId);
        }

        // Step 5 & 6: Validate candidate exists and belongs to election
        try {
            List<Map<String, Object>> candidates = electionServiceClient.getCandidates(electionId);
            boolean candidateValid = candidates.stream()
                    .anyMatch(c -> {
                        Object cId = c.get("candidateId");
                        if (cId instanceof Number) {
                            return ((Number) cId).longValue() == candidateId;
                        }
                        return false;
                    });
            if (!candidateValid) {
                throw new InvalidVoteException(
                        "Candidate " + candidateId + " does not belong to election " + electionId);
            }
        } catch (InvalidVoteException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidVoteException("Unable to validate candidate: " + e.getMessage());
        }

        // Step 7: Check for duplicate vote (application-level)
        if (voteRepository.existsByVoterReferenceAndElectionId(voterReference, electionId)) {
            throw new UserAlreadyVotedException("User has already voted in this election");
        }

        // Step 8: Save vote
        Vote vote = new Vote(voterReference, electionId, candidateId);
        Vote savedVote = voteRepository.save(vote);

        // Step 9: Notify Result Service — ANONYMOUS (only electionId + candidateId)
        Map<String, Long> anonymousVoteData = new HashMap<>();
        anonymousVoteData.put("electionId", electionId);
        anonymousVoteData.put("candidateId", candidateId);

        try {
            resultServiceClient.recordVote(anonymousVoteData);
        } catch (Exception e) {
            // If Result Service fails, we should not silently succeed
            // The vote has been saved but result not incremented — log error
            // In production, we'd use a compensation mechanism or event queue
            throw new RuntimeException("Vote saved but result service notification failed: " + e.getMessage());
        }

        // Step 10: Return success
        return new VoteResponse(
                savedVote.getVoteId(),
                savedVote.getElectionId(),
                savedVote.getCandidateId(),
                savedVote.getCastAt(),
                "Vote successfully recorded"
        );
    }

    public boolean hasVoted(String voterReference, Long electionId) {
        return voteRepository.existsByVoterReferenceAndElectionId(voterReference, electionId);
    }
}
