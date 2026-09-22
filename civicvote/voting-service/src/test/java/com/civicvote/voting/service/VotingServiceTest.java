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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Voting Service — Core Business Logic & Unique Vote Tests")
class VotingServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private ElectionServiceClient electionServiceClient;

    @Mock
    private ResultServiceClient resultServiceClient;

    @InjectMocks
    private VotingService votingService;

    private VoteRequest voteRequest;
    private static final Long USER_ID = 42L;
    private static final String VOTER_REF = "voter-42";
    private static final Long ELECTION_ID = 1L;
    private static final Long CANDIDATE_ID = 10L;

    @BeforeEach
    void setUp() {
        voteRequest = new VoteRequest();
        voteRequest.setElectionId(ELECTION_ID);
        voteRequest.setCandidateId(CANDIDATE_ID);
    }

    @Test
    @DisplayName("Vote rejected when election is not ACTIVE")
    void castVote_ElectionNotActive_ThrowsException() {
        when(electionServiceClient.getElectionStatus(ELECTION_ID))
                .thenReturn(Map.of("status", "COMPLETED"));

        assertThrows(ElectionNotActiveException.class, () ->
                votingService.castVote(voteRequest, USER_ID));

        verify(voteRepository, never()).save(any());
        verify(resultServiceClient, never()).recordVote(any());
    }

    @Test
    @DisplayName("Vote rejected when candidate does not belong to election")
    void castVote_InvalidCandidate_ThrowsException() {
        when(electionServiceClient.getElectionStatus(ELECTION_ID))
                .thenReturn(Map.of("status", "ACTIVE"));
        when(electionServiceClient.getCandidates(ELECTION_ID))
                .thenReturn(List.of(
                        Map.of("candidateId", 99L, "candidateName", "Other Candidate")
                ));

        assertThrows(InvalidVoteException.class, () ->
                votingService.castVote(voteRequest, USER_ID));

        verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Duplicate vote rejected when voter already voted in this election (user_id + election_id)")
    void castVote_DuplicateVote_ThrowsException() {
        when(electionServiceClient.getElectionStatus(ELECTION_ID))
                .thenReturn(Map.of("status", "ACTIVE"));
        when(electionServiceClient.getCandidates(ELECTION_ID))
                .thenReturn(List.of(
                        Map.of("candidateId", CANDIDATE_ID, "candidateName", "Valid Candidate")
                ));
        when(voteRepository.existsByUserIdAndElectionId(USER_ID, ELECTION_ID))
                .thenReturn(true);

        assertThrows(UserAlreadyVotedException.class, () ->
                votingService.castVote(voteRequest, USER_ID));

        verify(voteRepository, never()).save(any());
        verify(resultServiceClient, never()).recordVote(any());
    }

    @Test
    @DisplayName("Valid vote is recorded and forwarded anonymously to Result Service")
    void castVote_Success_AnonymousForwarding() {
        when(electionServiceClient.getElectionStatus(ELECTION_ID))
                .thenReturn(Map.of("status", "ACTIVE"));
        when(electionServiceClient.getCandidates(ELECTION_ID))
                .thenReturn(List.of(
                        Map.of("candidateId", CANDIDATE_ID, "candidateName", "Alice Johnson")
                ));
        when(voteRepository.existsByUserIdAndElectionId(USER_ID, ELECTION_ID))
                .thenReturn(false);

        Vote savedVote = new Vote(USER_ID, ELECTION_ID, CANDIDATE_ID);
        savedVote.setId(100L);
        when(voteRepository.save(any(Vote.class))).thenReturn(savedVote);

        VoteResponse response = votingService.castVote(voteRequest, USER_ID);

        assertNotNull(response);
        assertEquals(ELECTION_ID, response.getElectionId());
        assertEquals(CANDIDATE_ID, response.getCandidateId());
        assertEquals("Vote successfully recorded", response.getMessage());

        // Verify vote was saved locally
        verify(voteRepository).save(any(Vote.class));

        // CRITICAL: Verify resultServiceClient received ONLY anonymous payload {electionId, candidateId}
        verify(resultServiceClient).recordVote(eq(Map.of(
                "electionId", ELECTION_ID,
                "candidateId", CANDIDATE_ID
        )));
    }

    @Test
    @DisplayName("hasVoted correctly queries repository by userId")
    void hasVoted_ReturnsRepositoryStatus() {
        when(voteRepository.existsByUserIdAndElectionId(USER_ID, ELECTION_ID))
                .thenReturn(true);

        assertTrue(votingService.hasVoted(USER_ID, ELECTION_ID));
        verify(voteRepository).existsByUserIdAndElectionId(USER_ID, ELECTION_ID);
    }
}
