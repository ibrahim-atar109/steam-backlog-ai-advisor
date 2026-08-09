package com.codingproject.store;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String developer;
    private double price;

    //Backlog fields
    private String status; // "UNPLAYED", "IN_PROGRESS", "COMPLETED"
    private double hoursPlayed;
    private int userRating;

    public Game() {}

    public Game(String title, String developer, double price) {

        this.title = title;
        this.developer = developer;
        this.price = price;
        this.status = "UNPLAYED";
        this.hoursPlayed = 0;
        this.userRating = 0;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDeveloper() { return developer; }
    public double getPrice() { return price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getHoursPlayed() { return hoursPlayed; }
    public void setHoursPlayed(double hoursPlayed) { this.hoursPlayed = hoursPlayed; }

    public int getUserRating() { return userRating; }
    public void setUserRating(int userRating) { this.userRating = userRating; }
}
