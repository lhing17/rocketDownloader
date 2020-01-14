package com.ccjiuhong.mission;

import bt.magnet.MagnetUri;
import bt.magnet.MagnetUriParser;
import bt.metainfo.TorrentId;
import com.ccjiuhong.download.DownloadThreadPool;
import com.ccjiuhong.download.EnumDownloadStatus;
import com.ccjiuhong.util.BtInfo;
import com.ccjiuhong.util.DownloadUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 生产各类任务的工厂
 *
 * @author G. Seinfeld
 * @since 2019/12/12
 */
@Slf4j
public class MissionFactory {
    /**
     * 下载任务的ID，从0开始，每次加1
     */
    private static int serialMissionId = 0;

    public Mission createMissionIntelligently(String fileUrl, String targetDirectory, String targetFileName, DownloadThreadPool downloadThreadPool, boolean isBt) {
        // 处理BT种子下载（不包括磁力链）
        if (isBt) {
            return new BitTorrentMission(serialMissionId++, fileUrl, targetDirectory, targetFileName);
        }

        fileUrl = fileUrl.trim();
        // 处理thunder等下载协议
        fileUrl = DownloadUtil.decodeIfNecessary(fileUrl);


        if (fileUrl.startsWith("http") || fileUrl.startsWith("https")) {
            return new HttpMission(serialMissionId++, fileUrl, targetDirectory,
                    targetFileName, downloadThreadPool);
        }
        if (fileUrl.startsWith("ftp")) {
            return new FtpMission(serialMissionId++, fileUrl, targetDirectory,
                    targetFileName, downloadThreadPool);
        }
        if (fileUrl.startsWith("magnet")) {
            try {
                MagnetUri magnetUri = MagnetUriParser.lenientParser().parse(fileUrl);
                TorrentId torrentId = magnetUri.getTorrentId();
                if (PeerToPeerMission.btInfoMap.containsKey(torrentId)) {
                    BtInfo btInfo = PeerToPeerMission.btInfoMap.get(torrentId);
                    String torrentFilePath = btInfo.getDotTorrentFilePath();
                    Mission mission = new BitTorrentMission(serialMissionId++, torrentFilePath, targetDirectory, targetFileName);
                    mission.getMetaData().setStatus(EnumDownloadStatus.PAUSED);
                    return mission;
                }
            } catch (Exception e) {
                log.debug("创建BT下载任务失败", e);
            }
            return new MagnetMission(serialMissionId++, fileUrl, targetDirectory, targetFileName);
        }

        throw new RuntimeException("无法识别的文件地址");
    }


}
