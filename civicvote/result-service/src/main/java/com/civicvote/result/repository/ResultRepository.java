package com.civicvote.result.repository;

import com.civicvote.result.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByElectionIdAndCandidateId(Long electionId, Long candidateId);

    List<Result> findByElectionIdOrderByVoteCountDesc(Long electionId);
}
