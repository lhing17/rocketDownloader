package com.ccjiuhong.download;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author G. Seinfeld
 * @date 2019/07/09
 */
public class DownloadManagerTest {

    DownloadManager manager;

    @Before
    public void setUp() throws Exception {
        manager = DownloadManager.getInstance();
    }

    /**
     * 测试两次通过getInstance方法获得的DownloadManager实例是同一个实例，即测试单例模式是否成立。
     */
    @Test
    public void getInstance() {
        DownloadManager manager0 = DownloadManager.getInstance();
        DownloadManager manager1 = DownloadManager.getInstance();
        Assertions.assertThat(manager0).isSameAs(manager1);
    }

    /**
     * 测试添加重复任务的场景
     */
    @Test
    public void addDuplicatedMission() {
        String fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
        String targetDirectory = "F:\\rocketDownloader";
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        int missionId = manager.addMission(fileUrl, targetDirectory, targetFileName);
        Assertions.assertThat(missionId).isEqualTo(0);

        fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
        missionId = manager.addMission(fileUrl, targetDirectory, targetFileName);
        Assertions.assertThat(missionId).isEqualTo(-1);
    }

    /**
     * 测试添加不同的任务
     */
    @Test
    public void addDifferentMissions() {
        String fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
        String targetDirectory = "F:\\rocketDownloader";
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        int missionId = manager.addMission(fileUrl, targetDirectory, targetFileName);
        Assertions.assertThat(missionId).isEqualTo(0);

        fileUrl = "https://raw.githubusercontent.com/Himself65/LianXue/master/public/1.png";
        targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        missionId = manager.addMission(fileUrl, targetDirectory, targetFileName);
        Assertions.assertThat(missionId).isEqualTo(1);
    }

    /**
     * 测试启动一个新的下载任务
     */
    @Test
    public void startMission() {
        String fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
        String targetDirectory = "F:\\rocketDownloader";
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        int missionId = manager.addMission(fileUrl, targetDirectory, targetFileName);
        boolean success = manager.startOrResumeMission(missionId);
        Assertions.assertThat(success).isEqualTo(true);
    }

    @Test
    public void resumeMission() {

    }

    @Test
    public void startOrResumeAll() {
    }

    @Test
    public void pauseMission() {
    }

    @Test
    public void pauseAll() {
    }

    @Test
    public void cancelMission() {
    }

    @Test
    public void cancelAll() {
    }

    @Test
    public void getSpeed() {
    }

    @Test
    public void getTotalSpeed() {
    }

    @Test
    public void getDownloadedSize() {
    }

    @Test
    public void getReadableDownloadedPercent() {
    }
}