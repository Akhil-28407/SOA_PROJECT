package com.civicvote.voting.repository;

import com.civicvote.voting.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByVoterReferenceAndElectionId(String voterReference, Long electionId);
}
