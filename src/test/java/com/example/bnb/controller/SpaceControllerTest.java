package com.example.bnb.controller;

import com.example.bnb.configuration.EmailService;
import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @MockBean
    private EmailService emailService;

    @Autowired
    private SpaceController spaceController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private User user2;
    private Space testSpace1;
    private Space testSpace2;
    private SpaceAvailability testSpaceAvailability1;
    private SpaceAvailability testSpaceAvailability2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("bnbtestaddress@gmail.com");
        user.setPassword(passwordEncoder.encode("testpassword1!"));
        userRepository.save(user);

        user2 = new User();
        user2.setName("New User");
        user2.setEmail("bnb.test.address@gmail.com");
        user2.setPassword(passwordEncoder.encode("newpassword1!"));
        userRepository.save(user2);
    }

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addsSpaceWithLoggedUser() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                .param("description", "Test Description")
                .param("price", "40"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Space posted successfully!"));

        verify(emailService, times(1)).postingSpaceEmail(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsRedirectedToLoginPage() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                        .param("description", "Test Description")
                        .param("price", "40"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("http://localhost/login"));
    }

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfDescriptionIsEmpty() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                        .param("description", "")
                        .param("price", "40"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Description cannot be empty!"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfDescriptionIsBlank() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                        .param("description", "      ")
                        .param("price", "40"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Description cannot be empty!"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfPriceIsNegative() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                        .param("description", "Content")
                        .param("price", "-2"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Price has to be a positive number!"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsInvalidParameterExceptionIfPriceIsZero() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                        .param("description", "Content")
                        .param("price", "0"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Price has to be a positive number!"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsMissingParameterExceptionIfPriceIsEmpty() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                        .param("description", "Content")
                        .param("price", ""))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Missing parameter: price"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsMissingParameterExceptionIfDescriptionParameterIsMissing() throws Exception {
        mockMvc.perform(post("/spaces/add-space")
                        .param("price", "20"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Missing parameter: description"));
    }

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsExceptionIfSpaceDoesntExistWhileViewing() throws Exception{
        mockMvc.perform(get("/spaces/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Space not found!"));
    }

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

        verify(emailService, times(1)).spaceUpdateEmail(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsEntityNotFoundExceptionIfNoSpacesOnTheList() throws Exception {
        spaceRepository.deleteAll();
        mockMvc.perform(get("/spaces/all"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Spaces not found!"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsListOfAllSpacesDTOIfUsersLogged() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        Space space2 = new Space(user, "Test 2", new BigDecimal("80"));
        spaceRepository.save(space);
        spaceRepository.save(space2);
        mockMvc.perform(get("/spaces/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    @Test
    @WithAnonymousUser
    void redirectsUserIfViewingSpacesAsAnonymous() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        Space space2 = new Space(user, "Test 2", new BigDecimal("80"));
        spaceRepository.save(space);
        spaceRepository.save(space2);
        mockMvc.perform(get("/spaces/all"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""));
    }

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findsAllAvailableSpacesAsLoggedUser() throws Exception{
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                .param("startDate", "12/1/24")
                .param("endDate", "12/10/24"));

        mockMvc.perform(get("/spaces/find"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findsAllAvailableSpacesInTimeRangeAsLoggedUser() throws Exception{
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                .param("startDate", "12/01/24")
                .param("endDate", "12/10/24"));

        Space space2 = new Space(user, "Test 2", new BigDecimal("90"));
        spaceRepository.save(space2);
        Long id2 = space2.getId();

        mockMvc.perform(post("/spaces/{id2}/add-availability", id2)
                .param("startDate", "11/30/24")
                .param("endDate", "12/05/24"));

        mockMvc.perform(get("/spaces/find")
                        .param("startDate", "12/01/24")
                        .param("endDate", "12/04/24"))
                .andExpect(status().isFound())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsErrorMessageIfNoSpacesInTheRangeAreAvailable() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                .param("startDate", "12/01/24")
                .param("endDate", "12/10/24"));

        Space space2 = new Space(user, "Test 2", new BigDecimal("90"));
        spaceRepository.save(space2);
        Long id2 = space2.getId();

        mockMvc.perform(post("/spaces/{id2}/add-availability", id2)
                .param("startDate", "11/30/24")
                .param("endDate", "12/05/24"));

        mockMvc.perform(get("/spaces/find")
                        .param("startDate", "01/01/25")
                        .param("endDate", "02/01/25"))
                .andExpect(status().isConflict())
                .andExpect(content().string("No available spaces found in the selected date range!"));
    }
    @Test
    @WithUserDetails(value = "bnbtestaddress@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsErrorMessageIfDatesAreInThePast() throws Exception {
        Space space = new Space(user, "Test", new BigDecimal("70"));
        spaceRepository.save(space);
        Long id = space.getId();

        mockMvc.perform(post("/spaces/{id}/add-availability", id)
                .param("startDate", "12/01/24")
                .param("endDate", "12/10/24"));

        Space space2 = new Space(user, "Test 2", new BigDecimal("90"));
        spaceRepository.save(space2);
        Long id2 = space2.getId();

        mockMvc.perform(post("/spaces/{id2}/add-availability", id2)
                .param("startDate", "11/30/24")
                .param("endDate", "12/05/24"));

        mockMvc.perform(get("/spaces/find")
                        .param("startDate", "01/01/24")
                        .param("endDate", "02/01/24"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please select valid dates!"));
    }
}