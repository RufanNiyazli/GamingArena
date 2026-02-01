package com.project.redis.repository;

import com.project.redis.enums.GameStatus;
import com.project.redis.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByStatusOrderByPlayCountDesc();

    List<Game> findByCategoryAndStatus(String category, GameStatus status);
}
