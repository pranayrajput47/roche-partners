package com.roche.partners.poc.core.beans;

public class Roche {
    private String description;
    private String image;
    private String link;

    public Roche(String description, String image, String link) {
        this.description = description;
        this.image = image;
        this.link = link;
    }
    public String getDescription() {
        return description;
    }
    public String getImage() {
        return image;
    }
    public String getLink() {
        return link;
    }
}



