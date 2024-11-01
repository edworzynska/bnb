package com.example.bnb.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
    }
    public void registrationEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to BnB!");
        message.setText(String.format("Hello, %s\nThank you for signing up to BnB!\n", name));
        javaMailSender.send(message);
    }
    public void postingSpaceEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your space has been posted!");
        message.setText(String.format("Hello, %s\nThank you for posting your space!\nPlease add availability so others can request a booking.\nBest regards,\nBnB Team", name));
        javaMailSender.send(message);
    }
}
