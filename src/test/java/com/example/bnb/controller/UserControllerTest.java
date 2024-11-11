package com.example.bnb.controller;

import com.example.bnb.configuration.EmailService;
import com.example.bnb.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private UserController userController;

    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsUser() throws Exception {
        mockMvc.perform(post("/register").param("email", "test@email.com")
                .param("name", "Test Name")
                .param("password", "Password1!"))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully! You can sign in."));

        verify(emailService, times(1)).registrationEmail(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void throwsMissingParametersExceptionIfParamsAreMissing() throws Exception{
        mockMvc.perform(post("/register").param("email", "test@email.com")
                        .param("name", "Test Name"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Missing parameter: password"));
    }

    @Test
    void throwsInvalidParameterExceptionIfNameIsEmpty() throws Exception {
        mockMvc.perform(post("/register").param("email", "test@email.com")
                        .param("name", "")
                        .param("password", "Password!1"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Name cannot be empty!"));
    }
    @Test
    void throwsInvalidParameterExceptionIfEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/register").param("email", "test@")
                        .param("name", "Test Name")
                        .param("password", "Password!1"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Provided email address is invalid!"));
    }
    @Test
    void throwsSecurityExceptionIfPasswordIsInvalid() throws Exception {
        mockMvc.perform(post("/register").param("email", "test@test.com")
                        .param("name", "Test Name")
                        .param("password", "Pass"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Password must be at least 8 characters long, must contain at least one special character, one letter and one number!"));
    }

    @Test
    void throwsEntityExistExceptionIfEmailIsInUse() throws Exception{
        mockMvc.perform(post("/register").param("email", "test@test.com")
                        .param("name", "Test Name")
                        .param("password", "Passwor1!"));

        mockMvc.perform(post("/register").param("email", "test@test.com")
                        .param("name", "Test Name2")
                        .param("password", "Passworrr12!"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("An account with this email address already exists!"));
    }
}