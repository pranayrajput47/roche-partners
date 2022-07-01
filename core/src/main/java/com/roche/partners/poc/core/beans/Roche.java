package com.roche.partners.poc.core.beans;

import java.util.List;

public class Roche {
    private String description;
    private String image;
    private String imageAltText;
    private List<String> subStagesList;
    private String viewType;

    public Roche(String description, String image, String imageAltText, List<String> subStagesList, String viewType) {
        this.description = description;
        this.image = image;
        this.imageAltText = imageAltText;
        this.subStagesList = subStagesList;
        this.viewType = viewType;
    }
    public String getDescription() {
        return description;
    }
    public String getImage() {
        return image;
    }
    public String getImageAltText() {
        return imageAltText;
    }
    public List<String> getSubStagesList() { return subStagesList;}
    public String getViewType() {return viewType;}
}



