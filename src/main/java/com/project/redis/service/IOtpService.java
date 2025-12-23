package com.project.redis.service;

import com.project.redis.exception.InvalidOtpException;
import com.project.redis.exception.OtpExpiredException;

public interface IOtpService {
    public Long getOtpTTL(String email);

    public void deleteOtp(String email);

    public boolean hasOtp(String email);

    public boolean verifyOtp(String email, String inputOtp) throws OtpExpiredException, InvalidOtpException;

    public void generateAndSendOtp(String email, String username);
}
