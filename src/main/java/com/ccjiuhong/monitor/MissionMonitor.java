package com.ccjiuhong.monitor;

import com.ccjiuhong.download.DownloadMission;
import com.ccjiuhong.download.DownloadRunnable;
import com.ccjiuhong.download.EnumDownloadStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author G. Seinfeld
 * @date 2019/06/27
 */
@Getter
@Slf4j
public class MissionMonitor {
    /**
     * 已经下载的字节数，这里考虑到线程安全的问题，使用JUC包中的AtomicLong，确保计算的原子性
     */
    private AtomicLong downloadedSize = new AtomicLong();

    private DownloadMission downloadMission;

    public MissionMonitor(DownloadMission downloadMission) {
        this.downloadMission = downloadMission;
    }

    /**
     * 累加已下载字节数
     *
     * @param size 下载字节数
     */
    public void down(long size) {
        downloadedSize.addAndGet(size);
        if (downloadMission.getFileSize() == downloadedSize.get()) {
            downloadMission.setDownloadStatus(EnumDownloadStatus.FINISHED);
        }
    }

}
