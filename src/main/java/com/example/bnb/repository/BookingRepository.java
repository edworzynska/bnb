package com.example.bnb.repository;

import com.example.bnb.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBySpaceIdAndUserId(Long spaceId, Long userId);
}
