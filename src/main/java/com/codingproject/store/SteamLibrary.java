package com.codingproject.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SteamLibrary {

    @Value("${steam.api.key}")
    private String apiKey;

    @Value("${steam.user.id}")
    private String userId;

    private final GameRepository repo;
    private final RestTemplate rest;

    public SteamLibrary(GameRepository repo){
        this.repo = repo;
        this.rest = new RestTemplate();
    }

    public void syncLibrary(){

        if(apiKey == null || apiKey.isEmpty() || userId == null || userId.isEmpty()){

            System.out.println("API Key or User ID missing.");
            return;
        }

        String url = String.format(
                "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=%s&steamid=%s&format=json&include_appinfo=true",
                apiKey, userId
        );

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = rest.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if(body != null && body.containsKey("response")){

                Map<String, Object> responseData = (Map<String, Object>) body.get("response");
                List<Map<String, Object>> gameList = (List<Map<String, Object>>) responseData.get("games");

                if(gameList == null) return;

                for(Map<String, Object> game : gameList){

                    Long appId = ((Number) game.get("appid")).longValue();
                    String name = (String) game.get("name");
                    Number minutes = (Number) game.getOrDefault("playtime_forever", 0);
                    double hours = Math.round((minutes.doubleValue() / 60) * 10.0) / 10.0;

                    //Just checking if the game exists in our H2 Database
                    Optional<Game> existingGame = repo.findAll().stream()
                            .filter(g -> g.getSteamAppId() != null && g.getSteamAppId().equals(appId))
                            .findFirst();

                    if(existingGame.isPresent()){

                        Game g = existingGame.get();
                        g.setHoursPlayed(hours);
                        if("UNPLAYED".equals(g.getStatus()) && hours > 0){
                            g.setStatus("IN_PROGRESS");
                        }
                        repo.save(g);
                    }
                    else{

                        Game newGame = new Game(name, "Steam Import", 0.00);
                        newGame.setSteamAppId(appId);
                        newGame.setHoursPlayed(hours);
                        newGame.setStatus(hours > 0 ? "IN_PROGRESS" : "UNPLAYED");
                        repo.save(newGame);
                    }
                }
                System.out.println("Imported " + gameList.size() + " owned games from Steam.");
            }
        } catch (Exception e) {
            System.out.println("Couldn't sync Steam library: " + e.getMessage());
        }
    }
}
