package com.project.redis.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchResult {
    private Long matchId;
    private com.project.redis.enums.MatchResult result;
    private Integer score;
    private Integer earnedScore;
    private Long newTotalScore;
}
