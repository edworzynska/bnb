package com.example.bnb.controller;

import com.example.bnb.configuration.EmailService;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/spaces") //plural - good
public class SpaceController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private Mapper mapper;

    @Autowired//this is not needed here
    public SpaceController(SpaceService spaceService, Mapper mapper) { //you should not need this constructor here as everything is autowired? 
        this.spaceService = spaceService;
        this.mapper = mapper;
    }

    private String loggedUser(){//this should be reused outside of this class
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()){
            throw new SecurityException("Please log in to proceed.");
        }
        return authentication.getName();
    }

    @PostMapping("/add-space") // bad - use HTTP verbs instead https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
    public ResponseEntity<Object> addSpace
            (@RequestParam String description,//just use request body for POST 
             @RequestParam BigDecimal price){
        String loggedUser = loggedUser();

        Space space = spaceService.createSpace(loggedUser, description, price);//could pass down Space DTO object from request rather than separately price and description and user 
        emailService.postingSpaceEmail(loggedUser, space.getUser().getName());//I would add coordinating logic to send email somewhere else it does not belong to controller - see Single Responsibility Principle SRP
        return new ResponseEntity<>("Space posted successfully!", HttpStatus.CREATED); // if space has an ID you should probably return it here, probably the whole object returned back would make sense here
    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> viewSpace
            (@PathVariable Long id){
        Space space = spaceService.getSpace(id);
        SpaceDTO spaceDTO = mapper.spaceToDTO(space); //good attempt at using DTOs sadly youre still persisting raw domain object in DB so didn't get it quite deep enough. Ideally there was a second DTO type class (sepearate to existing one) that is used for DB saving and retrieval 

        return new ResponseEntity<>(spaceDTO, HttpStatus.OK);
    }

    @PostMapping("/{id}/add-availability") // bad - use HTTP verbs instead https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
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
            emailService.spaceUpdateEmail(loggedUser, spaceService.getSpace(id).getUser().getName());
            return new ResponseEntity<>("Availability added successfully!", HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>("Access denied.", HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping("/all")  // bad - use HTTP verbs instead https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
    public ResponseEntity<Object> viewAll(){
        List<Space> allSpaces = spaceService.getAll();
        List<SpaceDTO> allSpacesDTO = allSpaces.stream().map(space -> mapper.spaceToDTO(space)).toList();
        return new ResponseEntity<>(allSpacesDTO, HttpStatus.OK);
    }
    @GetMapping("/find")  // bad - use HTTP verbs instead https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
    public ResponseEntity<Object> findAvailable(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate){
        if (startDate == null && endDate == null){
            List <SpaceDTO> availableSpacesDTO = spaceService.findAvailable()
                    .stream()
                    .map(space -> mapper.spaceToDTO(space)).toList();
            return new ResponseEntity<>(availableSpacesDTO, HttpStatus.OK);
        }

        else if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now())){
            return new ResponseEntity<>("Please select valid dates!", HttpStatus.BAD_REQUEST);
        }
        else {
            List<LocalDate> dates = startDate.datesUntil(endDate).toList();
            List<SpaceDTO> availableSpacesDTO = spaceService.findAvailable(dates).stream()
                    .map(space -> mapper.spaceToDTO(space)).toList();
            return new ResponseEntity<>(availableSpacesDTO, HttpStatus.FOUND);
        }
    }

    //this could be something like:
    //GET /spaces - viewAll
    //POST /spaces - addSpace
    //GET /spaces?startDate=2024-11-11&endDate=2024-11-11&available=true - findAvailable
    //GET /spaces/{id} - viewSpace
    //PATCH or PUT /spaces/{id} - add availability

}
