package com.example.thebechain.ViewHolders;

public class ImageModel {
    public String imgUrl, id;

    public ImageModel(String id, String imgUrl) {
        this.imgUrl = imgUrl;
        this.id = id;
    }

    public ImageModel() {
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getKeyId() {
        return id;
    }
}
