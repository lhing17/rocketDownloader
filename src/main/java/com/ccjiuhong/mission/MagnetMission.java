package com.ccjiuhong.mission;

import bt.BtClientBuilder;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import com.ccjiuhong.util.DotTorrentFileGenerator;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 磁力链的任务
 *
 * @author G. Seinfeld
 * @since 2019/12/12
 */
@Slf4j
public class MagnetMission extends PeerToPeerMission {

    private String magnetUrl;

    public MagnetMission(int missionId, String magnetUrl, String targetDirectory, String targetFileName) {
        super(missionId, targetDirectory, targetFileName);
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
                .afterTorrentFetched(torrent -> {
                    getMetaData().setFileSize(torrent.getSize());
                    String targetDirectory = getMetaData().getTargetDirectory();
                    String fileName = Paths.get(targetDirectory, torrent.getTorrentId() + ".torrent").toString();
                    FileOutputStream outputStream;
                    try {
                        outputStream = new FileOutputStream(fileName);
                        DotTorrentFileGenerator.generate(torrent, outputStream);
                    } catch (IOException e) {
                        log.warn("无法写入torrent文件", e);
                    }
                })
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
