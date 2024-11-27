package com.example.bnb.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "Space_availabilities")
public class SpaceAvailability { //this should be derived from space bookings instead of being an entity 

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    private LocalDate date;
    private Boolean isAvailable;

    public SpaceAvailability() {
    }

    public SpaceAvailability(Boolean isAvailable, LocalDate date, Space space, Long id) {
        this.isAvailable = isAvailable;
        this.date = date;
        this.space = space;
        this.id = id;
    }

}
