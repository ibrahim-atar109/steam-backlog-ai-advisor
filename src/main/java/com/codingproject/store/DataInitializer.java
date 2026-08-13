package com.codingproject.store;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GameRepository repository;

    public DataInitializer(GameRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/games.csv"), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (parts.length >= 4) {
                    String title = parts[0].replaceAll("^\"|\"$", "").trim();

                    String rawPrice = parts[1].replaceAll("^\"|\"$", "").replaceAll("[^0-9.]", "").trim();
                    double price = rawPrice.isEmpty() ? 0.0 : Double.parseDouble(rawPrice);

                    String rawAppId = parts[2].replaceAll("^\"|\"$", "").replaceAll("[^0-9]", "").trim();
                    Long steamAppId = rawAppId.isEmpty() ? null : Long.parseLong(rawAppId);

                    String developer = parts[3].replaceAll("^\"|\"$", "").trim();

                    Game game = new Game(title, developer, price);
                    game.setSteamAppId(steamAppId);

                    repository.save(game);
                }
            }
            System.out.println("✅ Successfully loaded games");
        } catch (Exception e) {
            System.out.println("Error loading CSV data: " + e.getMessage());
        }
    }
}