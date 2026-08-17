package com.codingproject.store;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping(value = "/ai-recommend", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getAiRec(@RequestParam String prompt){
        SseEmitter emitter = new SseEmitter(60000L);
        service.getRec(prompt, emitter);
        return emitter;
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
    public ResponseEntity<Map<String, Object>> syncUserLibrary(@RequestParam(required = false) String steamId){

        int count = steamLibrary.syncLibrary(steamId);
        return ResponseEntity.ok(Map.of(
                "success", count > 0,
                "count", count,
                "message", count > 0 ? "Synced " + count + " games!" : "No games found. Ensure Steam Profile & Game details are set to PUBLIC."
        ));
    }
}
