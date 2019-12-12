package com.ccjiuhong.mission;

import com.ccjiuhong.download.DownloadThreadPool;

/**
 * @author G. Seinfeld
 * @since 2019/12/12
 */
public class MissionFactory {
    /**
     * 下载任务的ID，从0开始，每次加1
     */
    private static int serialMissionId = 0;

    public Mission createMissionIntelligently(String fileUrl, String targetDirectory, String targetFileName, DownloadThreadPool downloadThreadPool, boolean isBt) {
        if (isBt) {
            return new BitTorrentMission(serialMissionId++, fileUrl, targetDirectory, targetFileName);
        }
        if (fileUrl.startsWith("http") || fileUrl.startsWith("https") || fileUrl.startsWith("ftp")) {
            return new HttpMission(serialMissionId++, fileUrl, targetDirectory,
                    targetFileName, downloadThreadPool);
        }
        if (fileUrl.startsWith("magnet")) {
            return new MagnetMission(serialMissionId++, fileUrl, targetDirectory, targetFileName);
        }

        throw new RuntimeException("无法识别的文件地址");
    }
}
