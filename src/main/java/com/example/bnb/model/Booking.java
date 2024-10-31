package com.example.bnb.model;

import jakarta.persistence.*;
import lombok.Data;

import java.security.InvalidParameterException;
import java.time.LocalDate;

import static com.example.bnb.model.BookingStatus.*;

@Data
@Entity
@Table(name = "Bookings")
public class Booking {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "date")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    public Booking() {
    }

    public Booking(Space space, User user, LocalDate date) {
        this.space = space;
        this.user = user;
        this.date = date;
    }
    public void setBookingStatus(BookingStatus bookingStatus) {
        if (this.bookingStatus == BookingStatus.PENDING) {
            this.bookingStatus = bookingStatus;
        } else {
            throw new IllegalStateException("Unable to change the status from " + this.bookingStatus);
        }
    }
}
