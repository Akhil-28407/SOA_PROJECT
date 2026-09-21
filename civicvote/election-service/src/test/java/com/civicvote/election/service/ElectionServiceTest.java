package com.civicvote.election.service;

import com.civicvote.election.dto.CandidateRequest;
import com.civicvote.election.dto.CandidateResponse;
import com.civicvote.election.dto.ElectionRequest;
import com.civicvote.election.dto.ElectionResponse;
import com.civicvote.election.entity.Candidate;
import com.civicvote.election.entity.Election;
import com.civicvote.election.entity.ElectionStatus;
import com.civicvote.election.exception.ElectionNotFoundException;
import com.civicvote.election.repository.CandidateRepository;
import com.civicvote.election.repository.ElectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Election Service — Core Business Logic Tests")
class ElectionServiceTest {

    @Mock
    private ElectionRepository electionRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private ElectionService electionService;

    private Election election;
    private ElectionRequest electionRequest;
    private static final Long ELECTION_ID = 1L;

    @BeforeEach
    void setUp() {
        election = new Election();
        election.setElectionId(ELECTION_ID);
        election.setTitle("Student Union 2026");
        election.setDescription("Annual elections");
        election.setStartDate(LocalDateTime.now().minusDays(1));
        election.setEndDate(LocalDateTime.now().plusDays(2));

        electionRequest = new ElectionRequest();
        electionRequest.setTitle("Student Union 2026");
        electionRequest.setDescription("Annual elections");
        electionRequest.setStartDate(LocalDateTime.now().minusDays(1));
        electionRequest.setEndDate(LocalDateTime.now().plusDays(2));
    }

    @Test
    @DisplayName("Create election saves election and returns response")
    void createElection_Success() {
        when(electionRepository.save(any(Election.class))).thenReturn(election);

        ElectionResponse response = electionService.createElection(electionRequest);

        assertNotNull(response);
        assertEquals(ELECTION_ID, response.getElectionId());
        assertEquals("Student Union 2026", response.getTitle());
        assertEquals(ElectionStatus.ACTIVE, response.getStatus());
        verify(electionRepository).save(any(Election.class));
    }

    @Test
    @DisplayName("Create election with end date before start date throws IllegalArgumentException")
    void createElection_InvalidDates_ThrowsException() {
        electionRequest.setStartDate(LocalDateTime.now().plusDays(2));
        electionRequest.setEndDate(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> electionService.createElection(electionRequest));
        verify(electionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get election by ID throws ElectionNotFoundException when not found")
    void getElectionById_NotFound_ThrowsException() {
        when(electionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ElectionNotFoundException.class, () -> electionService.getElectionById(999L));
    }

    @Test
    @DisplayName("Add candidate saves and links candidate to election")
    void addCandidate_Success() {
        when(electionRepository.findById(ELECTION_ID)).thenReturn(Optional.of(election));

        Candidate candidate = new Candidate();
        candidate.setCandidateId(101L);
        candidate.setElection(election);
        candidate.setCandidateName("Jane Doe");
        candidate.setDescription("Running for President");

        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        CandidateRequest req = new CandidateRequest();
        req.setCandidateName("Jane Doe");
        req.setDescription("Running for President");

        CandidateResponse response = electionService.addCandidate(ELECTION_ID, req);

        assertNotNull(response);
        assertEquals(101L, response.getCandidateId());
        assertEquals("Jane Doe", response.getCandidateName());
        assertEquals(ELECTION_ID, response.getElectionId());
        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Get election status returns dynamic status")
    void getElectionStatus_ReturnsStatus() {
        when(electionRepository.findById(ELECTION_ID)).thenReturn(Optional.of(election));

        ElectionStatus status = electionService.getElectionStatus(ELECTION_ID);

        assertEquals(ElectionStatus.ACTIVE, status);
    }
}
