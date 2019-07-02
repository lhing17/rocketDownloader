package com.ccjiuhong.download;

import com.ccjiuhong.monitor.MissionMonitor;
import com.ccjiuhong.monitor.SpeedMonitor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 指一个下载任务的对象，一个下载任务可以由多个线程组成
 *
 * @author G. Seinfeld
 * @date 2019/06/28
 */
@Data
@Slf4j
public class DownloadMission {
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * 任务ID
     */
    private int missionId;
    /**
     * 目标文件URL
     */
    private String fileUrl;
    /**
     * 目标文件夹
     */
    private String targetDirectory;
    /**
     * 目标文件名
     */
    private String targetFileName;
    /**
     * 任务监测器
     */
    private MissionMonitor missionMonitor;
    /**
     * 速度监测器
     */
    private SpeedMonitor speedMonitor;
    /**
     * 下载线程列表
     */
    private List<DownloadRunnable> runnableList = new ArrayList<>();
    /**
     * 下载状态
     */
    private EnumDownloadStatus downloadStatus = EnumDownloadStatus.READY;
    /**
     * 用于执行速度监测的定时线程池
     */
    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
    /**
     * 一个下载任务分配的最大线程数
     */
    private static final int MAX_THREAD_PER_MISSION = 5;

    public DownloadMission(int missionId, String fileUrl, String targetDirectory, String targetFileName) {
        this.missionId = missionId;
        this.fileUrl = fileUrl;
        this.targetDirectory = targetDirectory;
        this.targetFileName = targetFileName;
    }

    /**
     * 开启当前下载任务
     *
     * @param downloadThreadPool 下载的线程池
     * @return 开启成功返回true，否则返回false
     */
    public boolean start(DownloadThreadPool downloadThreadPool) {
        // 线程池没有可用线程
//        if (downloadThreadPool.getPoolSize() == 0) {
//            return false;
//        }
        // 线程池已停止
        if (downloadThreadPool.isTerminated()) {
            return false;
        }
        // 获取文件大小，获取失败则返回false
        if ((fileSize = getFileSizeFromUrl(fileUrl)) == 0) {
            return false;
        }

        missionMonitor = new MissionMonitor(this);
        speedMonitor = new SpeedMonitor(this);
        // 开启速度监测
        executorService.scheduleAtFixedRate(speedMonitor, 0, 1, TimeUnit.SECONDS);

        // 开启线程任务执行
        for (int i = 0; i < MAX_THREAD_PER_MISSION; i++) {
            long start = i * (fileSize / MAX_THREAD_PER_MISSION);
            long end = (i == MAX_THREAD_PER_MISSION - 1)
                    ? fileSize - 1
                    : (i + 1) * (fileSize / MAX_THREAD_PER_MISSION) - 1;
            DownloadRunnable downloadRunnable =
                    new DownloadRunnable(targetDirectory, targetFileName, fileUrl, missionMonitor, start, end);

            log.info("新增下载线程，任务ID为{}，文件大小为{}，开始位置为{}，结束位置为{}", missionId, fileSize, start, end);
            downloadThreadPool.submit(downloadRunnable);
            runnableList.add(downloadRunnable);
        }

        // 修改任务状态
        downloadStatus = EnumDownloadStatus.DOWNLOADING;
        return true;

    }

    /**
     * 暂停当前任务
     *
     * @param downloadThreadPool 下载的线程池
     * @return 暂停成功返回true，否则返回false
     */
    public boolean pause(DownloadThreadPool downloadThreadPool) {
        try {
            downloadThreadPool.pause(missionId);
            // 修改任务状态为暂停
            downloadStatus = EnumDownloadStatus.PAUSED;
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从服务器获取文件大小
     *
     * @param fileUrl 文件地址
     * @return 文件大小
     */
    private long getFileSizeFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            return urlConnection.getContentLengthLong();
        } catch (IOException e) {
            log.error("从服务器获取文件大小失败", e);
            return 0;
        }
    }
}
