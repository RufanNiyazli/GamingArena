package com.project.redis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendOTPRequest(
        @NotBlank(message = "Email can't be empty")
        @Email(message = "Email format not correct")
        String email
) {
}
