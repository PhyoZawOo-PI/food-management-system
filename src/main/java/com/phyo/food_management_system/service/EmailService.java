package com.phyo.food_management_system.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);
}
