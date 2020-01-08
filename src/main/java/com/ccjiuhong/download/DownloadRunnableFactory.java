package com.ccjiuhong.download;

import com.ccjiuhong.mission.FtpMission;
import com.ccjiuhong.mission.HttpMission;
import com.ccjiuhong.mission.Mission;

/**
 * 处理下载线程的工厂类
 *
 * @author G. Seinfeld
 * @since 2020/01/08
 */
public class DownloadRunnableFactory {

    public static DownloadRunnable createDownloadRunnable(String targetDirectory, String targetFileName, String fileUrl,
                                                          Mission mission, long startPosition, long endPosition) {

        if (mission instanceof HttpMission)
            return new HttpDownloadRunnable(targetDirectory, targetFileName, fileUrl, mission, startPosition,
                    endPosition);
        else if (mission instanceof FtpMission)
            return new FtpDownloadRunnable(targetDirectory, targetFileName, fileUrl, mission, startPosition,
                    endPosition);
        return new HttpDownloadRunnable(targetDirectory, targetFileName, fileUrl, mission, startPosition,
                endPosition);
    }

    public static DownloadRunnable createDownloadRunnable(String targetDirectory, String targetFileName, String fileUrl,
                                                          Mission mission, long startPosition, long currentPosition, long endPosition) {

        if (mission instanceof HttpMission)
            return new HttpDownloadRunnable(targetDirectory, targetFileName, fileUrl, mission, startPosition, currentPosition,
                    endPosition);
        else if (mission instanceof FtpMission)
            return new FtpDownloadRunnable(targetDirectory, targetFileName, fileUrl, mission, startPosition, currentPosition,
                    endPosition);
        return new HttpDownloadRunnable(targetDirectory, targetFileName, fileUrl, mission, startPosition, currentPosition,
                endPosition);
    }

}
