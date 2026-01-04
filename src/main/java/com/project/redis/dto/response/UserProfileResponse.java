package com.project.redis.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Long totalScore;
    private Integer level;
    private Integer totalMatches;
    private Integer wins;
    private Double winRate;
    private Integer losses;
    private LocalDateTime lastLogin;
}
