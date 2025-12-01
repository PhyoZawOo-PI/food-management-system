package com.phyo.food_management_system.service.impl;

import com.phyo.food_management_system.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;


    // Send an email asynchronously
    @Async("asyncTaskExecutor")
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("sendEmail is triggered by {} at {}" , Thread.currentThread().getName() ,
                    LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send email: {}" , e.getMessage());
        }
    }
}
