package com.example.bnb.service;

import com.example.bnb.configuration.EmailService;
import com.example.bnb.model.Booking;
import com.example.bnb.model.BookingStatus;
import com.example.bnb.model.Space;
import com.example.bnb.model.User;
import com.example.bnb.repository.BookingRepository;
import com.example.bnb.repository.SpaceRepository;
import com.example.bnb.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceAvailabilityService spaceAvailabilityService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, SpaceRepository spaceRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
    }

    @Transactional
    public List<Booking> createBooking(Long spaceId, String userEmail, List<LocalDate> dates){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User doesn't exist!"));
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new EntityNotFoundException("Space not found!"));
        if (!spaceAvailabilityService.isSpaceAvailableInDates(spaceId, dates)){
            throw new CannotCreateTransactionException("Unable to create booking: space is not available in requested dates.");
        }
        if (dates.isEmpty()){
            throw new InvalidParameterException("Please select dates!");
        }
        List<Booking> bookings = new ArrayList<>();

        for (LocalDate date : dates){
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setSpace(space);
            booking.setDate(date);
            bookingRepository.save(booking);
            bookings.add(booking);
        }
        return bookings;
    }

    @Transactional
    public void approveBookings(Long spaceId, List<Long> bookingsIds){

        List<Booking> bookingsToApprove = bookingRepository.findAllById(bookingsIds);

        if (bookingsToApprove.isEmpty()) {
            throw new EntityNotFoundException("No bookings found for the provided IDs.");
        }
        for (Booking booking : bookingsToApprove){
            if (booking.getSpace().getId() != spaceId){
                throw new IllegalArgumentException("Unable to process; one or more bookings aren't assigned to the space!");
            }
        }
        List<LocalDate> datesToApprove = bookingsToApprove.stream()
                .map(Booking::getDate)
                .distinct()
                .toList();

        if (!spaceAvailabilityService.isSpaceAvailableInDates(spaceId, datesToApprove)){
            throw new CannotCreateTransactionException("Unable to approve the request - space is not available in selected dates.");
        }
        for (Booking booking : bookingsToApprove){
            booking.setBookingStatus(BookingStatus.APPROVED);
        }

        bookingRepository.saveAll(bookingsToApprove);
        spaceAvailabilityService.setUnavailable(spaceId, datesToApprove);

    }

    @Transactional
    public void denyBookings(Long spaceId, List<Long> bookingsIds){

        List<Booking> bookingsToDeny = bookingRepository.findAllById(bookingsIds);

        if (bookingsToDeny.isEmpty()) {
            throw new EntityNotFoundException("No bookings found for the provided IDs.");
        }
        for (Booking booking : bookingsToDeny){
            if (booking.getSpace().getId() != spaceId){
                throw new IllegalArgumentException("Unable to process; one or more bookings aren't assigned to the space!");
            }
        }
        List<LocalDate> datesToDeny = bookingsToDeny.stream()
                .map(Booking::getDate)
                .distinct()
                .toList();

        if (!spaceAvailabilityService.isSpaceAvailableInDates(spaceId, datesToDeny)){
            throw new CannotCreateTransactionException("Unable to process the request - space is not available in selected dates.");
        }
        for (Booking booking : bookingsToDeny) {
            booking.setBookingStatus(BookingStatus.DENIED);
        }
        bookingRepository.saveAll(bookingsToDeny);
    }
}
