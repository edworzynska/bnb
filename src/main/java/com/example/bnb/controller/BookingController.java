package com.example.bnb.controller;

import com.example.bnb.configuration.EmailService;
import com.example.bnb.model.Booking;
import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.model.User;
import com.example.bnb.repository.BookingRepository;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
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
@RequestMapping("/booking") //singular - bad
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    @Autowired
    private EmailService emailService;

//you should not need this constructor here as everything is autowired? 
    public BookingController(EmailService emailService, BookingService bookingService, SpaceService spaceService, UserRepository userRepository, SpaceRepository spaceRepository, SpaceAvailabilityRepository spaceAvailabilityRepository) {
        this.emailService = emailService;
        this.bookingService = bookingService;
        this.spaceService = spaceService;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.spaceAvailabilityRepository = spaceAvailabilityRepository;
    }

    private String loggedUser(){ //this should be reused outside of this class
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()){
            throw new SecurityException("Please log in to proceed.");
        }
        return authentication.getName();
    }

    @PostMapping("/spaces/{id}/request-booking")// bad; I showed some examples in spaces so will skip it here 
    public ResponseEntity<Object> requestBooking(
            @PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now())){
            return new ResponseEntity<>("Please select valid dates!", HttpStatus.BAD_REQUEST);
        }

        List<LocalDate> dates = startDate.datesUntil(endDate).toList();//lots of bookings get created as a result probably would make more sense to keep date range in one booking 
        String loggedUser = loggedUser();
        User spaceOwner = spaceRepository.findById(id).orElseThrow(EntityNotFoundException::new).getUser();
        var ownersEmail = spaceOwner.getEmail();

        if (loggedUser.equals(ownersEmail)){
            return new ResponseEntity<>("Unable to request a booking in owned space!", HttpStatus.BAD_REQUEST); // as an owner I probably want to be able to book my own space so other people don't use it when I don't want them to 
        }
        else {
            List<Booking> bookings = bookingService.createBooking(id, loggedUser, dates);
            emailService.newBookingRequestEmail(ownersEmail, spaceOwner.getName());
            emailService.requestConfirmationEmail(loggedUser);//
            return new ResponseEntity<>("Booking requested successfully! Please wait for approval.", HttpStatus.CREATED);
        }
    }
    @PostMapping("/spaces/{id}/approve")
    public ResponseEntity<Object> approveBooking(
            @PathVariable Long id,
            @RequestBody List<Long> bookingsIds){

        String loggedUser = loggedUser();
        Space space = spaceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        var spaceOwner = space.getUser().getEmail();

        if (!loggedUser.equals(spaceOwner)){
            return new ResponseEntity<>("Access denied!", HttpStatus.FORBIDDEN);
        }
        else {
            bookingService.approveBookings(id, bookingsIds);
            User requestingUser = userRepository.findByBookingsId(bookingsIds.get(0)).orElseThrow();

            emailService.requestConfirmedEmail(spaceOwner, space.getUser().getName());
            emailService.bookingConfirmationEmail(requestingUser.getEmail(), requestingUser.getName());

            return new ResponseEntity<>("Bookings approved successfully!", HttpStatus.OK);
        }
    }
    @PostMapping("/spaces/{id}/deny")
    public ResponseEntity<Object> denyBooking(
            @PathVariable Long id,
            @RequestBody List<Long> bookingsIds){

        String loggedUser = loggedUser();
        Space space = spaceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        var spaceOwner = space.getUser().getEmail();

        if (!loggedUser.equals(spaceOwner)){
            return new ResponseEntity<>("Access denied!", HttpStatus.FORBIDDEN);
        }
        else {
            bookingService.denyBookings(id, bookingsIds);
            User requestingUser = userRepository.findByBookingsId(bookingsIds.get(0)).orElseThrow();

            emailService.bookingDenialEmail(requestingUser.getEmail(), requestingUser.getName());

            return new ResponseEntity<>("Bookings denied successfully!", HttpStatus.OK);
        }
    }
}
