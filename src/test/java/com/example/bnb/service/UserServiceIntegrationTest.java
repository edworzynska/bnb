package com.example.bnb.service;

import com.example.bnb.model.User;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsUser() {
        User test = userService.createUser("email@email.com", "Test Name", "passsword1!");
        assertNotNull(test);
        assertTrue(userRepository.existsById(test.getId()));
        assertEquals("Test Name", test.getName());
    }

    @Test
    void throwsAnErrorIfCreatingUserWithInvalidEmailAddress() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->userService.createUser("email", "Test Name", "Password55#"));
        assertEquals("Provided email address is invalid!", e.getMessage());
    }
    @Test
    void throwsAnErrorIfCreatingUserWithInvalidEmailAddress2() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->userService.createUser("e  mail@email.com", "Test Name", "Password55#"));
        assertEquals("Provided email address is invalid!", e.getMessage());
    }

    @Test
    void throwsAnErrorIfEmailAddressAlreadyInUse() {
        User user = userService.createUser("email@email.com", "Test Name", "passsword1!");
        EntityExistsException e = assertThrows(EntityExistsException.class, ()->userService.createUser("email@email.com", "Test User", "passsword1!"));
        assertEquals("An account with this email address already exists!", e.getMessage());
    }

    @Test
    void throwsAnErrorIfPasswordIsEmpty() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()-> userService.createUser("", "Test User", "pass"));
        assertEquals("Provided email address is invalid!", e.getMessage());
    }

    @Test
    void throwsAnErrorIfPasswordIsInvalid1() {
        SecurityException e = assertThrows(SecurityException.class, ()-> userService.createUser("email@email.com", "Test User", "pass"));
        assertEquals("Password must be at least 8 characters long, must contain at least one special character, one letter and one number!", e.getMessage());
    }
    @Test
    void throwsAnErrorIfPasswordIsInvalid2() {
        SecurityException e = assertThrows(SecurityException.class, ()-> userService.createUser("email@email.com", "Test User", "passwordd"));
        assertEquals("Password must be at least 8 characters long, must contain at least one special character, one letter and one number!", e.getMessage());
    }
    @Test
    void throwsAnErrorIfPasswordIsInvalid3() {
        SecurityException e = assertThrows(SecurityException.class, ()-> userService.createUser("email@email.com", "Test User", "11111111"));
        assertEquals("Password must be at least 8 characters long, must contain at least one special character, one letter and one number!", e.getMessage());
    }
    @Test
    void throwsAnErrorIfPasswordIsInvalid4() {
        SecurityException e = assertThrows(SecurityException.class, ()-> userService.createUser("email@email.com", "Test User", "$$$$$$$$"));
        assertEquals("Password must be at least 8 characters long, must contain at least one special character, one letter and one number!", e.getMessage());
    }

    @Test
    void throwsAnErrorIfCreatingUserWithEmptyName() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()-> userService.createUser("email@email.com", "", "$$$Pass1$$$$$"));
        assertEquals("Name cannot be empty!", e.getMessage());

    }
}