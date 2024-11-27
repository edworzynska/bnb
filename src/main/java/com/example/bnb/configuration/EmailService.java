package com.example.bnb.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
//this is not a configuration class, package should be different 
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

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
        message.setText(String.format("Hello, %s\nThank you for signing up to BnB!\n\nBest regards,\nBnB Team", name));

        //why not simply reuse sendEmail above?
        javaMailSender.send(message);
    }
    public void postingSpaceEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your space has been posted!");
        message.setText(String.format("Hello, %s\nThank you for posting your space!\nPlease add availability so others can request a booking.\n\nBest regards,\nBnB Team", name));
        javaMailSender.send(message);
    }
    public void newBookingRequestEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your have a new booking request!");
        message.setText(String.format("Hello, %s\nSomeone has just submitted a request to book your space.\nPlease visit your profile to approve it.\n\nBest regards,\nBnB Team", name));
        javaMailSender.send(message);
    }
    public void spaceUpdateEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your space has been updated!");
        message.setText(String.format("Hello, %s\nYour space has been successfully updated and the changes are visible to other users.\n\nBest regards,\nBnB Team", name));
        javaMailSender.send(message);
    }
    public void requestConfirmedEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("You've confirmed a booking!");
        message.setText(String.format("Hello, %s\nYou've just confirmed booking of your space.\n\nBest regards,\nBnB Team", name));
        javaMailSender.send(message);
    }
    public void requestConfirmationEmail(String to){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("You've requested a booking!");
        message.setText("Hello!\nYou've just requested a booking.\nYou'll be informed in a separate email once your request is approved.\n\nBest regards,\nBnB Team");
        javaMailSender.send(message);
    }
    public void bookingConfirmationEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your request has been approved!");
        message.setText("Hello!\nYour request to book a space has been approved.\n\nBest regards,\nBnB Team");
        javaMailSender.send(message);
    }
    public void bookingDenialEmail(String to, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your request has been approved!");
        message.setText("Hello!\nYour request to book a space has been denied.\n\nBest regards,\nBnB Team");
        javaMailSender.send(message);
    }
}
