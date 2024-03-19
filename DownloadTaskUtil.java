package com.jinmgr.utils;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.jinmgr.dto.CommonResult;
import com.jinmgr.entity.DownTask;
import com.jinmgr.entity.MixInfo;
import com.jinmgr.m3u8.download.M3u8DownloadFactory;
import com.jinmgr.m3u8.utils.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.jinmgr.m3u8.utils.Constant.FILESEPARATOR;

/**
 * 下载任务管理工具
 * <p>
 * 参考链接
 * https://trac.ffmpeg.org/wiki/Concatenate
 * https://www.cnblogs.com/meow-world/articles/17659687.html
 * https://blog.csdn.net/weixin_38579366/article/details/115794188 把mp4转为mts再合并虽然可行,但是耗时长,输出文件太大,已弃用
 * https://blog.csdn.net/tong5956/article/details/108277191  解决合并视频部分视频段没有声音问题
 *
 * @author LiuChenghui 2024/03/13 10:27:00
 * @version 1.0
 */
public class DownloadTaskUtil {
    private Logger logger = LoggerFactory.getLogger(DownloadTaskUtil.class);
    private static DownloadTaskUtil mInstances;
    //下载视频线程池
    private ExecutorService exec = Executors.newFixedThreadPool(10);
    //单任务线程
    private ExecutorService singleTaskThread = Executors.newSingleThreadExecutor();
    private Future taskFuture;
    //单任务计数器
    private CountDownLatch countDownLatch;

    //任务集合
    private ConcurrentHashMap<String, DownTask> taskMap = new ConcurrentHashMap<>();
    //任务队列
    private Queue<DownTask> taskQueue = new LinkedBlockingQueue<>();

    private DownloadTaskUtil() {
    }

    public static DownloadTaskUtil getInstance() {
        if (mInstances == null) {
            synchronized (DownloadTaskUtil.class) {
                if (mInstances == null) {
                    mInstances = new DownloadTaskUtil();
                }
            }
        }
        return mInstances;
    }

    /**
     * 添加任务
     *
     * @param mixId
     * @param merge
     * @param list
     * @param baseDir
     */
    public CommonResult<List<DownTask>> addTask(DownTask task) {
        CommonResult<List<DownTask>> result = new CommonResult(false, "添加任务失败");
        try {
            if (task == null || StringUtils.isAnyEmpty(task.getTaskId(), task.getTaskName(), task.getDownloadDir()) || task.getMediaModules() == null || task.getMediaModules().size() == 0) {
                result.setErrorMsg("参数错误");
                return result;
            }
            //初始化状态
            task.setStatus(0);
            taskMap.put(task.getTaskId(), task);
            taskQueue.offer(task);
            result.success(getTaskList(), "添加下载任务成功");
        } catch (Exception e) {
            result.exception("添加任务异常:" + e.getMessage());
            logger.error("添加任务异常:" + e.getMessage(), e);
        }
        startTask();
        return result;
    }

    /**
     * 执行任务下载视频
     *
     * @param task
     */
    private void startTask() {
        try {
            if (taskFuture != null && !taskFuture.isDone()) return;
            taskFuture = singleTaskThread.submit(() -> {
                while (!taskQueue.isEmpty()) {
                    DownTask task = taskQueue.poll();
                    if (task == null) return;
                    doTask(task);
                }
            });
        } catch (Exception e) {
            logger.error("下载集合异常:" + e.getMessage(), e);
        }
    }

    private void doTask(DownTask task) {
        try {
            if (task == null) return;
            countDownLatch = new CountDownLatch(task.getMediaModules().size());
            //开始时间
            task.setStarttime(System.currentTimeMillis());
            taskMap.get(task.getTaskId()).setStatus(1);
            task.getMediaModules().stream().forEach(module -> {
                exec.execute(() -> {
                    try {
                        File outputFile = null;
                        String outPutFilePath = task.getDownloadDir() + FILESEPARATOR + task.getTaskName().trim() + FILESEPARATOR + module.getTitle().trim() + FILESEPARATOR + module.getTitle().trim() + ".mp4";
                        if (!FileUtil.exist(outPutFilePath)) {
                            int fcount = 0;
                            while (fcount < 3) {
                                //下载视频
                                long st = System.currentTimeMillis();
                                if (task.getTaskType() == 0) {
                                    logger.info("开始下载资源:" + module.getTitle() + ",url:" + module.getM3u8Url());
                                    //m3u8
                                    outputFile = downloadM3u8(module.getTitle().trim(), module.getM3u8Url().trim(), task.getDownloadDir() + FILESEPARATOR + task.getTaskName());
                                } else if (task.getTaskType() == 1) {
                                    logger.info("开始下载资源:" + module.getTitle() + ",url:" + module.getMediaUrl());
                                    outputFile = HttpUtils.saveToFile(module.getMediaUrl(), task.getDownloadDir() + FILESEPARATOR + task.getTaskName(), module.getTitle(), ".mp4");
                                }
                                if (outputFile != null && outputFile.exists()) {
                                    logger.info("资源:" + module.getTitle() + ",下载成功,耗时:" + (System.currentTimeMillis() - st) + "ms");
                                    if (StringUtils.isNotEmpty(module.getImageUrl().trim())) {
                                        //下载图片
                                        HttpUtils.saveToFile(module.getImageUrl(), task.getDownloadDir() + FILESEPARATOR + task.getTaskName(), module.getTitle(), ".jpg");
                                    }
                                    break;
                                } else {
                                    fcount++;
                                    logger.info("下载资源:" + module.getTitle() + " 失败,第" + fcount + "次重新下载,url:" + module.getMediaUrl());
                                }
                            }
                        } else {
                            outputFile = new File(outPutFilePath);
                            logger.info("资源:" + module.getTitle() + ",已存在,忽略下载");
                        }
                        if (outputFile != null && outputFile.exists()) {
                            module.setMediaLocalPath(outputFile.getAbsolutePath());
                        } else {
                            task.setDesc("下载资源失败:" + module.getTitle() + ",url:" + module.getM3u8Url());
                        }
                    } catch (Exception e) {
                        task.setDesc("任务执行异常:" + e.getMessage());
                        logger.error("任务执行异常:" + e.getMessage(), e);
                    } finally {
                        if (countDownLatch != null) {
                            countDownLatch.countDown();
                        }
                    }
                });
            });
            boolean flag = countDownLatch.await(20, TimeUnit.MINUTES);
            if (flag) {
                logger.info("任务:" + task.getTaskName() + ",下载完成,耗时:" + (System.currentTimeMillis() - task.getStarttime()) / 1000 + "秒");
                if (task.isMerge()) {
                    //合并视频
                    File tgFile = mergeMedia(task);
                    if (tgFile != null) {
                        taskMap.get(task.getTaskId()).setPreviewMedia(tgFile.getAbsolutePath());
                    }
                } else {
                    taskMap.get(task.getTaskId()).setPreviewMedia(task.getMediaModules().get(0).getMediaLocalPath());
                }
                task.setDesc("任务执行成功");
            } else {
                logger.info("任务:" + task.getTaskName() + ",资源下载超时");
                task.setDesc("任务执行超时");
            }
        } catch (Exception e) {
            task.setDesc("任务执行异常:" + e.getMessage());
            logger.error("任务:" + task.getTaskName() + ",执行异常:" + e.getMessage(), e);
        } finally {
            taskMap.get(task.getTaskId()).setStatus(2);
            taskMap.get(task.getTaskId()).setEndtime(System.currentTimeMillis());
            logger.info("任务:" + task.getTaskName() + ",执行完成,总耗时:" + (task.getEndtime() - task.getStarttime()) / 1000 + "秒");
        }
    }

    /**
     * 下载m3u8
     *
     * @param title
     * @param url
     * @param baseDir
     * @return
     */
    private File downloadM3u8(String title, String url, String baseDir) {
        try {
            M3u8DownloadFactory.M3u8Download m3u8Download = M3u8DownloadFactory.getInstance();
            //m3u8文件地址
            m3u8Download.setDOWNLOADURL(url);
            //设置生成目录
            m3u8Download.setDir(baseDir + Constant.FILESEPARATOR + title);
            //设置视频名称
            m3u8Download.setFileName(title);
            //设置线程数
            m3u8Download.setThreadCount(100);
            //设置重试次数
            m3u8Download.setRetryCount(10);
            //设置连接超时时间（单位：毫秒）
            m3u8Download.setTimeoutMillisecond(10000L);
            //添加额外请求头
      /*  Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("Content-Type", "text/html;charset=utf-8");
        m3u8Download.addRequestHeaderMap(headersMap);*/
            //如果需要的话设置http代理
            //m3u8Download.setProxy("172.50.60.3",8090);
            //添加监听器
            //开始下载
            return m3u8Download.start();
        } catch (Exception e) {
            logger.error("下载文件异常:{}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 合并视频
     *
     * @param task
     */
    private File mergeMedia(DownTask task) {
        File outputFile = null;
        //所有视频已下载完成
        long st = System.currentTimeMillis();
        try {
            logger.info("任务:" + task.getTaskName() + ",开始合并视频");
            outputFile = new File(task.getDownloadDir() + FILESEPARATOR + task.getTaskName() + FILESEPARATOR + task.getTaskName() + ".mp4");
            System.gc();
            if (!outputFile.getParentFile().exists()) outputFile.getParentFile().mkdirs();
            if (outputFile.exists()) outputFile.delete();
            //获取mp4本地文件地址并按照创建时间排序
            List<String> fileListPath = task.getMediaModules().stream().sorted(Comparator.comparingLong(t -> Long.parseLong(t.getDateTime()))).map(f -> f.getMediaLocalPath()).collect(Collectors.toList());
            //合并
            boolean f = FFmpegUtils.getInstance().mergeMp4(fileListPath, outputFile.getAbsolutePath());
            return f ? outputFile : null;
        } catch (Exception e) {
            task.setStatus(2);
            task.setEndtime(System.currentTimeMillis());
            task.setDesc("合并视频异常:" + e.getMessage());
            logger.error("任务:" + task.getTaskName() + ",合并视频异常:" + e.getMessage(), e);
        } finally {
            logger.info("任务:" + task.getTaskName() + ",合并视频完成,耗时:" + (System.currentTimeMillis() - st) / 1000 + "秒");
        }
        return outputFile;
    }

    /**
     * 任务创建时间倒序列表
     *
     * @return
     */
    public List<DownTask> getTaskList() {
        List<DownTask> list = taskMap.values().stream().sorted(Comparator.comparing(DownTask::getCreatetime).reversed()).collect(Collectors.toList());
        return list;
    }

    /**
     * 删除任务
     *
     * @param taskId
     * @return
     */
    public CommonResult deleteTask(String taskId) {
        CommonResult result = new CommonResult(false, "参数错误");
        try {
            if (StringUtils.isEmpty(taskId)) return result;
            if (!taskMap.containsKey(taskId)) {
                result.setErrorMsg("无效任务ID");
                return result;
            }
            if (taskMap.get(taskId).getStatus() == 1) {
                result.setErrorMsg("任务尚未终止,无法删除");
                return result;
            }
            logger.info("删除任务:" + taskMap.get(taskId).getTaskName());
            taskMap.remove(taskId);
            result.success("删除任务成功");
        } catch (Exception e) {
            result.exception("删除任务异常:" + e.getMessage());
            logger.error("删除任务异常:" + e.getMessage(), e);
        }
        return result;
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        //ffmpeg合并文件
        FFmpegUtils.getInstance().setFFmpegInstallDir("D:/ffmpeg/bin");   //ffmpeg安装路径
        String mixId = "7279628319534876706";
//        String mixId = "7264942174918314024";
//        String mixId = "7338512743114213395";
        String baseDir = "D:/douyin";
        MixInfo mixInfo = DouyinUtil.getUserMixList(mixId);
        System.out.println("集合信息:" + JSONObject.toJSONString(mixInfo));
        DownTask task = new DownTask(mixInfo.getMixId(), mixInfo.getMixName() + "全集合下载", 1, mixInfo.getImageUrl(), baseDir + FILESEPARATOR + mixInfo.getMixName(), true, mixInfo.getMediaModuleList());
        DownloadTaskUtil.getInstance().addTask(task);
        new CountDownLatch(1).await(3, TimeUnit.MINUTES);

    }
}
