package com.example.asus.firebasechatapp;

/**
 * Created by asus on 14/10/2017.
 */

public class ChatModel {
    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private long timestamp;

    public ChatModel(){

    }

    public ChatModel(String text, String name, String photoUrl, long timestamp){
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
