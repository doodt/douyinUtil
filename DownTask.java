package com.jinmgr.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;

import java.io.Serializable;
import java.util.List;

/**
 * 下载任务
 *
 * @author LiuChenghui 2024/03/14 10:22:00
 * @version 1.0
 */
public class DownTask implements Serializable {
    private String taskId;      //任务ID
    private String taskName;    //任务名称(文件夹名称)
    private int taskType;       //0:m3u8下载任务,1:MP4列表下载任务
    private String taskImage;   //任务预览图
    private String previewMedia; //预览视频地址
    private List<MediaModule> mediaModules;   //资源列表
    private String downloadDir;     //资源下载目录
    private boolean merge;          //下载结果是否合并为一个文件
    private int status;             //0未开始,1正在执行,2已完成
    private String payUrl;    //播放地址,status=2时有效
    private Long createtime;  //任务创建时间
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long starttime;   //任务开始时间
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long endtime;     //任务结束时间
    //执行结果
    private String desc;

    public DownTask() {
    }

    public DownTask(String taskId, String taskName, int taskType, String taskImage, String downloadDir, boolean merge, List<MediaModule> modules) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.downloadDir = downloadDir;
        this.merge = merge;
        this.mediaModules = modules;
        this.taskType = taskType;
        this.taskImage = taskImage;
        this.createtime = System.currentTimeMillis();
    }

    public String getTaskId() {
        return taskId;
    }

    public DownTask setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getTaskName() {
        return taskName;
    }

    public DownTask setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    public int getTaskType() {
        return taskType;
    }

    public DownTask setTaskType(int taskType) {
        this.taskType = taskType;
        return this;
    }

    public List<MediaModule> getMediaModules() {
        return mediaModules;
    }

    public DownTask setMediaModules(List<MediaModule> mediaModules) {
        this.mediaModules = mediaModules;
        return this;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public DownTask setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
        return this;
    }

    public boolean isMerge() {
        return merge;
    }

    public DownTask setMerge(boolean merge) {
        this.merge = merge;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public DownTask setStatus(int status) {
        this.status = status;
        return this;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public DownTask setCreatetime(Long createtime) {
        this.createtime = createtime;
        return this;
    }

    public Long getStarttime() {
        return starttime;
    }

    public DownTask setStarttime(Long starttime) {
        this.starttime = starttime;
        return this;
    }

    public Long getEndtime() {
        return endtime;
    }

    public DownTask setEndtime(Long endtime) {
        this.endtime = endtime;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public DownTask setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getTaskImage() {
        return taskImage;
    }

    public DownTask setTaskImage(String taskImage) {
        this.taskImage = taskImage;
        return this;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public DownTask setPayUrl(String payUrl) {
        this.payUrl = payUrl;
        return this;
    }

    public String getPreviewMedia() {
        return previewMedia;
    }

    public void setPreviewMedia(String previewMedia) {
        this.previewMedia = previewMedia;
    }
}
