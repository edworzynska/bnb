package com.example.bnb.controller;

import com.example.bnb.model.Space;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpaceController spaceController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private User user2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("test@email.com");
        user.setPassword(passwordEncoder.encode("testpassword1!"));
        userRepository.save(user);

        user2 = new User();
        user2.setName("New User");
        user2.setEmail("newuser@users.com");
        user2.setPassword(passwordEncoder.encode("newpassword1!"));
        userRepository.save(user2);
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addsSpaceWithLoggedUser() throws Exception {
        mockMvc.perform(post("/add-space")
                .param("description", "Test Description")
                .param("price", "40"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Space posted successfully!"));
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsRedirectedToLoginPage() throws Exception {
        mockMvc.perform(post("/add-space")
                        .param("description", "Test Description")
                        .param("price", "40"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("http://localhost/login"));
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfDescriptionIsEmpty() throws Exception {
        mockMvc.perform(post("/add-space")
                        .param("description", "")
                        .param("price", "40"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Description cannot be empty!"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfDescriptionIsBlank() throws Exception {
        mockMvc.perform(post("/add-space")
                        .param("description", "      ")
                        .param("price", "40"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Description cannot be empty!"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfPriceIsNegative() throws Exception {
        mockMvc.perform(post("/add-space")
                        .param("description", "Content")
                        .param("price", "-2"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Price has to be a positive number!"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfPriceIsZero() throws Exception {
        mockMvc.perform(post("/add-space")
                        .param("description", "Content")
                        .param("price", "0"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Price has to be a positive number!"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsMissingParameterExceptionIfPriceIsEmpty() throws Exception {
        mockMvc.perform(post("/add-space")
                        .param("description", "Content")
                        .param("price", ""))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Missing parameter: price"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsMissingParameterExceptionIfDescriptionParameterIsMissing() throws Exception {
        mockMvc.perform(post("/add-space")
                        .param("price", "20"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Missing parameter: description"));
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void viewsSpaceAsLoggedUser() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();
        mockMvc.perform(get("/spaces/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect((MockMvcResultMatchers.jsonPath("$.description").value("Test")))
                .andExpect((MockMvcResultMatchers.jsonPath("$.ownerName").value("Test User")));
    }

    @Test
    @WithAnonymousUser
    void redirectsAnonymousUserWhileViewingSpace() throws Exception{
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();
        mockMvc.perform(get("/spaces/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsExceptionIfSpaceDoesntExistWhileViewing() throws Exception{
        mockMvc.perform(get("/spaces/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Space not found!"));
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addsAvailabilityToSpaceOwnedByUser() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        // local date supported format is MM/DD/YY
        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                .param("startDate", "12/1/24")
                .param("endDate", "12/10/24"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Availability added successfully!"));
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void loggedUserIsNotTheOwnerOfTheSpaceReturnsAccessDeniedMessage() throws Exception {
        Space space2 = new Space(user2, "Test", new BigDecimal(40));
        spaceRepository.save(space2);
        var id = space2.getId();

        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                .param("startDate", "12/1/24")
                .param("endDate", "12/10/24"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Access denied."));
    }

    @Test
    @WithAnonymousUser
    void getsRedirectedIfTryingToAddAvailabilityAsAnonymousUser() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                .param("startDate", "12/1/24")
                .param("endDate", "12/10/24"))
                .andExpect(status().is3xxRedirection());
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsExceptionIfEndDateIsMissing () throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        // local date supported format is MM/DD/YY
        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                        .param("startDate", "12/1/24"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Missing parameter: endDate"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsExceptionIfStartDateIsMissing () throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        // local date supported format is MM/DD/YY
        mockMvc.perform(post("/spaces/{id}/add-availability", id))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Missing parameter: startDate"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsMessageIfStartDateIsInThePast() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        // local date supported format is MM/DD/YY
        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                        .param("startDate", "08/1/24")
                        .param("endDate", "12/10/24"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please select valid dates!"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsMessageIfEndDateIsInThePast() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        // local date supported format is MM/DD/YY
        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                        .param("startDate", "12/1/24")
                        .param("endDate", "07/10/24"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please select valid dates!"));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsMessageIfBothDatesAreInThePast() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        // local date supported format is MM/DD/YY
        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                        .param("startDate", "08/1/24")
                        .param("endDate", "08/10/24"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please select valid dates!"));
    }
}