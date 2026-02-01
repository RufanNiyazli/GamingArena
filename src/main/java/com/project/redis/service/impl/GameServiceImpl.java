package com.project.redis.service.impl;

import com.project.redis.dto.request.GameSession;
import com.project.redis.dto.response.GameSessionResponse;
import com.project.redis.enums.GameStatus;
import com.project.redis.exception.GameNotFoundException;
import com.project.redis.exception.UserNotFoundException;
import com.project.redis.model.Game;
import com.project.redis.model.User;
import com.project.redis.repository.GameRepository;
import com.project.redis.repository.UserRepository;
import com.project.redis.service.IGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements IGameService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @Override
    public List<Game> getAllGames() {
        String cacheKey = "games:all:active";
        List<Game> cached = (List<Game>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT: {}", cacheKey);
            return cached;
        }
        List<Game> games = gameRepository.findByStatusOrderByPlayCountDesc();
        redisTemplate.opsForValue().set(cacheKey, games, 5, TimeUnit.MINUTES);

        return games;
    }

    @Override
    public List<Game> getAllGamesByCategory(String category) {
        String cacheKey = "games:category:" + category;
        List<Game> cached = (List<Game>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT: {}", cacheKey);
            return cached;

        }
        List<Game> games = gameRepository.findByCategoryAndStatus(category, GameStatus.ACTIVE);
        redisTemplate.opsForValue().set(cacheKey, games, 5, TimeUnit.MINUTES);

        return games;
    }

    @Override
    public Game getGameById(Long id) throws GameNotFoundException {
        String cacheKey = "games:detail:" + id;
        Game cached = (Game) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Game game = gameRepository.findById(id).orElseThrow(() -> new GameNotFoundException("GAme not found!: " + id));

        redisTemplate.opsForValue().set(cacheKey, game, 5, TimeUnit.MINUTES);

        return game;
    }

    @Override
    public GameSessionResponse startGame(Long userId, Long gameId) throws GameNotFoundException, UserNotFoundException {
        Game game = getGameById(gameId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("UserNotFound:{}" + userId));
        String activeSessionKey = "active-session:user:" + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(activeSessionKey))) {
            throw new RuntimeException("There is already a game! ");

        }
        String sessionId = generateSessionId();
        GameSession gameSession = GameSession.builder()
                .gameId(gameId)
                .sessionId(sessionId)
                .startedAt(LocalDateTime.now())
                .userId(userId)
                .build();
        String sessionKey = "game-session:" + sessionId;
        redisTemplate.opsForValue().set(sessionKey, gameSession, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(activeSessionKey, sessionId, 30, TimeUnit.MINUTES);


        return GameSessionResponse.builder()
                .gameId(gameId)
                .gameName(game.getName())
                .sessionId(sessionId)
                .startedAt(gameSession.getStartedAt())
                .build();
    }

    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);

    }


}
