package com.example.dogfinder.Entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comment implements Comparable<Comment>{
    private String id;
    private String parentId;
    private String userId;
    private String postId;
    private String content;
    private String time;
    public Comment(){

    }
    public Comment(String id,String userId,String postId,String content,String time){
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int compareTo(Comment o) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = sdf.parse(this.getTime());
            d2 = sdf.parse(o.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(d1.before(d2)){
            return 1;
        }else{
            return -1;
        }
    }
}
