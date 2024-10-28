package com.example.bnb.controller;

import com.example.bnb.service.UserService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> signUp
            (@RequestParam String email,
             @RequestParam String name,
             @RequestParam String password){

        userService.createUser(email, name, password);
        return new ResponseEntity<>("User registered successfully! You can sign in.", HttpStatus.CREATED);
    }
}
