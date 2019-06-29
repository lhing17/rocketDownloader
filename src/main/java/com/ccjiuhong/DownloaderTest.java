package com.ccjiuhong;

import com.ccjiuhong.download.DownloadManager;

/**
 * @author G.Seinfeld
 * @date 2019/06/29
 */
public class DownloaderTest {
    public static void main(String[] args) {
        DownloadManager downloadManager = DownloadManager.getInstance();
        String fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/"));
        int missionId = downloadManager.addMission(fileUrl, "F:\\rocketDownloader", targetFileName);
        downloadManager.startMission(missionId);
    }
}
