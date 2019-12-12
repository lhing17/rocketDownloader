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

    public Mission createMissionAutomatically(String fileUrl, String targetDirectory, String targetFileName, DownloadThreadPool downloadThreadPool) {
        if (fileUrl.startsWith("http") || fileUrl.startsWith("https") || fileUrl.startsWith("ftp")) {
            return new HttpMission(serialMissionId++, fileUrl, targetDirectory,
                    targetFileName, downloadThreadPool);
        }

        throw new RuntimeException("无法识别的文件地址");
    }
}
