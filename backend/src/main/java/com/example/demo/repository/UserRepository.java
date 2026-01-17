package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> { // user: entity string: type of primary key 
    Optional<User> findByUsername(String username);
}


