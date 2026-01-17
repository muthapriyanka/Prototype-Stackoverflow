// package com.example.demo.controller;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.demo.User;
// import com.example.demo.Vote;
// import com.example.demo.VoteRequest;
// import com.example.demo.repository.UserRepository;
// //import com.example.demo.service.VoteService;

// @RestController
// @RequestMapping("/votes")
// public class VoteController {

//     // private final VoteService voteService;
//      private final UserRepository userRepository;

//     // public VoteController(VoteService voteService, UserRepository userRepository) {
//     //     this.voteService = voteService;
//     //     this.userRepository = userRepository;
//     // }

//     @PostMapping
//     public Vote createVote(@RequestBody VoteRequest request, Authentication authentication) {

//         // Step 1: get username from the JWT-authenticated user
//         String username = authentication.getName(); 

//         // Step 2: fetch the full User entity from DB
//         User user = userRepository.findByUsername(username)
//                         .orElseThrow(() -> new RuntimeException("User not found"));

//         // Step 3: pass User entity to vote service
//         return voteService.createVote(request, user);
//     }
// }
