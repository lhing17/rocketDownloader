package com.ccjiuhong.monitor;

import com.ccjiuhong.mission.HttpMission;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author G. Seinfeld
 * @date 2019/07/09
 */
@Getter
@Slf4j
public class AutoSaver implements Runnable {

    private HttpMission httpMission;

    public AutoSaver(HttpMission httpMission) {
        this.httpMission = httpMission;
    }

    @Override
    public void run() {
        log.info("更新进度文件");
        httpMission.saveOrUpdateDownloadInfo(httpMission.getRunnableList());
    }
}
