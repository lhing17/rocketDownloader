package com.ccjiuhong.mission;

import bt.BtClientBuilder;
import bt.metainfo.IMetadataService;
import bt.metainfo.Torrent;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * BT下载任务
 *
 * @author G. Seinfeld
 * @since 2019/12/11
 */
@Slf4j
public class BitTorrentMission extends PeerToPeerMission {

    private String torrentFilePath;

    public BitTorrentMission(int missionId, String torrentFilePath, String targetDirectory, String targetFileName) {
        super(missionId, targetDirectory, targetFileName);
        this.torrentFilePath = torrentFilePath;
    }

    @Override
    public boolean start() {
        // TODO 开启自动保存进度
        // TODO 存储下载信息
        BtRuntime runtime = getBtRuntime();
        BtClientBuilder builder = getBtClientBuilder(runtime);

        // 创建客户端
        Supplier<Torrent> supplier = null;
        try {
            final FileInputStream in = new FileInputStream(torrentFilePath);
            supplier = () -> runtime.service(IMetadataService.class).fromInputStream(in);
            getMetaData().setDotTorrentFilePath(torrentFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BtClient btClient = builder
                .torrent(Objects.requireNonNull(supplier))
                .afterTorrentFetched(torrent -> {
                    getMetaData().setFileSize(torrent.getSize());
                    getMetaData().setUniqueIdentifier(torrent.getTorrentId().toString());
                })
                .stopWhenDownloaded()
                .build();
        startDownload(btClient);

        return true;
    }


    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean resume() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

}
