package com.ccjiuhong.mission;

import bt.Bt;
import bt.BtClientBuilder;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.IMetadataService;
import bt.metainfo.Torrent;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import com.ccjiuhong.download.EnumDownloadStatus;
import com.google.inject.Module;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * BT下载任务
 *
 * @author G. Seinfeld
 * @since 2019/12/11
 */
@Slf4j
public class BitTorrentMission extends GenericMission {

    protected CompletableFuture<?> completableFuture;

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
             supplier = ()->{
                 Torrent torrent = runtime.service(IMetadataService.class).fromInputStream(in);
                 getMetaData().setFileSize(torrent.getSize());
                 return torrent;
             };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BtClient btClient = builder
                .torrent(Objects.requireNonNull(supplier))
                .stopWhenDownloaded()
                .build();
        startDownload(btClient);

        return true;
    }

    protected void startDownload(BtClient btClient) {
        // 开始下载
        long[] downloaded = {0L, 0L};
        getMetaData().setStatus(EnumDownloadStatus.DOWNLOADING);
        completableFuture = btClient.startAsync(torrentSessionState -> {
            // 上一秒下载字节数
            downloaded[0] = downloaded[1];
            // 当前下载字节数
            downloaded[1] = torrentSessionState.getDownloaded();
            getMetaData().setSpeed(downloaded[1] - downloaded[0]);
            getMetaData().setDownloadedSize(downloaded[1]);
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
