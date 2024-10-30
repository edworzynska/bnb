package com.example.bnb.controller;

import com.example.bnb.dto.Mapper;
import com.example.bnb.dto.SpaceDTO;
import com.example.bnb.model.Space;
import com.example.bnb.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private Mapper mapper;

    @Autowired
    public SpaceController(SpaceService spaceService, Mapper mapper) {
        this.spaceService = spaceService;
        this.mapper = mapper;
    }

    private String loggedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()){
            throw new SecurityException("Please log in to proceed.");
        }
        return authentication.getName();
    }

    @PostMapping("/add-space")
    public ResponseEntity<Object> addSpace
            (@RequestParam String description,
             @RequestParam BigDecimal price){
        String loggedUser = loggedUser();

        Space space = spaceService.createSpace(loggedUser, description, price);

        return new ResponseEntity<>("Space posted successfully!", HttpStatus.CREATED);
    }
    @GetMapping("/spaces/{id}")
    public ResponseEntity<Object> viewSpace
            (@PathVariable Long id){
        Space space = spaceService.getSpace(id);
        SpaceDTO spaceDTO = mapper.spaceToDTO(space);

        return new ResponseEntity<>(spaceDTO, HttpStatus.OK);
    }

    @PostMapping("/spaces/{id}/add-availability")
    public ResponseEntity<Object> addAvailability(
            @PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
            ){

        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now())){
            return new ResponseEntity<>("Please select valid dates!", HttpStatus.BAD_REQUEST);
        }

        List<LocalDate> dates = startDate.datesUntil(endDate).toList();
        String loggedUser = loggedUser();
        var spaceOwner = spaceService.getSpace(id).getUser().getEmail();

        if (loggedUser.equals(spaceOwner)){
            spaceService.addAvailability(id, dates);
            return new ResponseEntity<>("Availability added successfully!", HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>("Access denied.", HttpStatus.UNAUTHORIZED);
        }
    }
}
