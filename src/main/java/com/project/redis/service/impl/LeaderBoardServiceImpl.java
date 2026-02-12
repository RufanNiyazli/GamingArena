package com.project.redis.service.impl;


import com.project.redis.dto.response.LeaderboardEntry;
import com.project.redis.dto.response.LeaderboardUpdateResult;
import com.project.redis.dto.response.UserRankInfo;
import com.project.redis.model.User;
import com.project.redis.repository.UserRepository;
import com.project.redis.service.ILeaderBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        String weeklyKey = getWeeklyLeaderboardKey();
        Double newWeeklyScore = zSetOperations.incrementScore(weeklyKey, member, points);
        if (gameId != null) {
            String gameKey = getGameLeaderboardKey(gameId);
            zSetOperations.incrementScore(gameKey, member, points);
        }
        Long globalRank = zSetOperations.reverseRank(GLOBAL_LEADERBOARD, member);
        Long dailyRank = zSetOperations.reverseRank(dailyKey, member);


        return LeaderboardUpdateResult.builder()
                .pointsAdded(points)
                .newGlobalScore(newGlobalScore != null ? newGlobalScore.longValue() : 0L)
                .globalRank(globalRank != null ? globalRank.intValue() + 1 : 0)
                .newDailyScore(newDailyScore != null ? newDailyScore.longValue() : 0L)
                .dailyRank(dailyRank != null ? dailyRank.intValue() + 1 : 0)
                .build();
    }

    public List<LeaderboardEntry> getTop10() {
        return getTopPlayer(10);
    }

    public List<LeaderboardEntry> getTopPlayer(int count) {
        Set<ZSetOperations.TypedTuple<Object>> results = zSetOperations.reverseRangeWithScores(GLOBAL_LEADERBOARD, 0, count - 1);
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return buildLeaderboardEntries(results, 1);


    }

    public List<LeaderboardEntry> getTop100() {
        return getTopPlayer(100);
    }

    public List<LeaderboardEntry> getDailyTop10() {
        String key = getDailyLeaderboardKey(LocalDate.now());
        Set<ZSetOperations.TypedTuple<Object>> results = zSetOperations.reverseRangeWithScores(key, 0, 9);
        if (results == null) {
            return List.of();
        }
        return buildLeaderboardEntries(results, 1);
    }

    public List<LeaderboardEntry> getWeeklyTop10() {
        String key = getWeeklyLeaderboardKey();
        Set<ZSetOperations.TypedTuple<Object>> results = zSetOperations.reverseRangeWithScores(key, 0, 9);
        if (results == null) {
            return List.of();
        }
        return buildLeaderboardEntries(results, 1);
    }

    public List<LeaderboardEntry> getGameTop10(Long gameId) {
        String key = getGameLeaderboardKey(gameId);

        Set<ZSetOperations.TypedTuple<Object>> results =
                zSetOperations.reverseRangeWithScores(key, 0, 9);

        if (results == null) {
            return List.of();
        }

        return buildLeaderboardEntries(results, 1);
    }

    public UserRankInfo getUserRank(Long userId) {
        log.info("📊 User reytinqi: user:{}", userId);

        String member = getUserMember(userId);

        // Global
        Long globalRank = zSetOperations.reverseRank(GLOBAL_LEADERBOARD, member);
        Double globalScore = zSetOperations.score(GLOBAL_LEADERBOARD, member);

        // Daily
        String dailyKey = getDailyLeaderboardKey(LocalDate.now());
        Long dailyRank = zSetOperations.reverseRank(dailyKey, member);
        Double dailyScore = zSetOperations.score(dailyKey, member);

        // Weekly
        String weeklyKey = getWeeklyLeaderboardKey();
        Long weeklyRank = zSetOperations.reverseRank(weeklyKey, member);
        Double weeklyScore = zSetOperations.score(weeklyKey, member);

        // Total players
        Long totalPlayers = zSetOperations.size(GLOBAL_LEADERBOARD);

        if (globalRank == null || globalScore == null) {
            log.warn("❌ User leaderboard-da tapılmadı: user:{}", userId);
            return null;
        }

        User user = userRepository.findById(userId).orElse(null);

        return UserRankInfo.builder()
                .userId(userId)
                .username(user != null ? user.getUsername() : null)
                .avatar(user != null ? user.getAvatar() : null)
                .globalRank(globalRank.intValue() + 1)
                .globalScore(globalScore.longValue())
                .dailyRank(dailyRank != null ? dailyRank.intValue() + 1 : null)
                .dailyScore(dailyScore != null ? dailyScore.longValue() : null)
                .weeklyRank(weeklyRank != null ? weeklyRank.intValue() + 1 : null)
                .weeklyScore(weeklyScore != null ? weeklyScore.longValue() : null)
                .totalPlayers(totalPlayers != null ? totalPlayers.intValue() : 0)
                .percentile(calculatePercentile(globalRank.intValue(), totalPlayers))
                .tier(calculateTier(globalRank.intValue()))
                .build();
    }

    private String getUserMember(Long userId) {
        return "user:" + userId;
    }

    private Double calculatePercentile(int rank, Long totalPlayers) {
        if (totalPlayers == null || totalPlayers == 0) {
            return 0.0;
        }
        return ((totalPlayers - rank) * 100.0) / totalPlayers;
    }

    /**
     * Tier/Badge teyin et
     */
    private String calculateTier(int rank) {
        if (rank == 1) return "🥇 Champion";
        if (rank <= 3) return "🥈 Master";
        if (rank <= 10) return "🥉 Diamond";
        if (rank <= 50) return "⭐ Platinum";
        if (rank <= 100) return "💎 Gold";
        if (rank <= 500) return "🎖️ Silver";
        return "🏅 Bronze";
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

    private String getWeeklyLeaderboardKey() {
        LocalDate now = LocalDate.now();
        int weekOfYear = now.getDayOfYear();
        int week = weekOfYear / 7;
        return WEEKLY_LEADERBOARD_PREFIX + now.getYear() + "-W" + weekOfYear;
    }

    private String getGameLeaderboardKey(Long gameId) {
        return GAME_LEADERBOARD_PREFIX + gameId;
    }

    /**
     * TypedTuple-ları LeaderboardEntry-lara cevir
     * burada rank onlarin necenci yerde oldugunu gorsedir. onuste biz melumatlari cekende en cox xali olar basda gelirdi ona gore rank 1 den basdayir en cox xali olana rank bir her loop da artirirq bunu 2,3 ,4 kimi bucur isleyir
     */
    private List<LeaderboardEntry> buildLeaderboardEntries(Set<ZSetOperations.TypedTuple<Object>> results, int startRank) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        int currentRank = startRank;
        for (ZSetOperations.TypedTuple<Object> tuple : results) {
            String member = tuple.getValue().toString();
            Long userId = extractUserId(member);
            Long score = tuple.getScore() != null ? tuple.getScore().longValue() : 0L;
            User user = userRepository.findById(userId).orElse(null);

            LeaderboardEntry entry = LeaderboardEntry.builder()
                    .rank(currentRank++)
                    .userId(userId)
                    .username(user != null ? user.getUsername() : "Unknown")
                    .avatar(user != null ? user.getAvatar() : null)
                    .score(score)
                    .level(user != null ? user.getLevel() : 1)
                    .build();

            entries.add(entry);


        }

        return entries;

    }

    private Long extractUserId(String member) {
        return Long.parseLong(member.replace("user:", ""));
    }
}
