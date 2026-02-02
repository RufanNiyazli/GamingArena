package com.project.redis.dto.request;

import com.project.redis.enums.MatchResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinishGameRequest {
    private String sessionId;
    private Long opponentId;
    private MatchResult result;
    private Integer score;
    private Integer duration;
}
