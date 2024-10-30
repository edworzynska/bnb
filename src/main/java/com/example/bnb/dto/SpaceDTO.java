package com.example.bnb.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpaceDTO {

    private Long spaceId;
    private String ownerName;
    private String description;
    private BigDecimal pricePerNight;

    public SpaceDTO() {
    }

    public SpaceDTO(Long spaceId, String ownerName, String description, BigDecimal pricePerNight) {
        this.spaceId = spaceId;
        this.ownerName = ownerName;
        this.description = description;
        this.pricePerNight = pricePerNight;
    }
}
