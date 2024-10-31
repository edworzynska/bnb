package com.example.bnb.service;

import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpaceAvailability spaceAvailability;

    @Mock
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    @InjectMocks
    private SpaceService spaceService;

    private User mockedUser;
    private Space mockedSpace;
    private Space mockedSpace2;


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

        mockedSpace2 = new Space();
        mockedSpace2.setId(2L);
        mockedSpace2.setUser(mockedUser);
        mockedSpace2.setPricePerNight(new BigDecimal("20"));
        mockedSpace2.setDescription("Test description 2");

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createsNewSpace() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        Space testSpace = spaceService.createSpace("test@user.com", "Description", new BigDecimal("25"));
        assertNotNull(testSpace);
        assertEquals(1, testSpace.getUser().getId());
        assertEquals("Description", testSpace.getDescription());
        assertEquals(new BigDecimal("25"), testSpace.getPricePerNight());
        verify(spaceRepository, times(1)).save(testSpace);
    }

    @Test
    void throwsEntityNotFoundErrorIfUserDoesntExist() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.empty());
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()->spaceService.createSpace("test@user.com", "test description", new BigDecimal("40")));
        assertEquals("User doesn't exist!", e.getMessage());
        verify(spaceRepository, never()).save(any(Space.class));
    }
    @Test
    void throwsInvalidParameterExceptionIfDescriptionIsEmpty() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("test@user.com", "", new BigDecimal("40")));
        assertEquals("Description cannot be empty!", e.getMessage());
        verify(spaceRepository, never()).save(any(Space.class));
    }
    @Test
    void throwsInvalidParameterExceptionIfDescriptionIsBlank() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("test@user.com", "     ", new BigDecimal("40")));
        assertEquals("Description cannot be empty!", e.getMessage());
        verify(spaceRepository, never()).save(any(Space.class));
    }
    @Test
    void throwsInvalidParameterExceptionIfPriceIsNegative() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("test@user.com", "Test description", new BigDecimal("-7")));
        assertEquals("Price has to be a positive number!", e.getMessage());
        verify(spaceRepository, never()).save(any(Space.class));
    }
    @Test
    void throwsInvalidParameterExceptionIfPriceIsZero() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockedUser));
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("test@user.com", "Test description", new BigDecimal("0")));
        assertEquals("Price has to be a positive number!", e.getMessage());
        verify(spaceRepository, never()).save(any(Space.class));
    }

    @Test
    void findSpaceById() {
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(mockedSpace));
        Space space = spaceService.getSpace(1L);
        assertNotNull(space);
        assertEquals(1, space.getId());
        assertEquals(1, space.getUser().getId());
        assertEquals("test@user.com", space.getUser().getEmail());
        assertEquals("Test description", space.getDescription());
    }

    @Test
    void throwsEntityNotFoundExceptionIfUnableToFindSpaceById() {
        when(spaceRepository.findById(10L)).thenReturn(Optional.empty());
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()-> spaceService.getSpace(10L));
        assertEquals("Space not found!", e.getMessage());
    }

    @Test
    void addsAvailabilityToSpace() {
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(mockedSpace));
        List<LocalDate> dates = List.of(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3)
        );
        spaceService.addAvailability(1L, dates);
        verify(spaceAvailabilityRepository, times(3)).save(any(SpaceAvailability.class));
        assertEquals(3, mockedSpace.getSpaceAvailabilities().size());
        assertEquals(dates.get(0), mockedSpace.getSpaceAvailabilities().get(0).getDate());
        assertEquals(dates.get(1), mockedSpace.getSpaceAvailabilities().get(1).getDate());
    }

    @Test
    void throwsEntityNotFoundExceptionIfDoesntExist() {
        when(spaceRepository.findById(1L)).thenReturn(Optional.empty());
        List<LocalDate> dates = List.of(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3)
        );
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> spaceService.addAvailability(100L, dates));
        assertEquals("Space not found!", e.getMessage());
        verify(spaceAvailabilityRepository, never()).save(any(SpaceAvailability.class));
    }
    @Test
    void throwsInvalidParameterExceptionIfNoDatesSelected() {
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(mockedSpace));
        List<LocalDate> dates = new ArrayList<>();
        InvalidParameterException e = assertThrows(InvalidParameterException.class, () -> spaceService.addAvailability(1L, dates));
        assertEquals("Please select dates!", e.getMessage());
        verify(spaceAvailabilityRepository, never()).save(any(SpaceAvailability.class));
    }

    @Test
    void throwsEntityNotFoundExceptionIfListOfAllSpacesIsEmpty() {
        when(spaceRepository.findAll()).thenReturn(List.of());
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> spaceService.getAll());
        assertEquals("Spaces not found!", e.getMessage());
    }

    @Test
    void returnsListOfAllSpaces() {
        when(spaceRepository.findAll()).thenReturn(List.of(mockedSpace, mockedSpace2));
        List<Space> result = spaceService.getAll();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockedSpace.getDescription(), result.get(0).getDescription());
        assertEquals(mockedSpace2.getDescription(), result.get(1).getDescription());
    }

    @Test
    void returnsListOfAvailableSpaces() {

        when(spaceRepository.findAllAvailableSpaces()).thenReturn(List.of(mockedSpace, mockedSpace2));
        var result = spaceService.findAvailable();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(mockedSpace));
    }

    @Test
    void throwsErrorIfThereAreNoAvailableSpaces() {
        Space mockedSpace = mock(Space.class);
        Space mockedSpace2 = mock(Space.class);
        SpaceAvailability mockedSpaceAvailability1 = mock(SpaceAvailability.class);
        SpaceAvailability mockedSpaceAvailability2 = mock(SpaceAvailability.class);
        when(mockedSpaceAvailability1.getIsAvailable()).thenReturn(false);
        when(mockedSpaceAvailability2.getIsAvailable()).thenReturn(false);
        when(mockedSpace.getSpaceAvailabilities()).thenReturn(List.of());
        when(mockedSpace2.getSpaceAvailabilities()).thenReturn(List.of());
        when(spaceRepository.findAll()).thenReturn(List.of(mockedSpace, mockedSpace2));
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class,()-> spaceService.findAvailable());
        assertEquals("No available spaces found!", e.getMessage());
    }
}