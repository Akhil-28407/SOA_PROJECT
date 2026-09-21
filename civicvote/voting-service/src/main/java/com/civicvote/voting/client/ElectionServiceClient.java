package com.civicvote.voting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * OpenFeign client for communicating with the Election Service.
 * Used to validate election existence, status, and candidate membership.
 */
@FeignClient(name = "ELECTION-SERVICE")
public interface ElectionServiceClient {

    @GetMapping("/elections/{id}")
    Map<String, Object> getElection(@PathVariable("id") Long id);

    @GetMapping("/elections/{id}/candidates")
    List<Map<String, Object>> getCandidates(@PathVariable("id") Long id);

    @GetMapping("/elections/{id}/status")
    Map<String, Object> getElectionStatus(@PathVariable("id") Long id);
}
