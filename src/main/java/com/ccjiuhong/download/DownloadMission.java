package com.ccjiuhong.download;

import java.util.ArrayList;
import java.util.List;

/**
 * 指一个下载任务的对象，一个下载任务可以由多个线程组成
 *
 * @author G. Seinfeld
 * @date 2019/06/28
 */
public class DownloadMission {
    /**
     * 文件大小
     */
    private long fileSize;
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
     * 下载线程列表
     */
    private List<DownloadRunnable> runnableList = new ArrayList<>();
    /**
     * 下载状态
     */
    private EnumDownloadStatus downloadStatus = EnumDownloadStatus.READY;


}
