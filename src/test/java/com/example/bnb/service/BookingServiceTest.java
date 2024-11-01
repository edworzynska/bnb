package com.example.bnb.service;

import com.example.bnb.model.Booking;
import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.BookingRepository;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SpaceAvailabilityService spaceAvailabilityService;

    @InjectMocks
    private BookingService bookingService;

    private User mockedUser;
    private Space mockedSpace;
    private SpaceAvailability mockedSpaceAvailability;

    @BeforeEach
    void setUp() {
        mockedUser = new User();
        mockedUser.setEmail("test@user.com");
        mockedUser.setId(1L);

        mockedSpace = new Space();
        mockedSpace.setId(1L);
        mockedSpace.setUser(mockedUser);
        mockedSpace.setPricePerNight(new BigDecimal("15"));
        mockedSpace.setDescription("Test description");

        mockedSpaceAvailability = new SpaceAvailability();
        mockedSpaceAvailability.setSpace(mockedSpace);
        mockedSpaceAvailability.setIsAvailable(true);
        mockedSpaceAvailability.setId(1L);
        mockedSpaceAvailability.setDate(LocalDate.of(2025, 10, 1));

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createsBooking() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(mockedSpace));
        when(spaceAvailabilityService.isSpaceAvailableInDates(1L, List.of(LocalDate.of(2025, 10, 1)))).thenReturn(true);

        var result = bookingService.createBooking(1L, "test@user.com", List.of(LocalDate.of(2025, 10, 1)));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockedUser, result.get(0).getUser());
        assertEquals(mockedSpace, result.get(0).getSpace());

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void throwsEntityNotFoundExceptionIfUserNotFound() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.empty());
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(mockedSpace));

        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()-> bookingService.createBooking(1L, "test@user.com", List.of(LocalDate.of(2025, 5, 1))));
        assertEquals("User doesn't exist!", e.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
    @Test
    void throwsEntityNotFoundExceptionIfSpaceNotFound() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        when(spaceRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()-> bookingService.createBooking(1L, "test@user.com", List.of(LocalDate.of(2025, 5, 1))));
        assertEquals("Space not found!", e.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
    @Test
    void throwsInvalidParameterExceptionIfDatesAreEmpty() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(mockedSpace));
        when(spaceAvailabilityService.isSpaceAvailableInDates(1L, List.of())).thenReturn(true);

        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()-> bookingService.createBooking(1L, "test@user.com", List.of()));
        assertEquals("Please select dates!", e.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }


}
