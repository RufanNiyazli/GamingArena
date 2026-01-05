package com.project.redis.service.impl;

import com.project.redis.dto.response.UserProfileResponse;
import com.project.redis.model.User;
import com.project.redis.repository.UserRepository;
import com.project.redis.service.IUserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service

@Slf4j
public class UserProfileServiceImpl implements IUserProfileService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOps;

    public UserProfileServiceImpl(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    @Override
    public UserProfileResponse readUserProfile(Long userId) {

        String redisKey = "user:" + userId;

        Map<String, Object> cached = hashOps.entries(redisKey);

        if (!cached.isEmpty()) {
            return mapFromRedis(cached);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileResponse response = mapToUserProfileResponse(user);

        Map<String, Object> map = new HashMap<>();
        map.put("username", response.getUsername());
        map.put("email", response.getEmail());
        map.put("avatar", response.getAvatar());
        map.put("totalScore", response.getTotalScore());
        map.put("level", response.getLevel());
        map.put("totalMatches", response.getTotalMatches());
        map.put("wins", response.getWins());
        map.put("losses", response.getLosses());
        map.put("lastLogin", response.getLastLogin());

        hashOps.putAll(redisKey, map);
        redisTemplate.expire(redisKey, Duration.ofMinutes(5));

        return response;
    }

    private UserProfileResponse mapFromRedis(Map<String, Object> cached) {
        return UserProfileResponse.builder()
                .username((String) cached.get("username"))
                .email((String) cached.get("email"))
                .avatar((String) cached.get("avatar"))
                .totalScore(castToLong(cached.get("totalScore")))
                .level(castToInt(cached.get("level")))
                .totalMatches(castToInt(cached.get("totalMatches")))
                .wins(castToInt(cached.get("wins")))
                .losses(castToInt(cached.get("losses")))
                .winRate(castToDouble(cached.get("winRate")))
                .lastLogin(cached.get("lastLogin") != null ? LocalDateTime.parse((String) cached.get("lastLogin")) : null)
                .build();
    }

    private Integer castToInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Long) return ((Long) obj).intValue();
        return Integer.parseInt(obj.toString());
    }

    private Long castToLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private Double castToDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Double) return (Double) obj;
        if (obj instanceof Float) return ((Float) obj).doubleValue();
        return Double.parseDouble(obj.toString());
    }

    public UserProfileResponse mapToUserProfileResponse(User user) {

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .totalScore(user.getTotalScore())
                .level(user.getLevel())
                .totalMatches(user.getTotalMatches())
                .wins(user.getWins())
                .winRate(user.getWinRate())
                .losses(user.getLosses())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
