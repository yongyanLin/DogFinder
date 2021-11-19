package com.example.dogfinder.Entity;

public class TokenData {
    String userId;
    String token;
    public TokenData(){

    }
    public TokenData(String userId,String token){
        this.userId = userId;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
