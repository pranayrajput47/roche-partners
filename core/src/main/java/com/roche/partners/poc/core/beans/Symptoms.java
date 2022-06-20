package com.roche.partners.poc.core.beans;

public class Symptoms {

    private String heading;
    private String description;

    public Symptoms(String heading, String description) {
        this.heading = heading;
        this.description = description;
    }

    public String getHeading() {
        return heading;
    }

    public String getDescription() {
        return description;
    }
}
