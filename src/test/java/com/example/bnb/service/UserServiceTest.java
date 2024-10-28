package com.example.bnb.service;

import com.example.bnb.model.User;
import com.example.bnb.repository.UserRepository;
import jakarta.mail.internet.AddressException;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createsUserIfDoesntExist() {
        String email = "test@example.com";
        String name = "Test User";
        String password = "!password7";
        String encodedPassword = "Encoded!password7";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        User test = userService.createUser(email, name, password);

        assertNotNull(test);
        assertEquals(email, test.getEmail());
        assertEquals(name, test.getName());
        assertEquals(encodedPassword, test.getPassword());

        verify(userRepository, times(1)).save(test);
    }

    @Test
    void throwsExceptionIfEmailAlreadyInUse() {
        String email = "test@example.com";
        String name = "Test User";
        String password = "!password7";
        String encodedPassword = "Encoded!password7";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        EntityExistsException e = assertThrows(EntityExistsException.class, ()->userService.createUser(email, name, password));
        assertEquals("An account with this email address already exists!", e.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void throwsExceptionIfPasswordIsInvalid() {
        String email = "test@example.com";
        String name = "Test User";
        String password = "pass";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        SecurityException e = assertThrows(SecurityException.class, ()->userService.createUser(email, name, password));
        assertEquals("Password must be at least 8 characters long, must contain at least one special character, one letter and one number!", e.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}