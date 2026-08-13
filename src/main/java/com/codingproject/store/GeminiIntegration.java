package com.codingproject.store;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiIntegration {

    private final GameRepository repository;
    private final Client client;

    public GeminiIntegration(GameRepository repository, @Value("${gemini.api.key}") String apiKey) {
        this.repository = repository;
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String getRec(String prompt) {
        try {
            List<Game> games = repository.findAll();

            String catalogText = games.stream()
                    .limit(25)
                    .map(g -> String.format("- %s by %s ($%.2f)", g.getTitle(), g.getDeveloper(), g.getPrice()))
                    .collect(Collectors.joining("\n"));

            String fullPrompt = "You are a gaming advisor assistant. Recommend games that are ONLY from the provided catalog below based on the user's preference.\n" +
                    "Catalog:\n" + catalogText + "\n\n" +
                    "User Request: " + prompt + "\n\n" +
                    "Provide a concise recommendation (2-3 sentences) explaining why these games fit their request.";


            GenerateContentResponse response = client.models.generateContent(
                    "gemini-flash-latest",
                    fullPrompt,
                    null
            );

            if (response != null && response.text() != null) {
                return response.text();
            }

        } catch (Exception e) {
            System.out.println("Google Gen AI Error: " + e.getMessage());
            return getLocalFallback(prompt);
        }

        return "No recommendation found.";
    }

    private String getLocalFallback(String prompt) {
        List<Game> matches = repository.findByTitleContainingIgnoreCase(prompt);
        if (!matches.isEmpty()) {
            Game game = matches.get(0);
            return "Based on your store catalog, check out " + game.getTitle() +
                    " by " + game.getDeveloper() + " ($" + game.getPrice() + ")!";
        }
        return "Unable to connect to AI Advisor.";
    }
}