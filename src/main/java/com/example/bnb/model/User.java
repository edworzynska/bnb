package com.example.bnb.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.IdGeneratorType;

import javax.naming.InvalidNameException;
import java.security.InvalidParameterException;
import java.util.List;

@Data
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    List<Space> spaces;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    public void setName(String name) {
        if (name.isEmpty() || name.isBlank()){
            throw new InvalidParameterException("Name cannot be empty!");
        }
        this.name = name;
    }

    public User() {
    }

    public User(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
