package com.ccjiuhong.download;

import com.ccjiuhong.monitor.MissionMonitor;
import com.ccjiuhong.monitor.SpeedMonitor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author G. Seinfeld
 * @date 2019/06/27
 */
@Data
@Slf4j
public class DownloadRunnable implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    /**
     * 1048576字节，即1MB
     */
    private static final long SPLIT_THRESHOLD = 1 << 20;
    /**
     * 目标文件夹
     */
    private String targetDirectory;
    /**
     * 目标文件名
     */
    private String targetFileName;
    /**
     * 文件下载地址
     */
    private String fileUrl;
    /**
     * 任务监测器，用于监测某个线程下载的进度
     */
    private MissionMonitor missionMonitor;
    /**
     * 下载起始位置
     */
    private long startPosition;
    /**
     * 当前指针所在位置
     */
    private long currentPosition;
    /**
     * 文件结束位置
     */
    private long endPosition;

    /**
     * 线程ID
     */
    private int id;

    private static int ID_COUNTER = 0;


    /**
     * 构造新的下载线程
     *
     * @param targetDirectory 目标目录
     * @param targetFileName  目标文件名
     * @param fileUrl         文件下载地址
     * @param startPosition   下载开始位置
     * @param currentPosition 当前位置
     * @param endPosition     结束位置
     */
    public DownloadRunnable(String targetDirectory, String targetFileName, String fileUrl, MissionMonitor missionMonitor,
                            long startPosition, long currentPosition, long endPosition) {
        this.targetDirectory = targetDirectory;
        this.targetFileName = targetFileName;
        this.fileUrl = fileUrl;
        this.missionMonitor = missionMonitor;
        this.startPosition = startPosition;
        this.currentPosition = currentPosition;
        this.endPosition = endPosition;
        this.id = ID_COUNTER++;
    }

    /**
     * 不提供当前位置，则以开始位置作为当前位置
     */
    public DownloadRunnable(String targetDirectory, String targetFileName, String fileUrl, MissionMonitor missionMonitor, long startPosition, long endPosition) {
        this(targetDirectory, targetFileName, fileUrl, missionMonitor, startPosition, startPosition, endPosition);
    }

    @Override
    public void run() {


        File targetFile = createTargetFile();
        BufferedInputStream bufferedInputStream;
        RandomAccessFile randomAccessFile;
        byte[] buffer = new byte[BUFFER_SIZE];
        URLConnection urlConnection;
        try {
            URL url = new URL(fileUrl);
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Range", "bytes=" + currentPosition + "-" + endPosition);
            randomAccessFile = new RandomAccessFile(targetFile, "rw");
            randomAccessFile.seek(currentPosition);
            bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
            while (currentPosition < endPosition) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                int len = bufferedInputStream.read(buffer, 0, BUFFER_SIZE);
                if (len == -1) {
                    break;
                } else {
                    randomAccessFile.write(buffer, 0, len);
                    currentPosition += len;
                    missionMonitor.down(len);
                }
            }
            bufferedInputStream.close();
            randomAccessFile.close();
        } catch (IOException e) {
            log.error("文件读取不正确", e);
        }
    }

    /**
     * 创建任务对应的目标文件，这里考虑到并发问题，对创建文件使用了同步锁
     *
     * @return 文件对象
     */
    private File createTargetFile() {
        File targetFile;
        synchronized (this) {
            File dir = new File(targetDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            targetFile = new File(targetDirectory + File.separator + targetFileName);
            if (!targetFile.exists()) {
                try {
                    targetFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return targetFile;
    }

    /**
     * 将当前下载线程一分为二，拆分出一个
     */
    public DownloadRunnable split() {
        long remaining = endPosition - currentPosition;
        long half = remaining >> 1;
        if (half > SPLIT_THRESHOLD) {
            long newEndPosition = endPosition;
            long newStartPosition = currentPosition + half + 1;
            endPosition = newStartPosition - 1;
            return new DownloadRunnable(targetDirectory, targetFileName, fileUrl, missionMonitor, newStartPosition, newEndPosition);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "DownloadRunnable{" + targetFileName + ":" + id + "}";
    }
}
