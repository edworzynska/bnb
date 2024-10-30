package com.example.bnb.repository;

import com.example.bnb.model.Space;
import com.example.bnb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface SpaceRepository extends JpaRepository<Space, Long> {
    List<Space> findByUser(User user);
    List<Space> findByUserId(Long userId);
}
