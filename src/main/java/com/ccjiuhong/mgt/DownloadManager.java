package com.ccjiuhong.mgt;

import com.ccjiuhong.mission.Mission;

/**
 * @author G. Seinfeld
 * @since 2019/12/10
 */
public interface DownloadManager {
    int addMission(String fileUrl, String targetDirectory, String targetFileName);

    int addMission(Mission mission);

    boolean startOrResumeMission(int missionId);

    void startOrResumeAll();

    boolean pauseMission(int missionId);

    void pauseAll();

    boolean cancelMission(int missionId);

    void cancelAll();

    long getSpeed(int missionId);

    long getTotalSpeed();

    long getDownloadedSize(int missionId);

    String getReadableDownloadedPercent(int missionId);
}
