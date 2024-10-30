package com.example.bnb.service;

import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    @Autowired
    public SpaceService(SpaceRepository spaceRepository, UserRepository userRepository,
                        SpaceAvailabilityRepository spaceAvailabilityRepository) {
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
        this.spaceAvailabilityRepository = spaceAvailabilityRepository;
    }

    @Transactional
    public Space createSpace(String email, String description, BigDecimal pricePerNight){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User doesn't exist!"));

        Space space = new Space();
        space.setUser(user);
        space.setDescription(description);
        space.setPricePerNight(pricePerNight);
        spaceRepository.save(space);

        return space;
    }
    public Space getSpace(Long id){
        return spaceRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Space not found!"));
    }

    @Transactional
    public void addAvailability(Long spaceId, List<LocalDate> dates){
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new EntityNotFoundException("Space not found!"));
        if (dates.isEmpty()){
            throw new InvalidParameterException("Please select dates!");
        }
        for (LocalDate date : dates){
            SpaceAvailability spaceAvailability = new SpaceAvailability();
            spaceAvailability.setSpace(space);
            spaceAvailability.setDate(date);
            spaceAvailability.setIsAvailable(true);
            space.getSpaceAvailabilities().add(spaceAvailability);
            spaceAvailabilityRepository.save(spaceAvailability);
        }
    }
}
