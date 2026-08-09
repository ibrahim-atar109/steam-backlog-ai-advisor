package com.codingproject.store;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GameRepository repository;

    public DataInitializer(GameRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        InputStream is = getClass().getResourceAsStream("/games.csv");

        if (is == null) {
            System.out.println("❌ ERROR: Could not find 'games.csv' in src/main/resources/");
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean firstLine = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip header

                String[] data = line.split(",");

                if (data.length >= 3) {
                    try {
                        String title = data[0].trim();

                        // Fix: Price is index 1, Developer is index 2
                        String rawPrice = data[1].trim().replace("$", "");
                        double price = rawPrice.equalsIgnoreCase("Free") || rawPrice.isEmpty()
                                ? 0.0
                                : Double.parseDouble(rawPrice);

                        String developer = data[2].trim();

                        repository.save(new Game(title, developer, price));
                        count++;
                    } catch (Exception rowError) {
                        // Skip any single broken row without breaking the whole process
                        System.out.println("Skipped row due to parse error: " + line);
                    }
                }
            }
            System.out.println("✅ Loaded " + count + " games into H2 database successfully!");
        } catch (Exception e) {
            System.out.println("Could not load CSV: " + e.getMessage());
        }
    }
}