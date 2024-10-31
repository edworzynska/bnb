package com.example.bnb.controller;

import com.example.bnb.model.Booking;
import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.service.BookingService;
import com.example.bnb.service.SpaceService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    public BookingController(BookingService bookingService, SpaceService spaceService, SpaceAvailabilityRepository spaceAvailabilityRepository) {
        this.bookingService = bookingService;
        this.spaceService = spaceService;
        this.spaceAvailabilityRepository = spaceAvailabilityRepository;
    }

    private String loggedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()){
            throw new SecurityException("Please log in to proceed.");
        }
        return authentication.getName();
    }

    @PostMapping("/spaces/{id}/request-booking")
    public ResponseEntity<Object> requestBooking(
            @PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now())){
            return new ResponseEntity<>("Please select valid dates!", HttpStatus.BAD_REQUEST);
        }

        List<LocalDate> dates = startDate.datesUntil(endDate).toList();
        String loggedUser = loggedUser();
        User spaceOwner = spaceRepository.findById(id).orElseThrow(EntityNotFoundException::new).getUser();
        var ownersEmail = spaceOwner.getEmail();

        if (loggedUser.equals(ownersEmail)){
            return new ResponseEntity<>("Unable to request a booking in owned space!", HttpStatus.BAD_REQUEST);
        }
        else {
            List<Booking> bookings = bookingService.createBooking(id, loggedUser, dates);
            return new ResponseEntity<>("Booking requested successfully! Please wait for approval.", HttpStatus.CREATED);
        }
    }
    @PostMapping("/spaces/{id}/bookings/approve")
    public ResponseEntity<Object> approveBooking(
            @PathVariable Long id,
            @RequestBody List<Long> bookingsIds){

        String loggedUser = loggedUser();
        Space space = spaceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        Long spaceId = space.getId();
        var spaceOwner = space.getUser().getEmail();

        if (!loggedUser.equals(spaceOwner)){
            return new ResponseEntity<>("Access denied!", HttpStatus.FORBIDDEN);
        }
        else {
            bookingService.approveBookings(spaceId, spaceOwner, bookingsIds);
            return new ResponseEntity<>("Bookings approved successfully!", HttpStatus.OK);
        }
    }
}
