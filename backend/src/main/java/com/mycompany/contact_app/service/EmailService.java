package com.mycompany.contact_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private static final String FROM_EMAIL = "noreply@contactsmanager.com";

    public void sendWelcomeEmail(String to, String firstName, String lastName) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping welcome email");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Welcome to Contacts Manager!");
            message.setText(String.format(
                    "Dear %s %s,\n\n" +
                            "Welcome to Contacts Manager! Your account has been successfully created.\n\n" +
                            "You can now log in and start managing your contacts.\n\n" +
                            "Best regards,\nContacts Manager Team",
                    firstName, lastName));
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    public void sendLoginNotification(String to, String firstName, String ipAddress) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping login notification");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Login Notification - Contacts Manager");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your account was successfully logged in from IP address: %s\n\n" +
                            "If this wasn't you, please contact support immediately.\n\n" +
                            "Best regards,\nContacts Manager Team",
                    firstName, ipAddress));
            mailSender.send(message);
            log.info("Login notification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send login notification email to: {}", to, e);
        }
    }

    public void sendContactCreatedNotification(String to, String firstName, String contactName) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping contact creation notification");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("New Contact Added - Contacts Manager");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "A new contact '%s' has been successfully added to your contacts list.\n\n" +
                            "Best regards,\nContacts Manager Team",
                    firstName, contactName));
            mailSender.send(message);
            log.info("Contact creation notification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send contact creation notification to: {}", to, e);
        }
    }

    public void sendContactModifiedNotification(String to, String firstName, String contactName, String changes) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping contact modification notification");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Contact Modified - Contacts Manager");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "The contact '%s' has been successfully modified.\n\n" +
                            "Changes: %s\n\n" +
                            "Best regards,\nContacts Manager Team",
                    firstName, contactName, changes));
            mailSender.send(message);
            log.info("Contact modification notification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send contact modification notification to: {}", to, e);
        }
    }

    public void sendSecurityAlert(String to, String firstName, String alertMessage) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping security alert");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Security Alert - Contacts Manager");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Security Alert: %s\n\n" +
                            "If you didn't perform this action, please change your password immediately.\n\n" +
                            "Best regards,\nContacts Manager Security Team",
                    firstName, alertMessage));
            mailSender.send(message);
            log.info("Security alert email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send security alert to: {}", to, e);
        }
    }
}
