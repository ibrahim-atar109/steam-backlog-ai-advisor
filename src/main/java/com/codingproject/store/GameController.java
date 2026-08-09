package com.codingproject.store;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameRepository repository;
    private final DealService dealService;

    public GameController(GameRepository repository, DealService dealService) {
        this.repository = repository;
        this.dealService = dealService;
    }

    @GetMapping
    public List<Game> getGames() {
        return repository.findAll();
    }

    //Find games with the desired price
    @GetMapping("/under")
    public List<Game> getGamesUnderBudget(@RequestParam double maxPrice){
        return repository.findByPriceLessThanEqual(maxPrice);
    }

    //Search by title
    @GetMapping("/search")
    public List<Game> searchGames(@RequestParam String query){
        return repository.findByTitleContainingIgnoreCase(query);
    }

    //Live deal check using CheapShark
    @GetMapping("/deal")
    public DealDto getLiveDeal(@RequestParam String title){
        return dealService.getBestDealForGame(title);
    }

    //Update both hours played and status
    @PutMapping("/{id}/log-hours")
    public Game logHours(@PathVariable Long id, @RequestParam double hours, @RequestParam String status){

        Game game = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game with id " + id + " not found"));

        game.setHoursPlayed(game.getHoursPlayed() + hours);
        game.setStatus(status);

        return repository.save(game);
    }
}
