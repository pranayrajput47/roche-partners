package com.roche.partners.poc.core.beans;

public class Cures {

    private String heading;
    private String description;


    public Cures(String heading, String description) {
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
