package com.example.bnb.dto;

import com.example.bnb.model.BookingStatus;
import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.BookingRepository;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MapperTest {

    private SpaceDTO spaceDTO;
    private Space spaceMapper;
    private User userMapper;

    @Autowired
    private Mapper mapper;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;


    @BeforeEach
    void setUp() {
        userMapper = new User();
        userMapper.setName("Test Name in Mapper");
        userMapper.setEmail("test@mapper.com");
        userMapper.setPassword("TestPassword!@");
        userRepository.save(userMapper);

        spaceMapper = new Space();
        spaceMapper.setUser(userMapper);
        spaceMapper.setDescription("Short description in Mapper Class");
        spaceMapper.setPricePerNight(new BigDecimal("17"));
        spaceRepository.save(spaceMapper);
    }

    @Test
    void transfersSpaceToDTO() {
        spaceDTO = mapper.spaceToDTO(spaceMapper);
        assertNotNull(spaceDTO);
        assertEquals(spaceMapper.getId(), spaceDTO.getSpaceId());
        assertEquals(spaceMapper.getDescription(), spaceDTO.getDescription());
        assertEquals(spaceMapper.getUser().getName(), spaceDTO.getOwnerName());
    }

    @Test
    void transfersSpaceDTOtoSpace() {
        spaceDTO = mapper.spaceToDTO(spaceMapper);
        Space space2 = mapper.dtoToSpace(spaceDTO);

        assertNotNull(space2);
        assertEquals(spaceMapper.getId(), space2.getId());
        assertEquals(spaceMapper.getDescription(), space2.getDescription());
        assertEquals(spaceMapper.getPricePerNight(), space2.getPricePerNight());
        assertEquals(spaceMapper, space2);
    }

    @Test
    void transfersSpaceWithAvailabilitySetToDTO() {
        List<LocalDate> dateRange = LocalDate.of(2024, 12, 1).datesUntil(LocalDate.of(2024, 12, 10)).toList();
        for (LocalDate date : dateRange){
            SpaceAvailability spaceAvailability = new SpaceAvailability();
            spaceAvailability.setSpace(spaceMapper);
            spaceAvailability.setDate(date);
            spaceAvailability.setIsAvailable(true);
            spaceMapper.getSpaceAvailabilities().add(spaceAvailability);
            spaceAvailabilityRepository.save(spaceAvailability);
        }
        spaceDTO = mapper.spaceToDTO(spaceMapper);

        assertNotNull(spaceDTO);
        assertEquals(spaceMapper.getId(), spaceDTO.getSpaceId());
        assertEquals(spaceMapper.getDescription(), spaceDTO.getDescription());
        for (LocalDate date : dateRange){
            assertEquals(true, spaceDTO.getAvailableDates().get(date));
        }
    }
}