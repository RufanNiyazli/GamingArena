package com.project.redis.service.impl;

import com.project.redis.dto.UserProfileResponse;
import com.project.redis.dto.UserResponse;
import com.project.redis.exception.UserNotFoundException;
import com.project.redis.model.User;
import com.project.redis.repository.UserRepository;
import com.project.redis.service.IUserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    @Cacheable(value = "user-profile", key = "'user:' + #userId + ':profile'")
    public UserProfileResponse readUserProfile(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("This user not found!"));

        UserProfileResponse userProfileResponse = mapToUserProfileResponse(user);

        String redisKey = "cache::user:" + userId;


        Map<String, Object> map = new HashMap<>();
        map.put("username", userProfileResponse.getUsername());
        map.put("email", userProfileResponse.getEmail());
        map.put("avatar", userProfileResponse.getAvatar());
        map.put("totalScore", userProfileResponse.getTotalScore());
        map.put("level", userProfileResponse.getLevel());
        map.put("totalMatches", userProfileResponse.getTotalMatches());
        map.put("wins", userProfileResponse.getWins());
        map.put("winRate", userProfileResponse.getWinRate());
        map.put("losses", userProfileResponse.getLosses());
        map.put("lastLogin", userProfileResponse.getLastLogin());

        hashOps.putAll(redisKey, map);
        redisTemplate.expire(redisKey, Duration.ofMinutes(5));

        return userProfileResponse;
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
