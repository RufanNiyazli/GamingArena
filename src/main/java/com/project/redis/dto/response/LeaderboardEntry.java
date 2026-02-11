package com.project.redis.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LeaderboardEntry {
    private Integer rank;
    private Long userId;
    private String username;
    private String avatar;
    private Long score;
    private Integer level;
}
