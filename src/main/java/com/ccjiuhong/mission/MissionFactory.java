package com.ccjiuhong.mission;

import com.ccjiuhong.download.DownloadThreadPool;
import com.ccjiuhong.util.DownloadUtil;

/**
 * 生产各类任务的工厂
 *
 * @author G. Seinfeld
 * @since 2019/12/12
 */
public class MissionFactory {
    /**
     * 下载任务的ID，从0开始，每次加1
     */
    private static int serialMissionId = 0;

    public Mission createMissionIntelligently(String fileUrl, String targetDirectory, String targetFileName, DownloadThreadPool downloadThreadPool, boolean isBt) {
        // 处理BT种子下载（不包括磁力链）
        if (isBt) {
            return new BitTorrentMission(serialMissionId++, fileUrl, targetDirectory, targetFileName);
        }

        fileUrl = fileUrl.trim();
        // 处理thunder等下载协议
        fileUrl = DownloadUtil.decodeIfNecessary(fileUrl);


        if (fileUrl.startsWith("http") || fileUrl.startsWith("https")) {
            return new HttpMission(serialMissionId++, fileUrl, targetDirectory,
                    targetFileName, downloadThreadPool);
        }
        if (fileUrl.startsWith("ftp")) {
            return new FtpMission(serialMissionId++, fileUrl, targetDirectory,
                    targetFileName, downloadThreadPool);
        }
        if (fileUrl.startsWith("magnet")) {
            return new MagnetMission(serialMissionId++, fileUrl, targetDirectory, targetFileName);
        }

        throw new RuntimeException("无法识别的文件地址");
    }


}
