package com.example.bnb.service;

import com.example.bnb.model.User;
import com.example.bnb.repository.UserRepository;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Boolean validatePassword (String password) {
        boolean areRequirementsMet = false;
        if (password.length() >= 8) {
            Pattern letter = Pattern.compile("[a-zA-z]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

            Matcher hasLetter = letter.matcher(password);
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);

            if (hasLetter.find() && hasDigit.find() && hasSpecial.find()) {
                areRequirementsMet = true;
            }
        }
        return areRequirementsMet;
    }

    private Boolean validateEmailAddress(String email) {
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
    @Transactional
    public User createUser(String email, String name, String password){
        if (!validateEmailAddress(email)){
            throw new InvalidParameterException("Provided email address is invalid!");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EntityExistsException("An account with this email address already exists!");
        }
        if (!validatePassword(password)) {
            throw new SecurityException("Password must be at least 8 characters long, must contain at least one special character, one letter and one number!");
        }
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return user;
    }
}