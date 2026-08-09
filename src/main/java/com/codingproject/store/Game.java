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


    public Game() {}

    public Game(String title, String developer, double price) {

        this.title = title;
        this.developer = developer;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDeveloper() { return developer; }
    public double getPrice() { return price; }
}
