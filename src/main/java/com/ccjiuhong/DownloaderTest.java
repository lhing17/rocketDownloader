package com.ccjiuhong;

import com.ccjiuhong.mgt.DefaultDownloadManager;
import com.ccjiuhong.mgt.DownloadManager;
import com.ccjiuhong.mission.BitTorrentMission;
import com.ccjiuhong.mission.Mission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 使用代码运行下载项目的测试类
 *
 * @author G.Seinfeld
 * @date 2019/06/29
 */
@Slf4j
public class DownloaderTest {
    public static void main(String[] args) {
        testStartOrResumeBitTorrentMission();
    }


    private static void testStartOrResumeHttpMission() {
        DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();
        String fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
//        String fileUrl = "https://raw.githubusercontent.com/Himself65/LianXue/master/public/1.png";
//        String fileUrl = "https://download.oracle.com/otn/java/jdk/11.0
//        .3+12/37f5e150db5247ab9333b11c1dddcd30/jdk-11.0.3_windows-x64_bin
//        .zip?AuthParam=1561862894_d3c07f3538fb5d9f5c2a3df110fadbe4";
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//        String targetFileName = "jdk-11.0.3_windows-x64_bin.zip";
        int missionId = defaultDownloadManager.addMission(fileUrl, "F:\\rocketDownloader", targetFileName);
        defaultDownloadManager.startOrResumeMission(missionId);

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(() -> log.info("当前下载百分比为：" + defaultDownloadManager.getReadableDownloadedPercent(missionId)), 0, 1, TimeUnit.SECONDS);
    }

    private static void testStartOrResumeBitTorrentMission() {
        DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();
        String fileUrl = "/home/lhing17/b.torrent";
        Mission mission = new BitTorrentMission(1, fileUrl, "/home/lhing17/rocketDownloader", "a");
        defaultDownloadManager.addMission(mission);
        defaultDownloadManager.startOrResumeMission(1);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(() -> log.info("当前下载百分比为：" + defaultDownloadManager.getReadableDownloadedPercent(1)), 0, 1, TimeUnit.SECONDS);
    }
}
