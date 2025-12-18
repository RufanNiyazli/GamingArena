package com.project.redis.dto;

import jakarta.validation.constraints.*;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

public record VerifyOTPRequest(
        @NotBlank(message = "Email can't be empty")
        @Email(message = "Email format not correct")
        String email,

        @NotBlank(message = "OTP code can't be empty")
        @Size(min = 6, max = 6, message = "OTP code must be six number")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain only number")
        String otp
) {
}
