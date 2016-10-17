package com.xiaoguy.imageselector.bean;

import java.util.List;

/**
 * Created by XiaoGuy on 2016/10/9.
 */

public class ImageFolder {

    private int imageCount;
    private String path;
    private String firstImagePath;
    private String name;
    private List<String> imagePathList;

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

    public List<String> getImagePaths() {
        return imagePathList;
    }

    public void setImagePaths(List<String> imagePathList) {
        this.imagePathList = imagePathList;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
