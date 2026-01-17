package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.JwtUtil;
import com.example.demo.LoginRequest;
import com.example.demo.User;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

    // Signup
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Login
    // @PostMapping("/login")
    // public LoginResponse login(@RequestBody LoginRequest request) {
    //     User user = userService.login(request.getUsername(), request.getPassword());

    //     LoginResponse response = new LoginResponse();
    //     response.setUserId(user.getId());
    //     response.setUsername(user.getUsername());

    //     return response;
    // }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    User user = userService.login(request.getUsername(), request.getPassword());

    String token = jwtUtil.generateToken(user);

    return ResponseEntity.ok(Map.of(
        "token", token,
        "user", Map.of(
            "id", user.getId(),
            "username", user.getUsername()
        )
    ));
}


}