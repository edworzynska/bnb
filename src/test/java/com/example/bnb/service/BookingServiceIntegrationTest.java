package com.example.bnb.service;

import com.example.bnb.model.*;
import com.example.bnb.repository.BookingRepository;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;

import static com.example.bnb.model.BookingStatus.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SpaceAvailabilityService spaceAvailabilityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    private User user;
    private Space space;
    private SpaceAvailability spaceAvailability;
    private SpaceAvailability spaceAvailability2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("testuser@email.com");
        user.setPassword("testpassword!1");
        userRepository.save(user);

        space = new Space();
        space.setUser(user);
        space.setDescription("test description");
        space.setPricePerNight(new BigDecimal("70"));
        spaceRepository.save(space);
        user.setSpaces(List.of(space));

        spaceAvailability = new SpaceAvailability();
        spaceAvailability.setSpace(space);
        spaceAvailability.setDate(LocalDate.of(2025, 5, 1));
        spaceAvailability.setIsAvailable(true);
        spaceAvailabilityRepository.save(spaceAvailability);

        spaceAvailability2 = new SpaceAvailability();
        spaceAvailability2.setSpace(space);
        spaceAvailability2.setDate(LocalDate.of(2025, 5, 2));
        spaceAvailability2.setIsAvailable(true);
        spaceAvailabilityRepository.save(spaceAvailability2);

        space.setSpaceAvailabilities(List.of(spaceAvailability, spaceAvailability2));

    }

    @Test
    void createsBooking() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();

        var result = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1)));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PENDING, result.get(0).getBookingStatus());
        assertEquals(LocalDate.of(2025, 5, 1), result.get(0).getDate());
        assertEquals(space, result.get(0).getSpace());
    }

    @Test
    void throwsEntityNotFoundExceptionIfUserNotFound() {
        Long spaceId = space.getId();
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()-> bookingService.createBooking(spaceId, "nouser@user.com", List.of(LocalDate.of(2025, 5, 1))));
        assertEquals("User doesn't exist!", e.getMessage());
    }
    @Test
    void throwsEntityNotFoundExceptionIfSpaceNotFound() {
        Long spaceId = 999L;
        String userEmail = user.getEmail();
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()-> bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1))));
        assertEquals("Space not found!", e.getMessage());
    }
    @Test
    void throwsInvalidParameterExceptionIfDatesAreEmpty() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()-> bookingService.createBooking(spaceId, userEmail, List.of()));
        assertEquals("Please select dates!", e.getMessage());
    }

    @Test
    void throwsCannotCreateTransactionExceptionIfSpaceIsNotAvailableInDateRange() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        CannotCreateTransactionException e = assertThrows(CannotCreateTransactionException.class, ()-> bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 20))));
        assertEquals("Unable to create booking: space is not available in requested dates.", e.getMessage());
    }
    @Test
    void throwsCannotCreateTransactionExceptionIfSpaceIsNotAvailableInDateRange2() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        CannotCreateTransactionException e = assertThrows(CannotCreateTransactionException.class, ()-> bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 2),LocalDate.of(2025, 5, 3))));
        assertEquals("Unable to create booking: space is not available in requested dates.", e.getMessage());
    }

    @Test
    void throwsCannotCreateTransactionExceptionIfSpaceAlreadyBooked() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        spaceAvailabilityService.setUnavailable(spaceId, List.of(LocalDate.of(2025, 5, 1),LocalDate.of(2025, 5, 2)));
        CannotCreateTransactionException e = assertThrows(CannotCreateTransactionException.class, ()-> bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1),LocalDate.of(2025, 5, 2))));
        assertEquals("Unable to create booking: space is not available in requested dates.", e.getMessage());
    }
    @Test
    void throwsCannotCreateTransactionExceptionIfSpaceAlreadyBooked2() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        spaceAvailabilityService.setUnavailable(spaceId, List.of(LocalDate.of(2025, 5, 1)));
        CannotCreateTransactionException e = assertThrows(CannotCreateTransactionException.class, ()-> bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1),LocalDate.of(2025, 5, 2))));
        assertEquals("Unable to create booking: space is not available in requested dates.", e.getMessage());
    }

    @Test
    void approvesBookings() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());

        bookingService.approveBookings(spaceId, bookingsIds);
        assertEquals(BookingStatus.APPROVED, booking.get(0).getBookingStatus());
    }
    @Test
    void approvesBookings2() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());

        bookingService.approveBookings(spaceId, bookingsIds);
        assertEquals(BookingStatus.APPROVED, booking.get(0).getBookingStatus());
        assertEquals(BookingStatus.APPROVED, booking.get(1).getBookingStatus());
    }
    @Test
    void approvesOneBookingFromTwo() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());

        bookingService.approveBookings(spaceId, List.of(bookingsIds.get(0)));
        assertEquals(BookingStatus.APPROVED, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());
    }


    @Test
    void throwsCannotCreateTransactionExceptionIfSpaceIsAlreadyUnavailable() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());
        spaceAvailabilityService.setUnavailable(spaceId, List.of(LocalDate.of(2025, 5, 1)));

        CannotCreateTransactionException e = assertThrows(CannotCreateTransactionException.class, () -> bookingService.approveBookings(spaceId, bookingsIds));
        assertEquals("Unable to approve the request - space is not available in selected dates.", e.getMessage());
    }
    @Test
    void throwsCannotCreateTransactionExceptionIfSpaceIsUnavailableInSelectedDates() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());
        spaceAvailabilityService.setUnavailable(spaceId, List.of(LocalDate.of(2025, 5, 1)));

        CannotCreateTransactionException e = assertThrows(CannotCreateTransactionException.class, () -> bookingService.approveBookings(spaceId, bookingsIds));
        assertEquals("Unable to approve the request - space is not available in selected dates.", e.getMessage());
    }
    @Test
    void throwsCannotCreateTransactionExceptionIfDatesAreInPast() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());
        spaceAvailabilityService.setUnavailable(spaceId, List.of(LocalDate.of(2025, 5, 1)));

        CannotCreateTransactionException e = assertThrows(CannotCreateTransactionException.class, () -> bookingService.approveBookings(spaceId, bookingsIds));
        assertEquals("Unable to approve the request - space is not available in selected dates.", e.getMessage());
    }
    @Test
    void deniesBookings() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());

        bookingService.denyBookings(bookingsIds);
        assertEquals(BookingStatus.DENIED, booking.get(0).getBookingStatus());
    }
    @Test
    void deniesBookings2() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());

        bookingService.denyBookings(bookingsIds);
        assertEquals(BookingStatus.DENIED, booking.get(0).getBookingStatus());
        assertEquals(BookingStatus.DENIED, booking.get(1).getBookingStatus());
    }
    @Test
    void deniesOneBookingFromTwo() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(PENDING, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());

        bookingService.denyBookings(List.of(bookingsIds.get(0)));
        assertEquals(BookingStatus.DENIED, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());
    }
    @Test
    void throwsExceptionIfDenyingApprovedBooking() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        booking.get(0).setBookingStatus(APPROVED);
        bookingRepository.save(booking.get(0));

        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(APPROVED, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> bookingService.denyBookings(bookingsIds));
        assertEquals("Unable to change the status from APPROVED", e.getMessage());
    }
    @Test
    void throwsExceptionIfApprovingDeniedBooking() {
        Long spaceId = space.getId();
        String userEmail = user.getEmail();
        var booking = bookingService.createBooking(spaceId, userEmail, List.of(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2)));
        booking.get(0).setBookingStatus(DENIED);
        bookingRepository.save(booking.get(0));

        var bookingsIds = booking.stream().map(Booking::getId).toList();
        assertNotNull(booking);
        assertEquals(DENIED, booking.get(0).getBookingStatus());
        assertEquals(PENDING, booking.get(1).getBookingStatus());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> bookingService.approveBookings(spaceId, bookingsIds));
        assertEquals("Unable to change the status from DENIED", e.getMessage());
    }

}