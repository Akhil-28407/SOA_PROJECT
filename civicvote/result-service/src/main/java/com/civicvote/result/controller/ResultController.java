package com.civicvote.result.controller;

import com.civicvote.result.dto.ResultResponse;
import com.civicvote.result.dto.VoteEvent;
import com.civicvote.result.service.ResultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    /**
     * Called by Voting Service via OpenFeign.
     * Receives only anonymous vote data (electionId + candidateId).
     * NO voter identity is received or stored.
     */
    @PostMapping("/vote")
    public ResponseEntity<Map<String, String>> recordVote(@RequestBody VoteEvent event) {
        resultService.recordVote(event);
        return ResponseEntity.ok(Map.of("status", "recorded"));
    }

    @GetMapping("/{electionId}")
    public ResponseEntity<List<ResultResponse>> getResults(@PathVariable Long electionId) {
        List<ResultResponse> results = resultService.getResults(electionId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{electionId}/winner")
    public ResponseEntity<?> getWinner(@PathVariable Long electionId) {
        ResultResponse winner = resultService.getWinner(electionId);
        if (winner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No votes recorded for this election"));
        }
        return ResponseEntity.ok(winner);
    }
}
