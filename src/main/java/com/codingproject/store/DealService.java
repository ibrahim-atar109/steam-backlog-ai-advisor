package com.codingproject.store;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DealService {

    private final RestTemplate restTemplate;

    public DealService() {
        this.restTemplate = new RestTemplate();
    }

    public DealDto getBestDealForGame(String gameTitle) {
        if (gameTitle == null || gameTitle.trim().isEmpty()) {
            return null;
        }

        // Clean title: Remove symbols like ®, ™, ©, and curly apostrophes that break CheapShark search
        String cleanTitle = gameTitle.replaceAll("[®™©]", "")
                .replace("’", "'")
                .trim();

        String url = UriComponentsBuilder.fromUriString("https://www.cheapshark.com/api/1.0/deals")
                .queryParam("title", cleanTitle)
                .queryParam("limit", 1)
                .toUriString();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "SteamBacklogTracker/1.0 (student-project)");

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<DealDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    DealDto[].class
            );

            DealDto[] deals = response.getBody();
            if (deals != null && deals.length > 0) {
                return deals[0];
            } else {
                System.out.println("No CheapShark deals found for query: " + cleanTitle);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error fetching deal for '" + cleanTitle + "':");
            e.printStackTrace(); // Prints full error stack trace in IntelliJ console
        }
        return null;
    }
}