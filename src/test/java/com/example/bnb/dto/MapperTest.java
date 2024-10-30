package com.example.bnb.dto;

import com.example.bnb.model.Space;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MapperTest {

    private SpaceDTO spaceDTO;
    private Space space;
    private User user;

    @Autowired
    private Mapper mapper;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test Name");
        user.setEmail("test@email.com");
        user.setPassword("TestPassword!@");
        userRepository.save(user);

        space = new Space();
        space.setUser(user);
        space.setDescription("Short description");
        space.setPricePerNight(new BigDecimal("17"));
        spaceRepository.save(space);
    }

    @Test
    void transfersSpaceToDTO() {
        spaceDTO = mapper.spaceToDTO(space);
        assertNotNull(spaceDTO);
        assertEquals(space.getId(), spaceDTO.getSpaceId());
        assertEquals(space.getDescription(), spaceDTO.getDescription());
        assertEquals(space.getUser().getName(), spaceDTO.getOwnerName());
    }

    @Test
    void transfersSpaceDTOtoSpace() {
        spaceDTO = mapper.spaceToDTO(space);
        Space space2 = mapper.dtoToSpace(spaceDTO);

        assertNotNull(space2);
        assertEquals(space.getId(), space2.getId());
        assertEquals(space.getDescription(), space2.getDescription());
        assertEquals(space.getPricePerNight(), space2.getPricePerNight());
        assertEquals(space, space2);
    }
}