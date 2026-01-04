package com.project.redis.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username can't be empty")
        @Size(min = 3, max = 50, message = "Username can be only 3 to 50 character")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can be only letter and numbers")
        String username,

        @NotBlank(message = "Email can't be empty")
        @Email(message = "Email format not correct")
        String email
) {
}
