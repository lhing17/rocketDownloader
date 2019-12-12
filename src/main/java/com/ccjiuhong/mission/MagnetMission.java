package com.ccjiuhong.mission;

import bt.BtClientBuilder;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;

/**
 * 磁力链的任务
 *
 * @author G. Seinfeld
 * @since 2019/12/12
 */
public class MagnetMission extends BitTorrentMission {

    private String magnetUrl;

    public MagnetMission(int missionId, String magnetUrl, String targetDirectory, String targetFileName) {
        super(missionId, null, targetDirectory, targetFileName);
        this.magnetUrl = magnetUrl;
    }

    @Override
    public boolean start() {
        // TODO 开启自动保存进度
        // TODO 存储下载信息
        BtRuntime runtime = getBtRuntime();
        BtClientBuilder builder = getBtClientBuilder(runtime);

        // 创建客户端
        BtClient btClient = builder
                .magnet(magnetUrl)
                .stopWhenDownloaded()
                .build();
        startDownload(btClient);

        return true;
    }

    @Override
    public boolean pause() {
        return super.pause();
    }

    @Override
    public boolean resume() {
        return super.resume();
    }

    @Override
    public boolean delete() {
        return super.delete();
    }
}
