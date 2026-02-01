package com.project.redis.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {
    private String sessionId;
    private Long userId;
    private Long gameId;
    private LocalDateTime startedAt;
}
