package com.annmonstar.androidauctionapp.Models;

public class BiddingModal {
    private String bid, uid, name, timeStamp, image;

    public BiddingModal() {

    }

    public BiddingModal(String bid, String uid, String name, String timeStamp, String image) {
        this.bid = bid;
        this.uid = uid;
        this.name = name;
        this.image = image;
        this.timeStamp = timeStamp;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
