package com.example.dogfinder.Entity;

import java.io.Serializable;
import java.util.Objects;

public class Dog implements Serializable {
    private String id;
    private String userId;
    private String type;
    private String time;
    private String breed;
    private String condition;
    private String behavior;
    private String color;
    private String size;
    private String imageUrl;
    private String location;
    private String description;
    public Dog(){

    }
    public Dog(String userId,String type,String time,String breed,String condition,String behavior,String size,String color,String imageUrl,String location,String description){
        this.userId = userId;
        this.type = type;
        this.time = time;
        this.breed = breed;
        this.condition = condition;
        this.behavior = behavior;
        this.size = size;
        this.color = color;
        this.imageUrl = imageUrl;
        this.location = location;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dog dog = (Dog) o;
        return Objects.equals(id, dog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, type, time, breed, condition, behavior, color, size, imageUrl, location, description);
    }
}
