package com.project.redis.service.impl;

import com.project.redis.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor

public class EmailServiceImpl implements IEmailService {

    @Value("${spring.mail.username}")
    private String username;

    private final JavaMailSender javaMailSender;

    @Async
    public void SimpleMailSend(String to, String subject, String text) {
        try {

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setText(text);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setTo(to);
            simpleMailMessage.setFrom(username);
            javaMailSender.send(simpleMailMessage);
            log.info("Email sent : {} -> {}", username, to);
        } catch (Exception e) {
            log.error("Email could not be sent.: {} - {}", to, e.getMessage());
            throw new RuntimeException("Email could not be sent.", e);
        }

    }

    @Async
    public void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setText(htmlContent, true);
        helper.setTo(to);
        helper.setFrom(username);
        helper.setSubject(subject);
        javaMailSender.send(message);
        log.info("Html Email sent! {} -> {}", username, to);


    }


    @Async
    public void sendOTPEmail(String to, String otp, String username) throws MessagingException {
        String subject = "🎮 Gaming Arena - Your OTP Code";
        String htmlContent = buildOtpEmailTemplate(otp, username);

        sendHtmlMail(to, subject, htmlContent);
    }

    @Async
    public void sendWelcomeEmail(String to, String username) throws MessagingException {
        String subject = "Welcome Gaming Arena 🎮";
        String htmlContent = buildWelcomeEmailTemplate(username);
        sendHtmlMail(to, subject, htmlContent);
    }

    //
    private String buildOtpEmailTemplate(String otp, String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .container {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            padding: 40px;
                            border-radius: 10px;
                            color: white;
                            text-align: center;
                        }
                        .otp-box {
                            background: white;
                            color: #333;
                            padding: 30px;
                            margin: 30px 0;
                            border-radius: 10px;
                            font-size: 36px;
                            font-weight: bold;
                            letter-spacing: 10px;
                        }
                        .warning {
                            background: #fff3cd;
                            color: #856404;
                            padding: 15px;
                            border-radius: 5px;
                            margin-top: 20px;
                            font-size: 14px;
                        }
                        .footer {
                            margin-top: 30px;
                            font-size: 12px;
                            color: rgba(255,255,255,0.8);
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🎮 Gaming Arena</h1>
                        <h2>Salam, %s!</h2>
                        <p>OTP kodunuz:</p>
                
                        <div class="otp-box">
                            %s
                        </div>
                
                        <p>Bu kod <strong>5 dəqiqə</strong> ərzində etibarlıdır.</p>
                
                        <div class="warning">
                            ⚠️ Bu kodu heç kimlə paylaşmayın!<br>
                            Gaming Arena əməkdaşları heç vaxt sizdən OTP soruşmaz.
                        </div>
                
                        <div class="footer">
                            <p>Bu email-i siz tələb etməmisinizsə, iqnor edin.</p>
                            <p>&copy; 2024 Gaming Arena. Bütün hüquqlar qorunur.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username, otp);
    }

    //
    private String buildWelcomeEmailTemplate(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .container {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            padding: 40px;
                            border-radius: 10px;
                            color: white;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🎉 Xoş Gəldin!</h1>
                        <h2>%s</h2>
                        <p>Gaming Arena-ya qoşulduğun üçün təşəkkür edirik!</p>
                    </div>
                </body>
                </html>
                """.formatted(username);
    }


}
