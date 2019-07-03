package com.ccjiuhong.download;

import com.ccjiuhong.monitor.MissionMonitor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author G.Seinfeld
 * @date 2019/07/03
 */
public class DownloadMissionTest {

    /**
     * 测试将下载信息保存为JSON文件
     */
    @Test
    public void saveOrUpdateDownloadInfo() {
        String fileUrl = "http://www.qq.com/qq.exe";
        String targetDirectory = "F:\\rocketDownloader";
        String targetFileName = "qq.exe";
        DownloadMission downloadMission = new DownloadMission(0, fileUrl,
                targetDirectory, targetFileName);
        final MissionMonitor missionMonitor = new MissionMonitor(downloadMission);
        List<DownloadRunnable> runnableList = new ArrayList<>();
        DownloadRunnable downloadRunnable = new DownloadRunnable(targetDirectory, targetFileName,
                fileUrl, missionMonitor, 10, 1000);
        runnableList.add(downloadRunnable);
        downloadMission.saveOrUpdateDownloadInfo(runnableList);
    }
}