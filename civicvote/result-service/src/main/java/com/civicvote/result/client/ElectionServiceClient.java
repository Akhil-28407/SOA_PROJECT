package com.civicvote.result.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * OpenFeign client for Election Service — used to resolve candidate names for results display.
 */
@FeignClient(name = "ELECTION-SERVICE")
public interface ElectionServiceClient {

    @GetMapping("/elections/{id}/candidates")
    List<Map<String, Object>> getCandidates(@PathVariable("id") Long id);
}
