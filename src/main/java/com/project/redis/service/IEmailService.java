package com.project.redis.service;

import jakarta.mail.MessagingException;

public interface IEmailService {
    public void SimpleMailSend(String to, String subject, String text);

    public void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException;

    public void sendOTPEmail(String to, String otp, String username) throws MessagingException;

    public void sendWelcomeEmail(String to, String username) throws MessagingException;
}
