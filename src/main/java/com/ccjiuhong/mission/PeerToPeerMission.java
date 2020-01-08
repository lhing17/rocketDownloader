package com.ccjiuhong.mission;

import bt.Bt;
import bt.BtClientBuilder;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import bt.torrent.TorrentPersist;
import com.ccjiuhong.download.EnumDownloadStatus;
import com.google.inject.Module;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * 点对点类型下载任务
 *
 * @author G. Seinfeld
 * @since 2020/01/08
 */
@Slf4j
public abstract class PeerToPeerMission extends GenericMission {

    protected CompletableFuture<?> completableFuture;

    public PeerToPeerMission(int missionId, String targetDirectory, String targetFileName) {
        super(missionId, targetDirectory, targetFileName);
    }


    protected void startDownload(BtClient btClient) {
        // 开始下载
        long[] downloaded = {0L, 0L};
        getMetaData().setStatus(EnumDownloadStatus.DOWNLOADING);
        completableFuture = btClient.startAsync(
                torrentSessionState -> {
                    // 上一秒下载字节数
                    downloaded[0] = downloaded[1];
                    // 当前下载字节数
                    downloaded[1] = torrentSessionState.getDownloaded();
                    getMetaData().setSpeed(downloaded[1] - downloaded[0]);
                    getMetaData().setDownloadedSize(downloaded[1]);
                    log.info("设置下载的元数据，下载速度为：{}，已下载字节数为{}", getMetaData().getSpeed(), getMetaData().getDownloadedSize());
                }, torrentRegistry -> {
                    log.info("持久化种子信息");
                    if (torrentRegistry instanceof TorrentPersist) {
                        TorrentPersist persist = (TorrentPersist) torrentRegistry;
                        persist.serializeDescriptors();
//                        persist.serializeTorrents();
                    }
                }, 1000);
        log.info("新增BT下载任务，任务ID为{}，文件总大小为{}", getMissionId(), getMetaData().getFileSize());
    }

    protected BtClientBuilder getBtClientBuilder(BtRuntime runtime) {
        // 创建文件存储位置
        Path targetDirectory = Paths.get(getMetaData().getTargetDirectory());
        Storage storage = new FileSystemStorage(targetDirectory);

        return Bt.client(runtime)
                .storage(storage);
    }

    protected BtRuntime getBtRuntime() {
        // 开启多线程下载的配置
        Config config = new Config() {
            @Override
            public int getNumOfHashingThreads() {
                return getMetaData().getThreadNum();
            }
        };

        // 开启从公开路由器启动 （enable bootstrapping from public routers）
        Module dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });
        return BtRuntime.builder().config(config).autoLoadModules().module(dhtModule).build();
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
