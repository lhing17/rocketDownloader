package com.ccjiuhong.mission;

import bt.Bt;
import bt.BtClientBuilder;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.TorrentId;
import bt.net.InetPeerAddress;
import bt.protocol.Protocols;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import bt.torrent.TorrentPersist;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccjiuhong.download.EnumDownloadStatus;
import com.ccjiuhong.util.BtInfo;
import com.ccjiuhong.util.FileUtil;
import com.google.inject.Module;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 点对点类型下载任务
 *
 * @author G. Seinfeld
 * @since 2020/01/08
 */
@Slf4j
public abstract class PeerToPeerMission extends GenericMission {

    public static ConcurrentHashMap<TorrentId, BtInfo> btInfoMap = new ConcurrentHashMap<>();

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
                    try {
                        if (torrentRegistry instanceof TorrentPersist) {
                            TorrentPersist persist = (TorrentPersist) torrentRegistry;
                            TorrentId torrentId = TorrentId.fromBytes(Protocols.fromHex(getMetaData().getUniqueIdentifier()));
                            persist.serializeDescriptors(torrentId, getMetaData().getDotTorrentFilePath());
                        }
                    } catch (Exception e) {
                        log.error("持久化种子信息失败", e);
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

    protected static BtRuntime getBtRuntime() {


        // 开启多线程下载的配置
        Config config = new Config() {
            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors() * 2;
            }
        };


        try {
            File file = new File(config.getWorkDirectory(), "descriptors.rd");
            JSONObject jsonObject = JSON.parseObject(FileUtil.readText(file));
            for (String key : jsonObject.keySet()) {
                btInfoMap.put(TorrentId.fromBytes(Protocols.fromHex(key)), BtInfo.fromJSONObject(jsonObject.getJSONObject(key)));
            }
        } catch (Exception e) {
            log.warn("未能正确读取进度文件");
        }

        // 开启从公开路由器启动 （enable bootstrapping from public routers）
        Module dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }

            // 提供 bootstrap nodes TODO 这些初始化节点可配置
            @Override
            public Collection<InetPeerAddress> getBootstrapNodes() {
                return Arrays.asList(
                        new InetPeerAddress("router.magnets.im", 6881),
                        new InetPeerAddress("router.bitcomet.com", 6881),  // BitComet
                        new InetPeerAddress("dht.aelitis.com", 6881),  // Vuze
                        new InetPeerAddress("router.silotis.us", 6881), // IPv6
                        new InetPeerAddress("dht.libtorrent.org", 25401) // @arvidn's
                );
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
