package com.example.bnb.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class SpaceDTO {

    private Long spaceId;
    private String ownerName;
    private String description;
    private BigDecimal pricePerNight;
    private Map<LocalDate, Boolean> availableDates = new TreeMap<>();

    public SpaceDTO() {
    }

    public SpaceDTO(Long spaceId, String ownerName, String description, BigDecimal pricePerNight) {
        this.spaceId = spaceId;
        this.ownerName = ownerName;
        this.description = description;
        this.pricePerNight = pricePerNight;
    }

    public SpaceDTO(Long spaceId, String ownerName, String description,
                    BigDecimal pricePerNight, Map<LocalDate, Boolean> availableDates) {
        this.spaceId = spaceId;
        this.ownerName = ownerName;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.availableDates = availableDates;
    }
}
