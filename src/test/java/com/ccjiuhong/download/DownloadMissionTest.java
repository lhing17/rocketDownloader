package com.ccjiuhong.download;

import com.ccjiuhong.monitor.MissionMonitor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author G.Seinfeld
 * @date 2019/07/03
 */
public class DownloadMissionTest {
    private String fileUrl = "http://www.qq.com/qq.exe";
    private String targetDirectory = "F:\\rocketDownloader";
    private String targetFileName = "qq.exe";
    private DownloadMission downloadMission = new DownloadMission(0, fileUrl,
            targetDirectory, targetFileName);


    @Test
    public void readDownloadInfo() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<DownloadMission> clazz = DownloadMission.class;
        Method readDownloadInfo = clazz.getDeclaredMethod("readDownloadInfo");
        readDownloadInfo.setAccessible(true);
        readDownloadInfo.invoke(downloadMission);
    }

    /**
     * 测试将下载信息保存为JSON文件
     */
    @Test
    public void saveOrUpdateDownloadInfo() {

        final MissionMonitor missionMonitor = new MissionMonitor(downloadMission);
        List<DownloadRunnable> runnableList = new ArrayList<>();
        DownloadRunnable downloadRunnable = new DownloadRunnable(targetDirectory, targetFileName,
                fileUrl, missionMonitor, 0, 999);
        DownloadRunnable downloadRunnable1 = new DownloadRunnable(targetDirectory, targetFileName,
                fileUrl, missionMonitor, 1000, 1999);
        runnableList.add(downloadRunnable);
        runnableList.add(downloadRunnable1);
        downloadMission.saveOrUpdateDownloadInfo(runnableList);
    }
}