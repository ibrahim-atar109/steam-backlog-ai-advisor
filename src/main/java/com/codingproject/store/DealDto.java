package com.codingproject.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DealDto {

    private String title;
    private String normalPrice;
    private String salePrice;
    private String savings;
    private String dealID;

    public DealDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNormalPrice() { return normalPrice; }
    public void setNormalPrice(String normalPrice) { this.normalPrice = normalPrice; }

    public String getSalePrice() { return salePrice; }
    public void setSalePrice(String salePrice) { this.salePrice = salePrice; }

    public String getSavings() { return savings; }
    public void setSavings(String savings) { this.savings = savings; }

    public String getDealID() { return dealID; }
    public void setDealID(String dealID) { this.dealID = dealID; }
}
