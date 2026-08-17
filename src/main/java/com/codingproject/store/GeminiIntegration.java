package com.codingproject.store;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class GeminiIntegration {

    @Value("${gemini.model:gemini-flash-latest}")
    private String model;

    private final GameRepository repository;
    private final Client client;

    public GeminiIntegration(GameRepository repository, @Value("${gemini.api.key}") String apiKey) {
        this.repository = repository;
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String getRec(String prompt, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {

                emitter.send(SseEmitter.event().comment("connected"));

                List<Game> games = repository.findAll();

                String catalogText = games.stream()
                        .limit(5)
                        .map(g -> String.format("- %s by %s ($%.2f)", g.getTitle(), g.getDeveloper(), g.getPrice()))
                        .collect(Collectors.joining("\n"));

                String fullPrompt = "You are a gaming advisor assistant. Recommend games firstly from the provided catalog below based on the user's preference. Then recommend games from Steam in general.\n" +
                        "Catalog:\n" + catalogText + "\n\n" +
                        "User Request: " + prompt + "\n\n" +
                        "Provide a concise recommendation (2-3 sentences) explaining why these games fit their request. Do not use asterisks (*).";

                GenerateContentConfig config = GenerateContentConfig.builder()
                        .systemInstruction(Content.builder()
                                .parts(List.of(Part.fromText("You are a gaming advisor. Recommend catalog games first, then general Steam games. Concise (2-3 sentences). No asterisks (*).")))
                                .build())
                        .temperature(0.2f)
                        .build();

                var stream = client.models.generateContentStream(
                        model,
                        fullPrompt,
                        config
                );

                for (GenerateContentResponse res : stream) {
                    if (res != null && res.text() != null) {
                        String text = res.text().replace("*", "");
                        emitter.send(SseEmitter.event().data(java.util.Map.of("text", text)));
                    }
                }

                emitter.complete();

            } catch (Exception e) {
                System.out.println("Gemini API issue: " + e.getMessage());
                e.printStackTrace();
                try {
                    emitter.send(SseEmitter.event().data(java.util.Map.of("text", "Unable to connect.")));
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        });
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