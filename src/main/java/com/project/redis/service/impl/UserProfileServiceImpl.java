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

@Service

@Slf4j
public class UserProfileServiceImpl implements IUserProfileService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOps;

    public UserProfileServiceImpl(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate, HashOperations<String, String, Object> hashOps) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

        @Override
        @Cacheable(value = "users", key = "#userId")
        public UserProfileResponse readUserProfile(Long userId) {

            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("This user not found!"));

            UserProfileResponse userProfileResponse = mapToUserProfileResponse(user);

            String redisKey = "cache::user:" + userId;

            hashOps.put(redisKey, "username", userProfileResponse.getUsername());

            return null;
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
