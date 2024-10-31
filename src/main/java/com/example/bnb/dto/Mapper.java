package com.example.bnb.dto;

import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Component
public class Mapper {

    private UserRepository userRepository;
    private SpaceRepository spaceRepository;
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    @Autowired
    public Mapper(UserRepository userRepository, SpaceRepository spaceRepository,
                  SpaceAvailabilityRepository spaceAvailabilityRepository){
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.spaceAvailabilityRepository = spaceAvailabilityRepository;
    }

    public SpaceDTO spaceToDTO(Space space){
        Long userId = space.getUser().getId();
        String ownerName = space.getUser().getName();
        String description = space.getDescription();
        BigDecimal pricePerNight = space.getPricePerNight();
        Map<LocalDate, Boolean> availability = new TreeMap<>();

        for (SpaceAvailability s : space.getSpaceAvailabilities()){
            availability.put(s.getDate(), s.getIsAvailable());
        }
        return new SpaceDTO(userId, ownerName, description, pricePerNight, availability);
    }

    public Space dtoToSpace(SpaceDTO spaceDTO){
        Long id = spaceDTO.getSpaceId();
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Space not found!"));
        return space;
    }
}
