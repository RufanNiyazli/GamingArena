package com.project.redis.service.impl;


import com.project.redis.dto.response.LeaderboardUpdateResult;
import com.project.redis.repository.UserRepository;
import com.project.redis.service.ILeaderBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j

public class LeaderBoardServiceImpl implements ILeaderBoardService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final ZSetOperations<String, Object> zSetOperations;

    public LeaderBoardServiceImpl(RedisTemplate<String, Object> redisTemplate, UserRepository userRepository, ZSetOperations<String, Object> zSetOperations) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.zSetOperations = zSetOperations;
    }

    private static final String GLOBAL_LEADERBOARD = "leaderboard:global";
    private static final String DAILY_LEADERBOARD_PREFIX = "leaderboard:daily:";
    private static final String WEEKLY_LEADERBOARD_PREFIX = "leaderboard:weekly:";
    private static final String GAME_LEADERBOARD_PREFIX = "leaderboard:game:";


    @Override
    public LeaderboardUpdateResult addScore(Long userId, Integer points, Long gameId) {
        String member = getMember(userId);
        Double newGlobalScore = zSetOperations.incrementScore(GLOBAL_LEADERBOARD, member, points);

        String dailyKey = getDailyLeaderBoardKey();
        Double newDailyScore = zSetOperations.incrementScore(dailyKey, member, points);


        return null;
    }


    private String getMember(Long userId) {
        return "user:" + userId;
    }

    private String getDailyLeaderBoardKey() {
        return getDailyLeaderboardKey(LocalDate.now());
    }

    private String getDailyLeaderboardKey(LocalDate date) {
        return GLOBAL_LEADERBOARD + date.format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
