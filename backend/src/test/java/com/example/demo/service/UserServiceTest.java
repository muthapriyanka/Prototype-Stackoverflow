package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.User;
import com.example.demo.repository.UserRepository;

//Mocking the repository means creating a fake repository object and controlling the behavior of its methods.

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("maya_backend");
        user.setPassword("password");
    }

    @Test
    void loginReturnsUserWhenUsernameAndPasswordMatch() {
        when(userRepository.findByUsername("maya_backend")).thenReturn(Optional.of(user));

        User result = userService.login("maya_backend", "password");

        assertSame(user, result);
        verify(userRepository).findByUsername("maya_backend");
    }

    @Test
    void loginThrowsErrorWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login("unknown", "password")
        );
    }

    @Test
    void loginThrowsErrorWhenPasswordDoesNotMatch() {
        when(userRepository.findByUsername("maya_backend")).thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login("maya_backend", "wrong-password")
        );
    }
}
