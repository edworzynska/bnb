package com.example.bnb.dto;

import com.example.bnb.model.Space;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.Optional;

@Component
public class Mapper {

    private UserRepository userRepository;
    private SpaceRepository spaceRepository;

    @Autowired
    public Mapper(UserRepository userRepository, SpaceRepository spaceRepository){
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
    }

    public SpaceDTO spaceToDTO(Space space){
        Long userId = space.getUser().getId();
        String ownerName = space.getUser().getName();
        String description = space.getDescription();
        BigDecimal pricePerNight = space.getPricePerNight();

        return new SpaceDTO(userId, ownerName, description, pricePerNight);
    }
    public Space dtoToSpace(SpaceDTO spaceDTO){
        Long id = spaceDTO.getSpaceId();
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Space not found!"));
        return space;
    }
}
