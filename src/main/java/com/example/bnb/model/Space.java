package com.example.bnb.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Spaces")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL)
    List<SpaceAvailability> spaceAvailabilities = new ArrayList<>();

    @Column(name = "description")
    private String description;

    @Column(name = "price_per_night")
    private BigDecimal pricePerNight;

    public Space() {
    }

    public void setDescription(String description){
        if (description.isBlank() || description.isEmpty()){
            throw new InvalidParameterException("Description cannot be empty!");
        }
        this.description = description;
    }
    public void setPricePerNight(BigDecimal pricePerNight){
        if (pricePerNight.signum() != 1){
            throw new InvalidParameterException("Price has to be a positive number!");
        }
        this.pricePerNight = pricePerNight;
    }

    public Space(User user, String description, BigDecimal pricePerNight) {
        this.user = user;
        this.description = description;
        this.pricePerNight = pricePerNight;
    }
}
