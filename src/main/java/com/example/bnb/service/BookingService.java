package com.example.bnb.service;

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

        Long firstUserId = bookingsToApprove.get(0).getUser().getId();
        Long firstSpaceId = bookingsToApprove.get(0).getSpace().getId();

        boolean isTheSameUser = bookingsToApprove.stream()
                .allMatch(booking -> booking.getUser().getId().equals(firstUserId));
        boolean isTheSameSpace = bookingsToApprove.stream()
                .allMatch(booking -> booking.getSpace().getId().equals(firstSpaceId));

        if (!isTheSameUser) {
            throw new IllegalArgumentException("All bookings must belong to the same user!");
        }
        if (!isTheSameSpace){
            throw new IllegalArgumentException("All bookings must refer to the same space!");
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
    public void denyBookings(List<Long> bookingsIds){

        List<Booking> bookingsToDeny = bookingRepository.findAllById(bookingsIds);

        if (bookingsToDeny.isEmpty()) {
            throw new EntityNotFoundException("No bookings found for the provided IDs.");
        }

        Long firstUserId = bookingsToDeny.get(0).getUser().getId();
        Long firstSpaceId = bookingsToDeny.get(0).getSpace().getId();

        boolean isTheSameUser = bookingsToDeny.stream()
                .allMatch(booking -> booking.getUser().getId().equals(firstUserId));
        boolean isTheSameSpace = bookingsToDeny.stream()
                .allMatch(booking -> booking.getSpace().getId().equals(firstSpaceId));

        if (!isTheSameUser) {
            throw new IllegalArgumentException("All bookings must belong to the same user!");
        }
        if (!isTheSameSpace) {
            throw new IllegalArgumentException("All bookings must refer to the same space!");
        }
        List<LocalDate> datesToDeny = bookingsToDeny.stream()
                .map(Booking::getDate)
                .distinct()
                .toList();

        if (!spaceAvailabilityService.isSpaceAvailableInDates(firstSpaceId, datesToDeny)){
            throw new CannotCreateTransactionException("Unable to process the request - space is not available in selected dates.");
        }
        for (Booking booking : bookingsToDeny) {
            booking.setBookingStatus(BookingStatus.DENIED);
        }
        bookingRepository.saveAll(bookingsToDeny);
    }
}
