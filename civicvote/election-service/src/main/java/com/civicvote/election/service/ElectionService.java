package com.civicvote.election.service;

import com.civicvote.election.dto.*;
import com.civicvote.election.entity.Candidate;
import com.civicvote.election.entity.Election;
import com.civicvote.election.entity.ElectionStatus;
import com.civicvote.election.exception.CandidateNotFoundException;
import com.civicvote.election.exception.ElectionNotFoundException;
import com.civicvote.election.repository.CandidateRepository;
import com.civicvote.election.repository.ElectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;

    public ElectionService(ElectionRepository electionRepository,
                           CandidateRepository candidateRepository) {
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public ElectionResponse createElection(ElectionRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Election election = new Election();
        election.setTitle(request.getTitle());
        election.setDescription(request.getDescription());
        election.setStartDate(request.getStartDate());
        election.setEndDate(request.getEndDate());

        Election saved = electionRepository.save(election);
        return mapToResponse(saved);
    }

    public List<ElectionResponse> getAllElections() {
        return electionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ElectionResponse getElectionById(Long id) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new ElectionNotFoundException("Election not found with id: " + id));
        return mapToResponse(election);
    }

    @Transactional
    public ElectionResponse updateElection(Long id, ElectionRequest request) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new ElectionNotFoundException("Election not found with id: " + id));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        election.setTitle(request.getTitle());
        election.setDescription(request.getDescription());
        election.setStartDate(request.getStartDate());
        election.setEndDate(request.getEndDate());

        Election updated = electionRepository.save(election);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteElection(Long id) {
        if (!electionRepository.existsById(id)) {
            throw new ElectionNotFoundException("Election not found with id: " + id);
        }
        electionRepository.deleteById(id);
    }

    @Transactional
    public CandidateResponse addCandidate(Long electionId, CandidateRequest request) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ElectionNotFoundException("Election not found with id: " + electionId));

        Candidate candidate = new Candidate(
                request.getCandidateName(),
                request.getDescription(),
                election
        );

        Candidate saved = candidateRepository.save(candidate);
        return mapCandidateToResponse(saved);
    }

    public List<CandidateResponse> getCandidates(Long electionId) {
        if (!electionRepository.existsById(electionId)) {
            throw new ElectionNotFoundException("Election not found with id: " + electionId);
        }
        return candidateRepository.findByElection_ElectionId(electionId).stream()
                .map(this::mapCandidateToResponse)
                .collect(Collectors.toList());
    }

    public ElectionStatus getElectionStatus(Long electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ElectionNotFoundException("Election not found with id: " + electionId));
        return election.getStatus();
    }

    // --- Mapping helpers ---

    private ElectionResponse mapToResponse(Election election) {
        ElectionResponse response = new ElectionResponse();
        response.setElectionId(election.getElectionId());
        response.setTitle(election.getTitle());
        response.setDescription(election.getDescription());
        response.setStartDate(election.getStartDate());
        response.setEndDate(election.getEndDate());
        response.setStatus(election.getStatus());
        response.setCreatedAt(election.getCreatedAt());
        response.setUpdatedAt(election.getUpdatedAt());

        if (election.getCandidates() != null) {
            response.setCandidates(
                    election.getCandidates().stream()
                            .map(this::mapCandidateToResponse)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    private CandidateResponse mapCandidateToResponse(Candidate candidate) {
        CandidateResponse response = new CandidateResponse();
        response.setCandidateId(candidate.getCandidateId());
        response.setElectionId(candidate.getElection().getElectionId());
        response.setCandidateName(candidate.getCandidateName());
        response.setDescription(candidate.getDescription());
        response.setCreatedAt(candidate.getCreatedAt());
        return response;
    }
}
