package com.ccjiuhong.monitor;

import com.ccjiuhong.mission.ServerToClientMission;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author G. Seinfeld
 * @date 2019/07/09
 */
@Getter
@Slf4j
public class AutoSaver implements Runnable {

    private ServerToClientMission serverToClientMission;

    public AutoSaver(ServerToClientMission serverToClientMission) {
        this.serverToClientMission = serverToClientMission;
    }

    @Override
    public void run() {
        log.info("更新进度文件");
        serverToClientMission.saveOrUpdateDownloadInfo(serverToClientMission.getRunnableList());
    }
}
