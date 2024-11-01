package com.example.bnb.repository;

import com.example.bnb.model.Booking;
import com.example.bnb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBySpaceIdAndUserId(Long spaceId, Long userId);
}
