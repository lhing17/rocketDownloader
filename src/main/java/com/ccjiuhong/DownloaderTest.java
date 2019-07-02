package com.ccjiuhong;

import com.ccjiuhong.download.DownloadManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author G.Seinfeld
 * @date 2019/06/29
 */
@Slf4j
public class DownloaderTest {
    public static void main(String[] args) {
        DownloadManager downloadManager = DownloadManager.getInstance();
        String fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
//        String fileUrl = "https://download.oracle.com/otn/java/jdk/11.0.3+12/37f5e150db5247ab9333b11c1dddcd30/jdk-11.0.3_windows-x64_bin.zip?AuthParam=1561862894_d3c07f3538fb5d9f5c2a3df110fadbe4";
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//        String targetFileName = "jdk-11.0.3_windows-x64_bin.zip";
        int missionId = downloadManager.addMission(fileUrl, "F:\\rocketDownloader", targetFileName);
        downloadManager.startMission(missionId);

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(() -> log.info("当前下载百分比为：" + downloadManager.getReadableDownloadedPercent(missionId)), 0, 1, TimeUnit.SECONDS);
    }
}
