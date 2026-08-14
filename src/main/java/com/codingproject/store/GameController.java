package com.codingproject.store;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameRepository repository;
    private final DealService dealService;
    private final GeminiIntegration service;
    private final SteamLibrary steamLibrary;

    public GameController(GameRepository repository, DealService dealService, GeminiIntegration service, SteamLibrary steamLibrary) {
        this.repository = repository;
        this.dealService = dealService;
        this.service = service;
        this.steamLibrary = steamLibrary;
    }

    @GetMapping
    public List<Game> getGames() {
        return repository.findAll();
    }

    @GetMapping("/under")
    public List<Game> getGamesUnderBudget(@RequestParam double maxPrice){
        return repository.findByPriceLessThanEqual(maxPrice);
    }

    @GetMapping("/search")
    public List<Game> searchGames(@RequestParam String query){
        return repository.findByTitleContainingIgnoreCase(query);
    }

    @GetMapping("/status")
    public List<Game> getGameStatus(@RequestParam String status){
        return repository.findByStatus(status);
    }

    //Live deal check using CheapShark
    @GetMapping("/deal")
    public DealDto getLiveDeal(@RequestParam String title){
        return dealService.getBestDealForGame(title);
    }

    @GetMapping("/ai-recommend")
    public Map<String, String> getAiRec(@RequestParam String prompt){

        String rec = service.getRec(prompt);
        return Map.of("recommendation", rec);
    }

    @PutMapping("/{id}/log-hours")
    public Game logHours(@PathVariable Long id, @RequestParam double hours, @RequestParam String status){

        Game game = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game with id " + id + " not found"));

        game.setHoursPlayed(game.getHoursPlayed() + hours);
        game.setStatus(status);

        return repository.save(game);
    }

    @PostMapping("/sync-user-library")
    public ResponseEntity<String> syncUserLibrary(){

        steamLibrary.syncLibrary();
        return ResponseEntity.ok("Library synced.");
    }
}
