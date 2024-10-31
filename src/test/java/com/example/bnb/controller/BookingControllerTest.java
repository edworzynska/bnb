package com.example.bnb.controller;

import com.example.bnb.model.Booking;
import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.BookingRepository;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import com.example.bnb.service.SpaceAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpaceController spaceController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    @Autowired
    private SpaceAvailabilityService spaceAvailabilityService;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userBooking;
    private User userBooking2;
    private Space testSpaceBooking1;
    private SpaceAvailability testSpaceAvailabilityBooking1;
    private SpaceAvailability testSpaceAvailabilityBooking2;
    private LocalDate localDate1;
    private LocalDate localDate2;

    @BeforeEach
    void setUp() {
        localDate1 = LocalDate.of(2025, 5, 1);
        localDate2 = LocalDate.of(2025, 5, 2);

        userBooking = new User();
        userBooking.setName("Test User");
        userBooking.setEmail("test@email.com");
        userBooking.setPassword(passwordEncoder.encode("testpassword1!"));
        userRepository.save(userBooking);

        userBooking2 = new User();
        userBooking2.setName("New User");
        userBooking2.setEmail("newuser@users.com");
        userBooking2.setPassword(passwordEncoder.encode("newpassword1!"));
        userRepository.save(userBooking2);

        testSpaceBooking1 = new Space();
        testSpaceBooking1.setUser(userBooking);
        testSpaceBooking1.setDescription("test");
        testSpaceBooking1.setPricePerNight(new BigDecimal("70"));
        spaceRepository.save(testSpaceBooking1);
        userBooking.setSpaces(List.of(testSpaceBooking1));

        testSpaceAvailabilityBooking1 = new SpaceAvailability();
        testSpaceAvailabilityBooking1.setSpace(testSpaceBooking1);
        testSpaceAvailabilityBooking1.setIsAvailable(true);
        testSpaceAvailabilityBooking1.setDate(localDate1);
        spaceAvailabilityRepository.save(testSpaceAvailabilityBooking1);

        testSpaceAvailabilityBooking2 = new SpaceAvailability();
        testSpaceAvailabilityBooking2.setSpace(testSpaceBooking1);
        testSpaceAvailabilityBooking2.setIsAvailable(true);
        testSpaceAvailabilityBooking2.setDate(localDate2);
        spaceAvailabilityRepository.save(testSpaceAvailabilityBooking2);

        testSpaceBooking1.setSpaceAvailabilities(List.of(testSpaceAvailabilityBooking1, testSpaceAvailabilityBooking2));
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsErrorWhileCreatingNewBookingAsOwner() throws Exception {
        Long id = testSpaceBooking1.getId();
        mockMvc.perform(post("/spaces/{id}/request-booking", id)
                .param("startDate", "5/1/25")
                .param("endDate", "5/2/25"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Unable to request a booking in owned space!"));
    }
    @Test
    @WithUserDetails(value = "newuser@users.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createsNewBookingAsLoggedUser() throws Exception {
        Long id = testSpaceBooking1.getId();
        mockMvc.perform(post("/spaces/{id}/request-booking", id)
                        .param("startDate", "5/1/25")
                        .param("endDate", "5/2/25"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Booking requested successfully! Please wait for approval."));
    }

    @Test
    @WithAnonymousUser
    void redirectsAnonymousUser() throws Exception{
        Long id = testSpaceBooking1.getId();
        mockMvc.perform(post("/spaces/{id}/request-booking", id)
                .param("startDate", "5/1/25")
                .param("endDate", "5/2/25"))
                .andExpect(status().is3xxRedirection());
    }
    @Test
    @WithUserDetails(value = "newuser@users.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsErrorWhileCreatingNewBookingWithDatesInThePast() throws Exception {
        Long id = testSpaceBooking1.getId();
        mockMvc.perform(post("/spaces/{id}/request-booking", id)
                        .param("startDate", "5/1/24")
                        .param("endDate", "5/2/24"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please select valid dates!"));
    }
    @Test
    @WithUserDetails(value = "newuser@users.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsErrorWhileCreatingNewBookingWithUnavailableDates() throws Exception {
        Long id = testSpaceBooking1.getId();
        mockMvc.perform(post("/spaces/{id}/request-booking", id)
                        .param("startDate", "5/1/25")
                        .param("endDate", "5/7/25"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Unable to create booking: space is not available in requested dates."));
    }
    @Test
    @WithUserDetails(value = "newuser@users.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsErrorWhileCreatingNewBookingWithBookedDates() throws Exception {
        Long id = testSpaceBooking1.getId();
        spaceAvailabilityService.setUnavailable(id, List.of(LocalDate.of(2025, 5, 1)));
        mockMvc.perform(post("/spaces/{id}/request-booking", id)
                        .param("startDate", "5/1/25")
                        .param("endDate", "5/2/25"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Unable to create booking: space is not available in requested dates."));
    }
    @Test
    @WithUserDetails(value = "newuser@users.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void returnsErrorWhileCreatingNewBookingWithBookedDates2() throws Exception {
        Long id = testSpaceBooking1.getId();
        spaceAvailabilityService.setUnavailable(id, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        mockMvc.perform(post("/spaces/{id}/request-booking", id)
                        .param("startDate", "5/1/25")
                        .param("endDate", "5/2/25"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Unable to create booking: space is not available in requested dates."));
    }

    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void throwsMessageAndDeniesIfSpaceOwnerApprovesBookingInUnavailableDates() throws Exception {
        Booking booking = new Booking(testSpaceBooking1, userBooking, LocalDate.of(2025,10,1));
        Booking booking1 = new Booking(testSpaceBooking1, userBooking, LocalDate.of(2025,10,2));
        bookingRepository.save(booking);
        bookingRepository.save(booking1);
        Long idBooking = booking.getId();
        Long idBooking1 = booking1.getId();
        Long id = testSpaceBooking1.getId();

        mockMvc.perform(post("/spaces/{id}/bookings/approve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("[%s, %s]", idBooking, idBooking1)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Unable to approve the request - space is not available in selected dates."));
    }
    @Test
    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void spaceOwnerApprovesBooking() throws Exception {
        Booking booking = new Booking(testSpaceBooking1, userBooking, localDate1);
        Booking booking1 = new Booking(testSpaceBooking1, userBooking, localDate2);
        bookingRepository.save(booking);
        bookingRepository.save(booking1);
        Long idBooking = booking.getId();
        Long idBooking1 = booking1.getId();
        Long id = testSpaceBooking1.getId();

        mockMvc.perform(post("/spaces/{id}/bookings/approve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("[%s, %s]", idBooking, idBooking1)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bookings approved successfully!"));
    }
    @Test
    @WithUserDetails(value = "newuser@users.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void differentUserApprovesBooking() throws Exception {
        Booking booking = new Booking(testSpaceBooking1, userBooking, localDate1);
        Booking booking1 = new Booking(testSpaceBooking1, userBooking, localDate2);
        bookingRepository.save(booking);
        bookingRepository.save(booking1);
        Long idBooking = booking.getId();
        Long idBooking1 = booking1.getId();
        Long id = testSpaceBooking1.getId();

        mockMvc.perform(post("/spaces/{id}/bookings/approve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("[%s, %s]", idBooking, idBooking1)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Access denied!"));
    }

    @Test
    @WithAnonymousUser
    void redirectsAnonymousUserWhoTriesToAccessApprovalEndpoint() throws Exception {
        Booking booking = new Booking(testSpaceBooking1, userBooking, localDate1);
        Booking booking1 = new Booking(testSpaceBooking1, userBooking, localDate2);
        bookingRepository.save(booking);
        bookingRepository.save(booking1);
        Long idBooking = booking.getId();
        Long idBooking1 = booking1.getId();
        Long id = testSpaceBooking1.getId();

        mockMvc.perform(post("/spaces/{id}/bookings/approve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("[%s, %s]", idBooking, idBooking1)))
                .andExpect(status().is3xxRedirection());
    }
}