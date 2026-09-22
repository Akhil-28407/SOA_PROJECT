package com.civicvote.voting.repository;

import com.civicvote.voting.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByUserIdAndElectionId(Long userId, Long electionId);

    default boolean existsByVoterReferenceAndElectionId(String voterReference, Long electionId) {
        Long userId = parseUserId(voterReference);
        return existsByUserIdAndElectionId(userId, electionId);
    }

    private static Long parseUserId(String ref) {
        if (ref == null) return null;
        if (ref.startsWith("voter-")) {
            try {
                return Long.parseLong(ref.substring(6));
            } catch (NumberFormatException ignored) {}
        }
        try {
            return Long.parseLong(ref);
        } catch (NumberFormatException e) {
            return (long) Math.abs(ref.hashCode());
        }
    }
}
