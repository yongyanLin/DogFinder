package com.example.dogfinder.Entity;

public class LostDog {
    private String userId;
    private String breed;
    private String condition;
    private String behavior;
    private String color;
    private String size;
    private String imageUrl;
    private String location;
    private String description;
    public LostDog(String userId,String breed,String condition,String behavior,String color,String imageUrl,String location,String description){
        this.userId = userId;
        this.breed = breed;
        this.condition = condition;
        this.behavior = behavior;
        this.color = color;
        this.imageUrl = imageUrl;
        this.location = location;
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
