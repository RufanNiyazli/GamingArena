package com.project.redis.service;

import com.project.redis.dto.response.LeaderboardUpdateResult;

public interface ILeaderBoardService {
    public LeaderboardUpdateResult addScore(Long userId, Integer points, Long gameId);
}
