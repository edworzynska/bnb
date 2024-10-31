package com.example.bnb.service;

import com.example.bnb.model.Space;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SpaceServiceIntegrationTest {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    private User user1;
    private Long user1Id;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail("user1@email.com");
        user1.setName("Test User");
        user1.setPassword("user1pass@");
        userRepository.save(user1);
        user1Id = user1.getId();
    }

    @Test
    void createsSpace() {
        Space space = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        assertNotNull(space);
        assertEquals(user1Id, space.getUser().getId());
        assertEquals(new BigDecimal("25"), space.getPricePerNight());
    }

    @Test
    void throwsEntityNotFoundErrorIfUserDoesntExist() {
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()->spaceService.createSpace("user2@email.com", "test description", new BigDecimal("40")));
        assertEquals("User doesn't exist!", e.getMessage());
    }

    @Test
    void throwsInvalidParameterExceptionIfDescriptionIsEmpty() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("user1@email.com", "", new BigDecimal("40")));
        assertEquals("Description cannot be empty!", e.getMessage());
    }
    @Test
    void throwsInvalidParameterExceptionIfDescriptionIsBlank() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("user1@email.com", "     ", new BigDecimal("40")));
        assertEquals("Description cannot be empty!", e.getMessage());
    }
    @Test
    void throwsInvalidParameterExceptionIfPriceIsNegative() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("user1@email.com", "Test description", new BigDecimal("-7")));
        assertEquals("Price has to be a positive number!", e.getMessage());
    }
    @Test
    void throwsInvalidParameterExceptionIfPriceIsZero() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, ()->spaceService.createSpace("user1@email.com", "Test description", new BigDecimal("0")));
        assertEquals("Price has to be a positive number!", e.getMessage());
    }

    @Test
    void findsSpaceById() {
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        Long id = space1.getId();
        Space space2 = spaceService.getSpace(id);
        assertEquals(id, space2.getId());
        assertEquals(space1.getDescription(), space2.getDescription());
        assertEquals(space1.getUser().getId(), space2.getUser().getId());
    }

    @Test
    void throwsEntityNotFoundExceptionIfNotFoundById() {
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()-> spaceService.getSpace(100L));
        assertEquals("Space not found!", e.getMessage());
    }

    @Test
    void addsAvailabilityToCreatedSpace() {
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        Long spaceId = space1.getId();
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDate date2 = LocalDate.of(2025, 1, 2);
        LocalDate date3 = LocalDate.of(2025, 1, 3);

        List<LocalDate> dates = List.of(date1,date2,date3);
        spaceService.addAvailability(spaceId, dates);
        var result = space1.getSpaceAvailabilities();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void throwsEntityNotFoundExceptionIfAddingAvailabilityToNotExistingSpace() {
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDate date2 = LocalDate.of(2025, 1, 2);
        List<LocalDate> dates = List.of(date1,date2);
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> spaceService.addAvailability(200L, dates));
        assertEquals("Space not found!", e.getMessage());
    }

    @Test
    void throwsInvalidParameterExceptionIfListOfDatesIsEmpty() {
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        Long spaceId = space1.getId();
        List<LocalDate> dates = new ArrayList<>();

        InvalidParameterException e = assertThrows(InvalidParameterException.class, () -> spaceService.addAvailability(spaceId, dates));
        assertEquals("Please select dates!", e.getMessage());
    }

    @Test
    void throwsEntityNotFoundExceptionIfNoSpacesFound() {
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, ()-> spaceService.getAll());
        assertEquals("Spaces not found!", e.getMessage());
    }

    @Test
    void returnsListOfAllSpaces() {
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        Space space2 = spaceService.createSpace("user1@email.com", "test description 2", new BigDecimal("40"));
        var result = spaceService.getAll();
        assertEquals(2, result.size());
        assertEquals(space1.getUser(), result.get(0).getUser());
        assertEquals(space2.getUser(), result.get(1).getUser());
        assertEquals(space1.getDescription(), result.get(0).getDescription());
        assertEquals(space2.getDescription(), result.get(1).getDescription());
    }

    @Test
    void returnsListOfSpacesWithAvailability() {
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        Long spaceId = space1.getId();
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDate date2 = LocalDate.of(2025, 1, 2);
        LocalDate date3 = LocalDate.of(2025, 1, 3);

        List<LocalDate> dates = List.of(date1,date2,date3);
        spaceService.addAvailability(spaceId, dates);

        Space space2 = spaceService.createSpace("user1@email.com", "test description 2", new BigDecimal("25"));
        Long space2Id = space2.getId();
        LocalDate date11 = LocalDate.of(2025, 1, 1);
        LocalDate date22 = LocalDate.of(2025, 1, 2);
        LocalDate date33 = LocalDate.of(2025, 1, 6);

        List<LocalDate> dates2 = List.of(date11,date22,date33);
        spaceService.addAvailability(space2Id, dates2);

        Space space3 = spaceService.createSpace("user1@email.com", "test description 3", new BigDecimal("25"));
        Long space3Id = space3.getId();

        var result = spaceService.findAvailable();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of(space1, space2), result);
        assertEquals(date1, result.get(0).getSpaceAvailabilities().get(0).getDate());

    }

    @Test
    void throwsEntityNotFoundExceptionIfNoAvailableSpacesAreFound(){
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> spaceService.findAvailable());
        assertEquals("No available spaces found!", e.getMessage());
    }

    @Test
    void returnsAvailableSpacesInProvidedDateRange() {
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        Long spaceId = space1.getId();
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDate date2 = LocalDate.of(2025, 1, 2);
        LocalDate date3 = LocalDate.of(2025, 1, 3);

        List<LocalDate> dates = List.of(date1,date2,date3);
        spaceService.addAvailability(spaceId, dates);

        Space space2 = spaceService.createSpace("user1@email.com", "test description 2", new BigDecimal("25"));
        Long space2Id = space2.getId();
        LocalDate date11 = LocalDate.of(2025, 2, 1);
        LocalDate date22 = LocalDate.of(2025, 2, 2);
        LocalDate date33 = LocalDate.of(2025, 2, 6);

        List<LocalDate> dates2 = List.of(date11,date22,date33);
        spaceService.addAvailability(space2Id, dates2);

        var result = spaceService.findAvailable(List.of(date11, date22));
        assertNotNull(result);
        assertEquals(List.of(space2), result);
    }

    @Test
    void throwsEntityNotFoundExceptionIfNoSpacesFoundInRequestedDateRange() {
        Space space1 = spaceService.createSpace("user1@email.com", "test description", new BigDecimal("25"));
        Long spaceId = space1.getId();
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDate date2 = LocalDate.of(2025, 1, 2);
        LocalDate date3 = LocalDate.of(2025, 1, 3);

        List<LocalDate> dates = List.of(date1,date2,date3);
        spaceService.addAvailability(spaceId, dates);

        Space space2 = spaceService.createSpace("user1@email.com", "test description 2", new BigDecimal("25"));
        Long space2Id = space2.getId();
        LocalDate date11 = LocalDate.of(2025, 2, 1);

        List<LocalDate> dates2 = List.of(date11);
        spaceService.addAvailability(space2Id, dates2);

        LocalDate date22 = LocalDate.of(2025, 2, 2);
        LocalDate date33 = LocalDate.of(2025, 2, 6);

        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> spaceService.findAvailable(List.of(date22, date33)));
        assertEquals("No available spaces found in the selected date range!", e.getMessage());
    }
}