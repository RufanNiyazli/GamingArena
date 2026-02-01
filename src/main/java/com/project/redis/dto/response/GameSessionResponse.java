package com.project.redis.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data@Builder

public class GameSessionResponse {
    private String sessionId;
    private Long gameId;
    private String gameName;
    private LocalDateTime startedAt;
}
