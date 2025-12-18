package com.project.redis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer level;
    private Long totalScore;
    private Integer totalMatches;
    private Integer wins;
    private Integer losses;
    private Double winRate;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Boolean emailVerified;
}
