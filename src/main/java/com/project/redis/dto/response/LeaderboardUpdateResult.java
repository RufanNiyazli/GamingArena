package com.project.redis.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardUpdateResult {
    private Long userId;
    private Integer pointsAdded;
    private Long newGlobalScore;
    private Integer globalRank;
    private Long newDailyScore;
    private Integer dailyRank;
}
