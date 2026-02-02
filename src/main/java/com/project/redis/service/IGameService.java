package com.project.redis.service;

import com.project.redis.dto.request.FinishGameRequest;
import com.project.redis.dto.response.GameSessionResponse;
import com.project.redis.dto.response.MatchResult;
import com.project.redis.exception.GameNotFoundException;
import com.project.redis.exception.UnauthorizedException;
import com.project.redis.exception.UserNotFoundException;
import com.project.redis.model.Game;

import java.util.List;

public interface IGameService {
    public List<Game> getAllGames();

    public List<Game> getAllGamesByCategory(String category);

    public Game getGameById(Long id) throws GameNotFoundException;

    public GameSessionResponse startGame(Long userId, Long gameId) throws GameNotFoundException, UserNotFoundException;

    public MatchResult finishGame(Long userId, FinishGameRequest request) throws UnauthorizedException, UserNotFoundException;


}
