package com.example.bnb.repository;

import com.example.bnb.model.Space;
import com.example.bnb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface SpaceRepository extends JpaRepository<Space, Long> {

    @Query("SELECT s FROM Space s JOIN s.spaceAvailabilities sa " +
            "WHERE sa.date IN :dates AND sa.isAvailable = true " +
            "GROUP BY s.id HAVING COUNT(sa) = :dateCount")
    List<Space> findSpacesWithAvailableDates(@Param("dates") List<LocalDate> dates, @Param("dateCount") long dateCount);

    List<Space> findAllBySpaceAvailabilitiesIsAvailableIsTrue();
    
    List<Space> findByUser(User user);
    List<Space> findByUserId(Long userId);
}
