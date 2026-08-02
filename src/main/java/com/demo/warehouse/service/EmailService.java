package com.demo.warehouse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Value("${app.notification.email:}")
    private String notificationEmail;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Async
    public void sendNewUserNotification(String userEmail, String auth0Sub) {
        if (notificationEmail == null || notificationEmail.isEmpty()) {
            log.warn("Notification email not configured, skipping email notification");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notificationEmail);
            message.setSubject("New Auth0 User Created");
            message.setText(String.format(
                "A new user has been created in Auth0:\n\n" +
                "Email: %s\n" +
                "Auth0 Sub: %s\n\n" +
                "This is an automated notification.",
                userEmail, auth0Sub
            ));

            mailSender.send(message);
            log.info("New user notification sent to {} for user {}", notificationEmail, userEmail);
        } catch (Exception e) {
            log.error("Failed to send new user notification email", e);
        }
    }
}
