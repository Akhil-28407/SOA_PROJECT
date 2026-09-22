package com.civicvote.election.repository;

import com.civicvote.election.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Query("SELECT c FROM Candidate c WHERE c.election.id = :electionId")
    List<Candidate> findByElectionId(@Param("electionId") Long electionId);

    default List<Candidate> findByElection_ElectionId(Long electionId) {
        return findByElectionId(electionId);
    }
}
