package com.project.redis.service.impl;

import com.project.redis.exception.InvalidOtpException;
import com.project.redis.exception.OtpExpiredException;
import com.project.redis.service.IEmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl {
    private final IEmailService emailService;
    private static final SecureRandom random = new SecureRandom();
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${otp.length}")
    private int otpLength;

    @Value("${otp.expiration}")
    private long expiration_time;

    public void generateAndSendOtp(String email, String username) {
        String otp = generateOtp();
        String key = getOtpKey(email);
        redisTemplate.opsForValue().set(key, otp, expiration_time, TimeUnit.SECONDS);
        log.info("OTP created {}->{}->{}", otp, email, expiration_time);
        try {
            emailService.sendOTPEmail(email, otp, username);
        } catch (MessagingException e) {
            log.error("OTP cannot sent!{}", email, e);
            throw new RuntimeException(e);
        }
        resetAttempts(email);


    }

    public boolean verifyOtp(String email, String inputOtp) throws OtpExpiredException, InvalidOtpException {
        String key = getOtpKey(email);
        String dbOtp = (String) redisTemplate.opsForValue().get(key);
        if (dbOtp == null) {
            log.error("There is not otp code such  as . {}", email);
            throw new OtpExpiredException("OTP expired or not in db");

        }
        checkAttempts(email);
        if (!dbOtp.equals(inputOtp)) {
            incrementAttempts(email);
            log.warn("❌ Wrong OTP: {} (Included: {})", email, inputOtp);
            throw new InvalidOtpException("OTP kodu yanlışdır");
        }
        redisTemplate.delete(key);
        resetAttempts(email);

        log.info(" OTP verified: {}", email);
        return true;
    }

    private void checkAttempts(String email) throws InvalidOtpException {
        String key = getAttemptsKey(email);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        if (attempts != null && attempts >= 5) {
            log.warn("Many attempts: {} ({} time)", email, attempts);
            throw new InvalidOtpException(
                    "Too many incorrect attempts. Please wait 15 minutes."
            );
        }

    }

    private void incrementAttempts(String email) {
        String key = getAttemptsKey(email);

        Long attempts = redisTemplate.opsForValue().increment(key);


        if (attempts != null && attempts == 3) {
            redisTemplate.expire(key, 15, TimeUnit.MINUTES);
        }
    }

    private void resetAttempts(String email) {
        String key = getAttemptsKey(email);
        redisTemplate.delete(key);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();

    }

    private String getOtpKey(String email) {
        return "otp:" + email;

    }

    private String getAttemptsKey(String email) {
        return "otp:attempts:" + email;
    }

    public boolean hasOtp(String email) {
        String key = getOtpKey(email);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    public Long getOtpTTL(String email) {
        String key = getOtpKey(email);
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void deleteOtp(String email) {
        String key = getOtpKey(email);
        redisTemplate.delete(key);
        log.info("OTP cannot delete: {}", email);
    }
}
