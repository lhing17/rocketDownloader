package com.ccjiuhong.monitor;

import com.ccjiuhong.download.DownloadMission;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author G. Seinfeld
 * @date 2019/07/09
 */
@Getter
@Slf4j
public class AutoSaver implements Runnable {

    private DownloadMission downloadMission;

    public AutoSaver(DownloadMission downloadMission) {
        this.downloadMission = downloadMission;
    }

    @Override
    public void run() {
        log.info("更新进度文件");
        downloadMission.saveOrUpdateDownloadInfo(downloadMission.getRunnableList());
    }
}
