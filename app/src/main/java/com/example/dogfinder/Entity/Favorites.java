package com.example.dogfinder.Entity;

public class Favorites {
    private String id;
    private String userId;
    //private String postId;
    private Dog dog;
    private String time;
    public Favorites(){

    }
    public Favorites(String userId, Dog dog,String time){
        this.userId = userId;
        this.dog = dog;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
