package com.example.bnb.service;

import com.example.bnb.model.SpaceAvailability;
import com.example.bnb.repository.SpaceAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SpaceAvailabilityService {

    @Autowired
    private SpaceAvailabilityRepository spaceAvailabilityRepository;

    public boolean isSpaceAvailableInDates(Long spaceId, List<LocalDate> dates){

         List<SpaceAvailability> matchingDates = spaceAvailabilityRepository
                 .findBySpaceIdAndDateIn(spaceId, dates);

         return matchingDates.size() == dates.size()
                 && matchingDates.stream()
                 .allMatch(SpaceAvailability::getIsAvailable);
    }
    @Transactional
    public void setUnavailable(Long spaceId, List<LocalDate> bookedDates){
        List<SpaceAvailability> availabilitiesToChange = spaceAvailabilityRepository
                .findBySpaceIdAndDateIn(spaceId, bookedDates);
        for (SpaceAvailability spaceAvailability : availabilitiesToChange){
            spaceAvailability.setIsAvailable(false);
        }
        spaceAvailabilityRepository.saveAll(availabilitiesToChange);
    }
}
