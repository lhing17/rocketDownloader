package com.ccjiuhong.mission;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccjiuhong.download.DownloadInfo;
import com.ccjiuhong.download.DownloadRunnable;
import com.ccjiuhong.download.DownloadThreadPool;
import com.ccjiuhong.download.EnumDownloadStatus;
import com.ccjiuhong.monitor.AutoSaver;
import com.ccjiuhong.util.DownloadUtil;
import com.ccjiuhong.util.Tested;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * 从服务器下载到客户端的任务
 *
 * @author G. Seinfeld
 * @since 2020/01/07
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ServerToClientMission extends GenericMission {
    /**
     * 目标文件URL
     */
    protected String fileUrl;

    /**
     * 下载任务所用的线程池
     */
    protected DownloadThreadPool downloadThreadPool;
    /**
     * 任务监测器
     */
    protected MissionMonitor missionMonitor = new MissionMonitor();
    /**
     * 速度监测器
     */
    protected SpeedMonitor speedMonitor = new SpeedMonitor();
    /**
     * 自动保存器，用于自动保存下载进度
     */
    protected AutoSaver autoSaver = new AutoSaver(this);
    /**
     * 下载线程列表
     */
    protected List<DownloadRunnable> runnableList = new ArrayList<>();
    /**
     * 下载状态
     */
    protected EnumDownloadStatus status = EnumDownloadStatus.READY;

    /**
     * 用于执行速度监测的定时线程池
     */
    protected ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
    /**
     * 进度文件
     */
    protected File progressFile;

    /**
     * 一个下载任务分配的最大线程数
     */
    protected static final int MAX_THREAD_PER_MISSION = 5;

    protected static final int MAX_DEFAULT_BYTE_SIZE = 1024;

    public ServerToClientMission(int missionId, String fileUrl, String targetDirectory, String targetFileName, DownloadThreadPool downloadThreadPool) {
        super(missionId, targetDirectory, targetFileName);
        this.progressFile = getMetaData().getProgressFile();
        this.fileUrl = fileUrl;
        this.downloadThreadPool = downloadThreadPool;
    }

    public ServerToClientMission(int missionId, String targetDirectory, String targetFileName) {
        super(missionId, targetDirectory, targetFileName);
    }


    /**
     * 开启当前下载任务
     *
     * @return 开启成功返回true，否则返回false
     */
    public boolean start() {
        assertMissionStateCorrect(downloadThreadPool);

        // 开启速度监测
        executorService.scheduleAtFixedRate(speedMonitor, 0, 1, TimeUnit.SECONDS);
        // 开启自动保存进度
        executorService.scheduleAtFixedRate(autoSaver, 0, 1, TimeUnit.SECONDS);

        // TODO 线程数可配置
        int threadNum = Math.min(getMetaData().getThreadNum(), MAX_THREAD_PER_MISSION);
        // 开启线程任务执行
        for (int i = 0; i < threadNum; i++) {
            long fileSize = getMetaData().getFileSize();
            long start = i * (fileSize / threadNum);
            long end = (i == threadNum - 1)
                    ? fileSize
                    : (i + 1) * (fileSize / threadNum);
            DownloadRunnable downloadRunnable =
                    new DownloadRunnable(getMetaData().getTargetDirectory(), getMetaData().getTargetFileName(), fileUrl, this, start, end);

            log.info("新增下载线程，任务ID为{}，文件大小为{}，开始位置为{}，结束位置为{}", getMissionId(), fileSize, start, end);
            downloadThreadPool.submit(downloadRunnable);
            runnableList.add(downloadRunnable);
        }
        // 修改任务状态
        status = EnumDownloadStatus.compareAndSetDownloadStatus(status, EnumDownloadStatus.DOWNLOADING);
        // 存储下载信息
        saveOrUpdateDownloadInfo(runnableList);
        return true;
    }

    /**
     * 暂停当前任务
     *
     * @return 暂停成功返回true，否则返回false
     */
    public boolean pause() {
        try {
            assertMissionStateCorrect(downloadThreadPool);
            downloadThreadPool.pause(getMissionId());
            // 修改任务状态为暂停
            status = EnumDownloadStatus.compareAndSetDownloadStatus(status, EnumDownloadStatus.PAUSED);
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
     * @return 继续成功返回true，否则返回false
     */
    public boolean resume() {
        try {
            assertMissionStateCorrect(downloadThreadPool);
            // 开启速度监测
            executorService.scheduleAtFixedRate(speedMonitor, 0, 1, TimeUnit.SECONDS);
            // 开启自动保存进度
            executorService.scheduleAtFixedRate(autoSaver, 0, 1, TimeUnit.SECONDS);
            resumeDownloadMission(downloadThreadPool);
            // 修改任务状态
            status = EnumDownloadStatus.compareAndSetDownloadStatus(status,
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
                Charset charset = StandardCharsets.UTF_8;
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
                        downloadInfo.getString("targetFileName"), downloadInfo.getString("fileUrl"), this,
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
     * @return 删除成功返回true，否则返回false
     */
    public boolean delete() {
        try {
            downloadThreadPool.cancel(getMissionId());
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
    @Tested
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
    abstract protected long getFileSizeFromUrl(String fileUrl);


    /**
     * 调用此方法确保任务的状态是正确的
     *
     * @param downloadThreadPool 下载任务使用的线程池
     */
    protected void assertMissionStateCorrect(DownloadThreadPool downloadThreadPool) {
        // 线程池已停止
        if (downloadThreadPool.isTerminated()) {
            throw new IllegalStateException("线程池已停止");
        }
        // 获取文件大小，获取失败则返回false
        if ((getFileSizeFromUrl(fileUrl)) == 0) {
            throw new IllegalStateException("获取文件大小失败");
        }
    }

    @Getter
    public class MissionMonitor {
        /**
         * 已经下载的字节数，这里考虑到线程安全的问题，使用JUC包中的AtomicLong，确保计算的原子性
         */
        private AtomicLong downloadedSize = new AtomicLong();

        /**
         * 累加已下载字节数
         *
         * @param size 下载字节数
         */
        public void down(long size) {
            downloadedSize.addAndGet(size);
            if (getMetaData().getFileSize() == downloadedSize.get()) {
                getMetaData().setStatus(EnumDownloadStatus.FINISHED);
            }
        }

    }

    @Getter
    class SpeedMonitor implements Runnable {

        private long lastSecondSize;
        private long currentSize;
        private long speed;


        @Override
        public void run() {
            lastSecondSize = currentSize;
            currentSize = getMissionMonitor().getDownloadedSize().get();
            speed = currentSize - lastSecondSize;
            getMetaData().setSpeed(speed);
            if (!EnumDownloadStatus.FINISHED.equals(getMetaData().getStatus())) {
                log.info("当前下载任务为{}，下载速度为{}", getMissionId(), DownloadUtil.getReadableSpeed(speed));
            }
        }
    }
}
