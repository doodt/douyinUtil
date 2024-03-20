package com.jinmgr.entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class MediaModule implements Serializable {
    //标题
    private String title;
    //所在目录
    private String dir;
    //日期
    private String dateTime;
    //预览图片地址
    private String imageUrl;
    //原始视频文件地址
    private String mediaUrl;
    //原始视频文件本地地址
    private String mediaLocalPath;
    //m3u8文件地址
    private String m3u8Url;
    //集数
    private int playIndex;
    //备注
    private String text;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getM3u8Url() {
        return m3u8Url;
    }

    public MediaModule setM3u8Url(String m3u8Url) {
        this.m3u8Url = m3u8Url;
        return this;
    }

    public String getDir() {
        return dir;
    }

    public MediaModule setDir(String dir) {
        this.dir = dir;
        return this;
    }

    public String getMediaLocalPath() {
        return mediaLocalPath;
    }

    public MediaModule setMediaLocalPath(String mediaLocalPath) {
        this.mediaLocalPath = mediaLocalPath;
        return this;
    }

    public int getPlayIndex() {
        return playIndex;
    }

    public MediaModule setPlayIndex(int playIndex) {
        this.playIndex = playIndex;
        return this;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
