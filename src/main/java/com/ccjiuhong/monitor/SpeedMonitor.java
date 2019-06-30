package com.ccjiuhong.monitor;

import com.ccjiuhong.download.DownloadMission;
import com.ccjiuhong.download.DownloadRunnable;
import com.ccjiuhong.download.EnumDownloadStatus;
import com.ccjiuhong.util.DownloadUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 速度监测器，监测某一个线程的下载速度
 *
 * @author G.Seinfeld
 * @date 2019/06/29
 */
@Getter
@Slf4j
public class SpeedMonitor implements Runnable {

    private DownloadMission downloadMission;
    private long lastSecondSize;
    private long currentSize;
    private long speed;

    public SpeedMonitor(DownloadMission downloadMission) {
        this.downloadMission = downloadMission;
    }

    @Override
    public void run() {
        lastSecondSize = currentSize;
        currentSize = downloadMission.getMissionMonitor().getDownloadedSize().get();
        speed = currentSize - lastSecondSize;
        if (!EnumDownloadStatus.FINISHED.equals(downloadMission.getDownloadStatus())){
            log.info("当前下载任务为{}，下载速度为{}", downloadMission.getMissionId(), DownloadUtil.getReadableSpeed(speed));
        }
    }
}
