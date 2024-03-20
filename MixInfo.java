package com.jinmgr.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jinmgr.annotations.DatabaseIgnore;

import java.io.Serializable;
import java.util.List;

/**
 * 抖音视频集合实体
 *
 * @author LiuChenghui 2024/03/14 18:06:00
 * @version 1.0
 */
public class MixInfo extends BaseEntity implements Serializable {
    private String mixId;     //aweme_list[0].mix_info.mix_id   集合ID
    private String mixName;   //aweme_list[0].mix_info.mix_name  集合名称
    private String imageUrl;  //aweme_list[0].mix_info.cover_url.url_list[0] 集合图片
    private String authName;  //作者
    @DatabaseIgnore
    private List<MediaModule> mediaModuleList;   //集合视频列表

    public String getMixId() {
        return mixId;
    }

    public MixInfo setMixId(String mixId) {
        this.mixId = mixId;
        return this;
    }

    public String getMixName() {
        return mixName;
    }

    public MixInfo setMixName(String mixName) {
        this.mixName = mixName;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public MixInfo setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public List<MediaModule> getMediaModuleList() {
        return mediaModuleList;
    }

    public MixInfo setMediaModuleList(List<MediaModule> mediaModuleList) {
        this.mediaModuleList = mediaModuleList;
        return this;
    }

    public String getAuthName() {
        return authName;
    }

    public MixInfo setAuthName(String authName) {
        this.authName = authName;
        return this;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this, SerializerFeature.WriteNullListAsEmpty);
    }
}
