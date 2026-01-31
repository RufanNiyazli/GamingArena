package com.project.redis.service.impl;

import com.project.redis.model.Game;
import com.project.redis.repository.GameRepository;
import com.project.redis.service.IGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements IGameService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final GameRepository gameRepository;

    @Override
    public List<Game> getAllGames() {
        String cacheKey = "games:all:active";
        List<Game> games = (List<Game>) redisTemplate.opsForValue().get(cacheKey);
        return List.of();
    }
}
