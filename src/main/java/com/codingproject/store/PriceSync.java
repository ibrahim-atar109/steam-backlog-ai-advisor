package com.codingproject.store;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PriceSync {

    private final GameRepository repo;
    private final RestTemplate rest;

    public PriceSync(GameRepository repo){
        this.repo = repo;
        this.rest = new RestTemplate();
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void syncLive(){

        List<Game> games = repo.findAll();

        List<Game> steamGames = games.stream()
                .filter(g -> g.getSteamAppId() != null && g.getSteamAppId() > 0)
                .limit(40)
                .collect(Collectors.toList());

        if(steamGames.isEmpty()) return;

        int updatedCount = 0;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        for(Game game : steamGames) {

            try {

                Thread.sleep(1500);

                String url = "https://store.steampowered.com/api/appdetails?appids="
                        + game.getSteamAppId() + "&cc=us&filters=price_overview,basic";

                ResponseEntity<Map> response = rest.exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity,
                        Map.class
                );

                Map<String, Object> body = response.getBody();
                String appId = String.valueOf(game.getSteamAppId());

                if (body != null && body.containsKey(appId)) {

                    Map<String, Object> appObj = (Map<String, Object>) body.get(appId);

                    if (appObj != null && Boolean.TRUE.equals(appObj.get("success"))) {
                        Map<String, Object> data = (Map<String, Object>) appObj.get("data");

                        if (data != null) {
                            Boolean free = (Boolean) data.get("is_free");
                            if (Boolean.TRUE.equals(free)) {
                                game.setPrice(0.00);
                                repo.save(game);
                                updatedCount++;
                                continue;
                            }

                            Map<String, Object> priceOverview = (Map<String, Object>) data.get("price_overview");
                            if (priceOverview != null && priceOverview.containsKey("final")) {
                                Number finalCents = (Number) priceOverview.get("final");
                                double livePrice = finalCents.doubleValue() / 100.00;

                                game.setPrice(livePrice);
                                repo.save(game);
                                updatedCount++;
                            }
                        }
                    }
                }
            } catch (HttpClientErrorException.TooManyRequests e) {

                System.out.println("Steam API rate limit hit");
                break;
            } catch (Exception e) {
                System.out.println("Error for " + game.getSteamAppId() + ": " + e.getMessage());
            }
        }
        System.out.println("Updated Games: " + updatedCount);
    }
}
