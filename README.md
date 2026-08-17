# Steam Backlog & AI Game Advisor

A web application that tracks your Steam game library progress, visualizes analytics, and provides AI-driven game recommendations with live deal tracking.

## Features
* **Steam Library Sync:** Automatically imports and tracks owned Steam games and playtime.
* **AI Game Advisor:** Generates real-time, personalized game recommendations using the Google Gemini API.
* **Live Deal Tracking:** Fetches real-time game prices and discounts via the CheapShark API.
* **Analytics Dashboard:** Visualizes library statistics and playtime metrics.

## Prerequisites
To run this project, you will need the following:
* **Google Gemini API Key:** Required for the AI Game Advisor. 
* **Steam Web API Key:** Required to authenticate requests to Steam.
* **Steam ID64:** Your 17-digit Steam code is required to fetch your library. Ensure your Steam profile and game details are set to "Public".
* **CheapShark API:** Works out of the box; no configuration or personal API key is required.

## Installation and Setup

1. Clone the repository to your local machine:

2. Configure your environment variables in src/main/resources/application.properties:

   gemini.api.key=YOUR_GEMINI_API_KEY
   steam.api.key=YOUR_STEAM_API_KEY
   steam.user.id=YOUR_17_DIGIT_STEAM_ID

   (Note: The steam.user.id is an optional default; it can also be entered dynamically in the UI).

3. Build and run the Spring Boot application using Maven:

    mvn spring-boot:run
    Access the web interface at http://localhost:8080.

## Usage
Enter your 17-digit Steam ID in the top input field and click "Sync Steam Profile" to load your library. Use the AI Game Advisor to ask for tailored recommendations based on your synced catalog, playtime, or budget.
