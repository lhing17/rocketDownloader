package com.ccjiuhong.download;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 可以转换为文件形式暂存的下载信息，用于恢复下载
 *
 * @author dagerer
 */
@Data
@Slf4j
public class DownloadInfo {
    private int missionId;
    private long fileSize;
    private String fileUrl;
    private String targetDirectory;
    private String targetFileName;
    private List<PositionInfo> positionInfoList;

    /**
     * 根据下载线程的列表构造下载信息
     *
     * @param downloadRunnableList 下载线程列表
     */
    public DownloadInfo(List<DownloadRunnable> downloadRunnableList) {
        if (downloadRunnableList == null || downloadRunnableList.size() == 0) {
            log.warn("下载线程为空");
            return;
        }
        DownloadRunnable downloadRunnable = downloadRunnableList.get(0);
        missionId = downloadRunnable.getMissionId();
        fileUrl = downloadRunnable.getFileUrl();
        targetDirectory = downloadRunnable.getTargetDirectory();
        targetFileName = downloadRunnable.getTargetFileName();
        fileSize = downloadRunnable.getMissionMonitor().getDownloadMission().getFileSize();
        positionInfoList = new ArrayList<>();
        for (DownloadRunnable runnable : downloadRunnableList) {
            PositionInfo positionInfo = new PositionInfo();
            positionInfo.runnableId = runnable.getId();
            positionInfo.startPosition = runnable.getStartPosition();
            positionInfo.currentPosition = runnable.getCurrentPosition();
            positionInfo.endPosition = runnable.getEndPosition();
            positionInfoList.add(positionInfo);
        }
    }

    @Data
    private class PositionInfo {
        private int runnableId;
        private long startPosition;
        private long currentPosition;
        private long endPosition;
    }

}
