package com.civicvote.election.controller;

import com.civicvote.election.dto.*;
import com.civicvote.election.entity.ElectionStatus;
import com.civicvote.election.service.ElectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/elections")
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @PostMapping
    public ResponseEntity<ElectionResponse> createElection(
            @Valid @RequestBody ElectionRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        validateAdminRole(role);
        ElectionResponse response = electionService.createElection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ElectionResponse>> getAllElections() {
        return ResponseEntity.ok(electionService.getAllElections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElectionResponse> getElectionById(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.getElectionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ElectionResponse> updateElection(
            @PathVariable Long id,
            @Valid @RequestBody ElectionRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        validateAdminRole(role);
        return ResponseEntity.ok(electionService.updateElection(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteElection(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        validateAdminRole(role);
        electionService.deleteElection(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/candidates")
    public ResponseEntity<CandidateResponse> addCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        validateAdminRole(role);
        CandidateResponse response = electionService.addCandidate(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/candidates")
    public ResponseEntity<List<CandidateResponse>> getCandidates(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.getCandidates(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getElectionStatus(@PathVariable Long id) {
        ElectionStatus status = electionService.getElectionStatus(id);
        return ResponseEntity.ok(Map.of(
                "electionId", id,
                "status", status.name()
        ));
    }

    private void validateAdminRole(String role) {
        if (role == null || !role.equals("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Admin access required");
        }
    }
}
