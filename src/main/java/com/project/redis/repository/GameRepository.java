package com.project.redis.repository;

import com.project.redis.enums.GameStatus;
import com.project.redis.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByStatusOrderByPlayCountDesc(GameStatus status);

    List<Game> findByCategoryAndStatus(String category, GameStatus status);
}
