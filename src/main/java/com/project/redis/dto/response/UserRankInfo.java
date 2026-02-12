package com.project.redis.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserRankInfo {
    private Long userId;
    private String username;
    private String avatar;

    private Integer globalRank;
    private Long globalScore;

    private Integer dailyRank;
    private Long dailyScore;

    private Integer weeklyRank;
    private Long weeklyScore;

    // Stats
    private Integer totalPlayers;
    private Double percentile;
    private String tier;
}
