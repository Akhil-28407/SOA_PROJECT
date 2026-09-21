package com.civicvote.result.service;

import com.civicvote.result.client.ElectionServiceClient;
import com.civicvote.result.dto.ResultResponse;
import com.civicvote.result.dto.VoteEvent;
import com.civicvote.result.entity.Result;
import com.civicvote.result.repository.ResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ElectionServiceClient electionServiceClient;

    public ResultService(ResultRepository resultRepository,
                         ElectionServiceClient electionServiceClient) {
        this.resultRepository = resultRepository;
        this.electionServiceClient = electionServiceClient;
    }

    /**
     * Atomically increment vote count for a candidate in an election.
     * Creates the result record if it doesn't exist yet.
     */
    @Transactional
    public void recordVote(VoteEvent event) {
        Result result = resultRepository
                .findByElectionIdAndCandidateId(event.getElectionId(), event.getCandidateId())
                .orElseGet(() -> {
                    Result newResult = new Result(event.getElectionId(), event.getCandidateId());
                    return resultRepository.save(newResult);
                });

        result.setVoteCount(result.getVoteCount() + 1);
        resultRepository.save(result);
    }

    /**
     * Get results for an election with candidate names and percentages.
     */
    public List<ResultResponse> getResults(Long electionId) {
        List<Result> results = resultRepository.findByElectionIdOrderByVoteCountDesc(electionId);

        // Calculate total votes
        long totalVotes = results.stream().mapToLong(Result::getVoteCount).sum();

        // Resolve candidate names from Election Service
        Map<Long, String> candidateNames = resolveCandidateNames(electionId);

        return results.stream().map(r -> {
            double percentage = totalVotes > 0
                    ? Math.round((r.getVoteCount() * 100.0 / totalVotes) * 100.0) / 100.0
                    : 0.0;
            return new ResultResponse(
                    r.getCandidateId(),
                    candidateNames.getOrDefault(r.getCandidateId(), "Candidate " + r.getCandidateId()),
                    r.getVoteCount(),
                    percentage
            );
        }).collect(Collectors.toList());
    }

    /**
     * Get the winner of an election.
     */
    public ResultResponse getWinner(Long electionId) {
        List<Result> results = resultRepository.findByElectionIdOrderByVoteCountDesc(electionId);

        if (results.isEmpty()) {
            return null;
        }

        long totalVotes = results.stream().mapToLong(Result::getVoteCount).sum();
        Result winner = results.get(0);

        Map<Long, String> candidateNames = resolveCandidateNames(electionId);

        double percentage = totalVotes > 0
                ? Math.round((winner.getVoteCount() * 100.0 / totalVotes) * 100.0) / 100.0
                : 0.0;

        return new ResultResponse(
                winner.getCandidateId(),
                candidateNames.getOrDefault(winner.getCandidateId(), "Candidate " + winner.getCandidateId()),
                winner.getVoteCount(),
                percentage
        );
    }

    private Map<Long, String> resolveCandidateNames(Long electionId) {
        Map<Long, String> nameMap = new HashMap<>();
        try {
            List<Map<String, Object>> candidates = electionServiceClient.getCandidates(electionId);
            for (Map<String, Object> c : candidates) {
                Object idObj = c.get("candidateId");
                Object nameObj = c.get("candidateName");
                if (idObj instanceof Number && nameObj != null) {
                    nameMap.put(((Number) idObj).longValue(), nameObj.toString());
                }
            }
        } catch (Exception e) {
            // If Election Service is unavailable, fallback to generic names
        }
        return nameMap;
    }
}
