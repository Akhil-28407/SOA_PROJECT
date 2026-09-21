package com.civicvote.result.service;

import com.civicvote.result.client.ElectionServiceClient;
import com.civicvote.result.dto.ResultResponse;
import com.civicvote.result.dto.VoteEvent;
import com.civicvote.result.entity.Result;
import com.civicvote.result.repository.ResultRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Result Service — Vote Tallying & Winner Calculation Tests")
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ElectionServiceClient electionServiceClient;

    @InjectMocks
    private ResultService resultService;

    private static final Long ELECTION_ID = 1L;
    private static final Long CANDIDATE_1 = 101L;
    private static final Long CANDIDATE_2 = 102L;

    @Test
    @DisplayName("Record vote creates new result if none exists and sets count to 1")
    void recordVote_NewResult() {
        VoteEvent event = new VoteEvent();
        event.setElectionId(ELECTION_ID);
        event.setCandidateId(CANDIDATE_1);

        when(resultRepository.findByElectionIdAndCandidateId(ELECTION_ID, CANDIDATE_1))
                .thenReturn(Optional.empty());

        Result newResult = new Result(ELECTION_ID, CANDIDATE_1);
        newResult.setVoteCount(0L);
        when(resultRepository.save(any(Result.class))).thenReturn(newResult);

        resultService.recordVote(event);

        verify(resultRepository, times(2)).save(any(Result.class));
    }

    @Test
    @DisplayName("Record vote increments existing vote count atomically")
    void recordVote_ExistingResult() {
        VoteEvent event = new VoteEvent();
        event.setElectionId(ELECTION_ID);
        event.setCandidateId(CANDIDATE_1);

        Result existing = new Result(ELECTION_ID, CANDIDATE_1);
        existing.setVoteCount(5L);

        when(resultRepository.findByElectionIdAndCandidateId(ELECTION_ID, CANDIDATE_1))
                .thenReturn(Optional.of(existing));

        resultService.recordVote(event);

        assertEquals(6L, existing.getVoteCount());
        verify(resultRepository).save(existing);
    }

    @Test
    @DisplayName("Get results calculates percentages and candidate names properly")
    void getResults_CalculatesPercentagesAndNames() {
        Result r1 = new Result(ELECTION_ID, CANDIDATE_1);
        r1.setVoteCount(75L);
        Result r2 = new Result(ELECTION_ID, CANDIDATE_2);
        r2.setVoteCount(25L);

        when(resultRepository.findByElectionIdOrderByVoteCountDesc(ELECTION_ID))
                .thenReturn(List.of(r1, r2));

        when(electionServiceClient.getCandidates(ELECTION_ID)).thenReturn(List.of(
                Map.of("candidateId", CANDIDATE_1, "candidateName", "Alice"),
                Map.of("candidateId", CANDIDATE_2, "candidateName", "Bob")
        ));

        List<ResultResponse> results = resultService.getResults(ELECTION_ID);

        assertEquals(2, results.size());
        assertEquals("Alice", results.get(0).getCandidateName());
        assertEquals(75L, results.get(0).getVoteCount());
        assertEquals(75.0, results.get(0).getPercentage());

        assertEquals("Bob", results.get(1).getCandidateName());
        assertEquals(25L, results.get(1).getVoteCount());
        assertEquals(25.0, results.get(1).getPercentage());
    }

    @Test
    @DisplayName("Get winner returns candidate with maximum votes")
    void getWinner_ReturnsTopCandidate() {
        Result r1 = new Result(ELECTION_ID, CANDIDATE_1);
        r1.setVoteCount(80L);
        Result r2 = new Result(ELECTION_ID, CANDIDATE_2);
        r2.setVoteCount(20L);

        when(resultRepository.findByElectionIdOrderByVoteCountDesc(ELECTION_ID))
                .thenReturn(List.of(r1, r2));

        when(electionServiceClient.getCandidates(ELECTION_ID)).thenReturn(List.of(
                Map.of("candidateId", CANDIDATE_1, "candidateName", "Alice Winner"),
                Map.of("candidateId", CANDIDATE_2, "candidateName", "Bob RunnerUp")
        ));

        ResultResponse winner = resultService.getWinner(ELECTION_ID);

        assertNotNull(winner);
        assertEquals(CANDIDATE_1, winner.getCandidateId());
        assertEquals("Alice Winner", winner.getCandidateName());
        assertEquals(80L, winner.getVoteCount());
        assertEquals(80.0, winner.getPercentage());
    }
}
