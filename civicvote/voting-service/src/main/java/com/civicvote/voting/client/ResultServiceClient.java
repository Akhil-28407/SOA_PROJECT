package com.civicvote.voting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * OpenFeign client for communicating with the Result Service.
 * Sends only anonymous vote data (electionId, candidateId) — NO voter identity.
 */
@FeignClient(name = "RESULT-SERVICE")
public interface ResultServiceClient {

    @PostMapping("/results/vote")
    Map<String, Object> recordVote(@RequestBody Map<String, Long> voteData);
}
