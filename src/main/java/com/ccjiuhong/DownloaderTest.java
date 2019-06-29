package com.ccjiuhong;

import com.ccjiuhong.download.DownloadManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

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
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log.info("当前下载百分比为：" + downloadManager.getReadableDownloadedPercent(missionId));
            }
        }, 0, 1000);
    }
}
