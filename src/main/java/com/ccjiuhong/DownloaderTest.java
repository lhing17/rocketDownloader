package com.ccjiuhong;

import com.ccjiuhong.mgt.DefaultDownloadManager;
import com.ccjiuhong.mgt.DownloadManager;
import com.ccjiuhong.mission.BitTorrentMission;
import com.ccjiuhong.mission.MagnetMission;
import com.ccjiuhong.mission.Mission;
import com.ccjiuhong.util.DownloadUtil;
import com.ccjiuhong.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.net.ftp.FTPClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 使用代码运行下载项目的测试类
 *
 * @author G.Seinfeld
 * @date 2019/06/29
 */
@Slf4j
public class DownloaderTest {
    public static void main(String[] args) {
//        testStartOrResumeMagnetMission();
//        testStartOrResumeBitTorrentMission();
//        decodeUrl();
        testStartOrResumeHttpMission();
//        List<String> urls = getAvailableUrls();
//        for (String url : urls) {
//            System.out.println(url);
//        }
    }

    private static void decodeUrl() {
        String url = "thunder://QUFtYWduZXQ6P3h0PXVybjpidGloOkM3Mjk1NkUxRUExMTU0NzEyOEIwRUY1ODkzMzhBNjhCNEM2M0Y2QzAmZG49SUJXLTUxOFpaWg==";
        String decodedUrl = DownloadUtil.decodeIfNecessary(url);
        System.out.println(decodedUrl);
    }


    private static void testStartOrResumeHttpMission() {
        DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();
        String fileUrl = "ftp://ygdy8:ygdy8@yg39.dydytt.net:7004/阳光电影www.ygdy8.com.灭狼行动.HD.1080p.国语中字.mp4";
//        String fileUrl = "https://dldir1.qq.com/qqfile/qq/PCQQ9.1.5/25530/QQ9.1.5.25530.exe";
//        String fileUrl = "https://raw.githubusercontent.com/Himself65/LianXue/master/public/1.png";
//        String fileUrl = "https://download.oracle.com/otn/java/jdk/11.0
//        .3+12/37f5e150db5247ab9333b11c1dddcd30/jdk-11.0.3_windows-x64_bin
//        .zip?AuthParam=1561862894_d3c07f3538fb5d9f5c2a3df110fadbe4";
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//        String targetFileName = "jdk-11.0.3_windows-x64_bin.zip";
        int missionId = defaultDownloadManager.addMission(fileUrl, "/home/lhing17/rocketDownloader", targetFileName);
        defaultDownloadManager.startOrResumeMission(missionId);

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(() -> log.info("当前下载百分比为：" + defaultDownloadManager.getReadableDownloadedPercent(missionId)), 0, 1, TimeUnit.SECONDS);
    }

    private static void testStartOrResumeBitTorrentMission() {
        DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();
        String fileUrl = "/home/lhing17/d.torrent";
        Mission mission = new BitTorrentMission(1, fileUrl, "/home/lhing17/rocketDownloader", "a");
        defaultDownloadManager.addMission(mission);
        defaultDownloadManager.startOrResumeMission(1);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(() -> log.info("当前下载百分比为：" + defaultDownloadManager.getReadableDownloadedPercent(1)), 0, 1, TimeUnit.SECONDS);
    }

    private static void testStartOrResumeMagnetMission() {
        DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();
        String fileUrl = "magnet:?xt=urn:btih:5800E072B34BA6DD2AE2BF72940BBD4A831A6AC2&dn=Colette.15.10.09.Piper.Perri.Orgy.Is.The.New.Black.XXX.1080p.MP4-KTR%5Brarbg%5D";
        Mission mission = new MagnetMission(1, fileUrl, "/home/lhing17/rocketDownloader", "a");
        defaultDownloadManager.addMission(mission);
        defaultDownloadManager.startOrResumeMission(1);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(() -> log.info("当前下载百分比为：" + defaultDownloadManager.getReadableDownloadedPercent(1)), 0, 1, TimeUnit.SECONDS);
    }

    private static List<String> getAvailableUrls() {
        List<String> urls = new ArrayList<>();
        for (int prefix = 10; prefix < 100; prefix++) {
            for (int port = 7000; port < 10000; port++) {
                String address = "yg" + prefix + ".dydytt.net";
                System.out.println(address + ":" + port);
                Optional<FTPClient> login = FtpUtil.login(address, port, "ygdy8", "ygdy8");
                if (login.isPresent()) {
                    urls.add(address + ":" + port);
                }
            }
        }
        return urls;
    }
}
