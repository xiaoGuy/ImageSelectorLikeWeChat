package com.xiaoguy.imageselector.bean;

import java.util.ArrayList;

/**
 * Created by XiaoGuy on 2016/10/9.
 */

public class ImageFolder {

    private int imageCount;
    private String path;
    private String firstImage;
    private String name;
    private ArrayList<String> images;

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getFirstImage() {
        return firstImage;
    }

    public void setFirstImage(String firstImage) {
        this.firstImage = firstImage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ImageFolder{" +
                "imageCount=" + imageCount +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
