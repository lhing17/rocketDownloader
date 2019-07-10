package com.ccjiuhong.download;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccjiuhong.monitor.AutoSaver;
import com.ccjiuhong.monitor.MissionMonitor;
import com.ccjiuhong.monitor.SpeedMonitor;
import com.ccjiuhong.util.DownloadUtil;
import com.ccjiuhong.util.Tested;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.ccjiuhong.util.SslUtil.DO_NOT_VERIFY;
import static com.ccjiuhong.util.SslUtil.trustAllHosts;

/**
 * 指一个下载任务的对象，一个下载任务可以由多个线程组成
 *
 * @author G. Seinfeld
 * @date 2019/06/28
 */
@Data
@Slf4j
public class DownloadMission {
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * 任务ID
     */
    private int missionId;
    /**
     * 目标文件URL
     */
    private String fileUrl;
    /**
     * 目标文件夹
     */
    private String targetDirectory;
    /**
     * 目标文件名
     */
    private String targetFileName;
    /**
     * 任务监测器
     */
    private MissionMonitor missionMonitor = new MissionMonitor(this);
    /**
     * 速度监测器
     */
    private SpeedMonitor speedMonitor = new SpeedMonitor(this);
    /**
     * 自动保存器，用于自动保存下载进度
     */
    private AutoSaver autoSaver = new AutoSaver(this);
    /**
     * 下载线程列表
     */
    private List<DownloadRunnable> runnableList = new ArrayList<>();
    /**
     * 下载状态
     */
    private EnumDownloadStatus downloadStatus = EnumDownloadStatus.READY;
    /**
     * 用于执行速度监测的定时线程池
     */
    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
    /**
     * 进度文件
     */
    private File progressFile;
    /**
     * 一个下载任务分配的最大线程数
     */
    private static final int MAX_THREAD_PER_MISSION = 5;

    private static final int MAX_DEFAULT_BYTE_SIZE = 1024;

    private static final String DOWNLOAD_INFO_SUFFIX = "dl.json";

    public DownloadMission(int missionId, String fileUrl, String targetDirectory, String targetFileName) {
        this.missionId = missionId;
        this.fileUrl = fileUrl;
        this.targetDirectory = targetDirectory;
        this.targetFileName = targetFileName;
        this.progressFile = new File(DownloadUtil.getName(targetDirectory, String.valueOf(missionId),
                DOWNLOAD_INFO_SUFFIX));
    }

    /**
     * 开启当前下载任务
     *
     * @param downloadThreadPool 下载的线程池
     * @return 开启成功返回true，否则返回false
     */
    public boolean start(DownloadThreadPool downloadThreadPool) {
        assertMissionStateCorrect(downloadThreadPool);

        // 开启速度监测
        executorService.scheduleAtFixedRate(speedMonitor, 0, 1, TimeUnit.SECONDS);
        // 开启自动保存进度
        executorService.scheduleAtFixedRate(autoSaver, 0, 1, TimeUnit.SECONDS);
        // 开启线程任务执行
        for (int i = 0; i < MAX_THREAD_PER_MISSION; i++) {
            long start = i * (fileSize / MAX_THREAD_PER_MISSION);
            long end = (i == MAX_THREAD_PER_MISSION - 1)
                    ? fileSize
                    : (i + 1) * (fileSize / MAX_THREAD_PER_MISSION);
            DownloadRunnable downloadRunnable =
                    new DownloadRunnable(targetDirectory, targetFileName, fileUrl, missionMonitor, start, end);

            log.info("新增下载线程，任务ID为{}，文件大小为{}，开始位置为{}，结束位置为{}", missionId, fileSize, start, end);
            downloadThreadPool.submit(downloadRunnable);
            runnableList.add(downloadRunnable);
        }
        // 修改任务状态
        downloadStatus = EnumDownloadStatus.compareAndSetDownloadStatus(downloadStatus, EnumDownloadStatus.DOWNLOADING);
        // 存储下载信息
        saveOrUpdateDownloadInfo(runnableList);
        return true;
    }

    /**
     * 暂停当前任务
     *
     * @param downloadThreadPool 下载的线程池
     * @return 暂停成功返回true，否则返回false
     */
    public boolean pause(DownloadThreadPool downloadThreadPool) {
        try {
            assertMissionStateCorrect(downloadThreadPool);
            downloadThreadPool.pause(missionId);
            // 修改任务状态为暂停
            downloadStatus = EnumDownloadStatus.compareAndSetDownloadStatus(downloadStatus, EnumDownloadStatus.PAUSED);
            // 存储下载信息
            saveOrUpdateDownloadInfo(runnableList);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 继续下载
     *
     * @param downloadThreadPool 下载的线程池
     * @return 继续成功返回true，否则返回false
     */
    public boolean resume(DownloadThreadPool downloadThreadPool) {
        try {
            assertMissionStateCorrect(downloadThreadPool);
            // 开启速度监测
            executorService.scheduleAtFixedRate(speedMonitor, 0, 1, TimeUnit.SECONDS);
            // 开启自动保存进度
            executorService.scheduleAtFixedRate(autoSaver, 0, 1, TimeUnit.SECONDS);
            resumeDownloadMission(downloadThreadPool);
            // 修改任务状态
            downloadStatus = EnumDownloadStatus.compareAndSetDownloadStatus(downloadStatus,
                    EnumDownloadStatus.DOWNLOADING);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 继续下载任务
     *
     * @param downloadThreadPool 任务连接池
     */
    private void resumeDownloadMission(DownloadThreadPool downloadThreadPool) {
        if (readDownloadInfo()) {
            this.runnableList.forEach(downloadThreadPool::submit);
        } else {
            throw new IllegalStateException("任务无法继续，请检查下载信息文件");
        }
    }

    /**
     * 读取下载信息文件
     *
     * @return 读取结果
     */
    private boolean readDownloadInfo() {

        try (FileInputStream fileInputStream = new FileInputStream(progressFile)) {
            FileChannel fc = fileInputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(MAX_DEFAULT_BYTE_SIZE);
            StringBuilder sb = new StringBuilder();
            while (fc.read(buffer) != -1) {
                buffer.flip();
                Charset charset = Charset.forName("utf-8");
                CharBuffer charBuffer = charset.decode(buffer);
                while (charBuffer.hasRemaining()) {
                    // 读取buffer当前位置的整数
                    char b = charBuffer.get();
                    sb.append(b);
                }
                buffer.clear();
            }
            String downloadInfoJsonStr = sb.toString();
            this.runnableList.clear();

            // 通过文件中的下载信息还原下载
            JSONObject downloadInfo = JSON.parseObject(downloadInfoJsonStr);
            JSONArray positionInfoList = downloadInfo.getJSONArray("positionInfoList");

            for (Object positionInfo : positionInfoList) {
                DownloadRunnable downloadRunnable = new DownloadRunnable(downloadInfo.getString("targetDirectory"),
                        downloadInfo.getString("targetFileName"), downloadInfo.getString("fileUrl"), missionMonitor,
                        ((JSONObject) positionInfo).getLong("startPosition"), ((JSONObject) positionInfo).getLong(
                        "currentPosition"), ((JSONObject) positionInfo).getLong("endPosition"));
                runnableList.add(downloadRunnable);
                // 还原已下载的字节数
                missionMonitor.down(downloadRunnable.getCurrentPosition() - downloadRunnable.getStartPosition());
            }

            return true;
        } catch (Exception e) {
            log.error("读取文件失败, read error is " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除当前下载任务
     *
     * @param downloadThreadPool 下载的线程池
     * @return 删除成功返回true，否则返回false
     */
    public boolean delete(DownloadThreadPool downloadThreadPool) {
        try {
            downloadThreadPool.cancel(missionId);
            this.runnableList.clear();
            if (!progressFile.delete()) {
                log.warn("删除文件失败");
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 存储下载信息
     *
     * @param runnableList 下载信息集合
     */
    @Tested()
    public void saveOrUpdateDownloadInfo(List<DownloadRunnable> runnableList) {
        if (runnableList.size() <= 0) {
            throw new IllegalStateException("当前没有下载任务");
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(progressFile)) {
            if (!progressFile.exists()) {
                boolean newFile = progressFile.createNewFile();
                if (!newFile) {
                    throw new IllegalStateException("创建信息文件失败");
                }
            }
            byte[] bytes = JSONObject.toJSONString(new DownloadInfo(runnableList)).getBytes(StandardCharsets.UTF_8);
            FileChannel fc = fileOutputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(bytes);
            buffer.flip();
            fc.write(buffer);
        } catch (IOException e) {
            log.error("存储更新下载信息文件失败", e);
        }
    }

    /**
     * 从服务器获取文件大小
     *
     * @param fileUrl 文件地址
     * @return 文件大小
     */
    private long getFileSizeFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            boolean useHttps = fileUrl.startsWith("https");
            if (useHttps) {
                HttpsURLConnection https = (HttpsURLConnection) urlConnection;
                trustAllHosts(https);
                https.setHostnameVerifier(DO_NOT_VERIFY);
                return https.getContentLengthLong();
            }
            return urlConnection.getContentLengthLong();
        } catch (IOException e) {
            log.error("从服务器获取文件大小失败", e);
            return 0;
        }
    }

    /**
     * 调用此方法确保任务的状态是正确的
     *
     * @param downloadThreadPool 下载任务使用的线程池
     */
    private void assertMissionStateCorrect(DownloadThreadPool downloadThreadPool) {
        // 线程池已停止
        if (downloadThreadPool.isTerminated()) {
            throw new IllegalStateException("线程池已停止");
        }
        // 获取文件大小，获取失败则返回false
        if ((fileSize = getFileSizeFromUrl(fileUrl)) == 0) {
            throw new IllegalStateException("获取文件大小失败");
        }
    }


}
