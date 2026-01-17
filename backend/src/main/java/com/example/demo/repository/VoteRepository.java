package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Vote;
import com.example.demo.Vote.EntityType;

@Repository
public interface VoteRepository extends JpaRepository<Vote, String> {

    Optional<Vote> findByUser_IdAndEntityIdAndEntityType(
        String userId,
        String entityId,
        Vote.EntityType entityType
);

    long countByEntityIdAndEntityType(String entityId, EntityType entityType);

    @Query("""
   SELECT COALESCE(SUM(
       CASE 
         WHEN v.voteType = 'UP' THEN 1 
         ELSE -1 
       END
   ), 0)
   FROM Vote v
   WHERE v.entityId = :entityId
   AND v.entityType = :entityType
""")
int getVoteCount(@Param("entityId") String entityId, @Param("entityType") Vote.EntityType entityType);

}

