// package com.example.demo.service;

// import java.util.Optional;

// import org.springframework.stereotype.Service;

// import com.example.demo.User;
// import com.example.demo.Vote;
// import com.example.demo.VoteRequest;
// import com.example.demo.repository.VoteRepository;

// @Service
// public class VoteService {

//     private final VoteRepository voteRepository;

//     public VoteService(VoteRepository voteRepository) {
//         this.voteRepository = voteRepository;
//     }

//     // public Vote createVote(VoteRequest request, User user) {

//     // Optional<Vote> existing = voteRepository
//     //     .findByUser_IdAndEntityIdAndEntityType(
//     //         user.getId(),
//     //         request.getEntityId(),
//     //         request.getEntityType()
//     //     );

//     // if (existing.isPresent()) {
//     //     throw new RuntimeException("User already voted on this entity");
//     // }

//     // Vote vote = new Vote();
//     // vote.setUser(user);
//     // vote.setEntityId(request.getEntityId());
//     // vote.setEntityType(request.getEntityType());
//     // vote.setVoteType(
//     //     request.getValue() == 1 ? Vote.VoteType.UP : Vote.VoteType.DOWN
//     // );

//     // return voteRepository.save(vote);
//     // }
// }