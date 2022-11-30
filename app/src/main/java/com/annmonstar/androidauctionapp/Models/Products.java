package com.annmonstar.androidauctionapp.Models;

public class Products {
    private String name, description, days, uid, bid, timestamp, status, mainImage, winner, approve;

    public Products() {

    }

    public Products(String name, String description, String days, String uid, String bid, String timestamp, String status, String mainImage, String winner, String approve) {
        this.name = name;
        this.description = description;
        this.days = days;
        this.uid = uid;
        this.timestamp = timestamp;
        this.status = status;
        this.winner = winner;
        this.bid = bid;
        this.mainImage = mainImage;
        this.approve = approve;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setImageUrl(String image) {
        this.mainImage = image;
    }

    public String getMainImageUrl() {
        return this.mainImage;
    }

    public String getApprove() {
        return approve;
    }

    public void setApprove(String approve) {
        this.approve = approve;
    }
}
