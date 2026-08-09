package com.codingproject.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    //Spring auto-generates SQL for these based on the method names
    List<Game> findByPriceLessThanEqual(double maxPrice);

    List<Game> findByTitleContainingIgnoreCase(String keyword);

    List<Game> findByStatus(String status);
}
