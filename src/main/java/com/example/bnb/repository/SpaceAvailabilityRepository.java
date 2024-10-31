package com.example.bnb.repository;

import com.example.bnb.model.Space;
import com.example.bnb.model.SpaceAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpaceAvailabilityRepository extends JpaRepository<SpaceAvailability, Long> {
    List<SpaceAvailability> findBySpaceId(Long spaceId);
    List<SpaceAvailability> findBySpace(Space space);
    List<SpaceAvailability> findByDate(LocalDate date);

    List<SpaceAvailability> findBySpaceIdAndDateIn(Long spaceId, List<LocalDate> dates);
}
