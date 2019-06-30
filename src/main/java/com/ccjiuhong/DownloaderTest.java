package com.ccjiuhong;

import com.ccjiuhong.download.DownloadManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Timer;
import java.util.TimerTask;
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
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/"));
        int missionId = downloadManager.addMission(fileUrl, "F:\\rocketDownloader", targetFileName);
        downloadManager.startMission(missionId);

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log.info("当前下载百分比为：" + downloadManager.getReadableDownloadedPercent(missionId));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
